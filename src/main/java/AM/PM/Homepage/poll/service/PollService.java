package AM.PM.Homepage.poll.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.poll.entity.Poll;
import AM.PM.Homepage.poll.entity.PollOption;
import AM.PM.Homepage.poll.entity.PollResultVisibility;
import AM.PM.Homepage.poll.entity.PollVote;
import AM.PM.Homepage.poll.repository.PollOptionRepository;
import AM.PM.Homepage.poll.repository.PollRepository;
import AM.PM.Homepage.poll.repository.PollVoteRepository;
import AM.PM.Homepage.poll.request.PollCreateRequest;
import AM.PM.Homepage.poll.request.PollSearchParam;
import AM.PM.Homepage.poll.request.PollVoteRequest;
import AM.PM.Homepage.poll.response.PollDetailResponse;
import AM.PM.Homepage.poll.response.PollOptionResponse;
import AM.PM.Homepage.poll.response.PollResultOptionResponse;
import AM.PM.Homepage.poll.response.PollResultResponse;
import AM.PM.Homepage.poll.response.PollSummaryResponse;
import AM.PM.Homepage.poll.response.PollVoteDto;
import AM.PM.Homepage.poll.response.PollVoterResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final StudentRepository studentRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;

    // 전체 투표 검색
    @Transactional(readOnly = true)
    public Page<PollSummaryResponse> searchPoll(PollSearchParam params, Pageable pageable) {
        log.debug("[투표 검색 시작] params={}, pageable={}", params, pageable);

        Page<PollSummaryResponse> page = pollRepository.searchByParam(params, pageable);

        log.debug("[투표 검색 종료] 전체 개수={}, 전체 페이지={}", page.getTotalElements(), page.getTotalPages());
        return page;
    }

    // 투표 상세 조회
    @Transactional(readOnly = true)
    public PollDetailResponse getPollDetail(Long pollId, Long studentId) {
        log.debug("[투표 상세 조회 시작] pollId={}, studentId={}", pollId, studentId);

        PollDetailResponse pollResponse = pollRepository.findPollDetailResponseById(pollId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POLL));

        List<PollOptionResponse> options = pollRepository.findPollOptionResponsesByPollId(pollId);
        pollResponse.setOptions(options);
        log.debug("[투표 옵션 로드 완료] pollId={}, 옵션 수={}", pollId, options.size());

        // 비로그인
        if (studentId == null) {
            pollResponse.setVoted(false);
            log.debug("[투표 상세 조회 완료] 비로그인 사용자, 내 선택 없음 | pollId={}", pollId);
            return pollResponse;
        }

        Set<Long> mySelectedOptionIds = pollRepository.findOptionIdsByPollIdAndUserId(pollId, studentId);
        pollResponse.setMySelectedOptionIds(mySelectedOptionIds);
        pollResponse.setVoted(!mySelectedOptionIds.isEmpty());
        log.debug("[투표 상세 조회 완료] 내 선택 개수={} | pollId={}, studentId={}", mySelectedOptionIds.size(), pollId, studentId);
        return pollResponse;
    }

    // 투표 결과 조회
    @Transactional(readOnly = true)
    public PollResultResponse getPollResult(Long pollId, Long studentId) {
        log.debug("[투표 결과 조회 시작] pollId={}, studentId={}", pollId, studentId);

        PollResultResponse result = new PollResultResponse();
        var opt = pollRepository.findPollDetailResponseById(pollId);
        if (opt.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_POLL);
        }
        PollDetailResponse poll = opt.get();
        result.setPoll(poll);

        // 공개 정책 체크
        PollResultVisibility vis = poll.getResultVisibility();
        if (vis == PollResultVisibility.AUTHENTICATED && studentId == null) {
            log.info("[투표 결과 숨김] 로그인 사용자만 열람 가능 | pollId={}", pollId);
            return result;
        }
        if (vis == PollResultVisibility.ADMIN_ONLY && !isAdmin(studentId)) {
            log.info("[투표 결과 숨김] 관리자만 열람 가능 | pollId={}, studentId={}", pollId, studentId);
            return result;
        }
        if (vis == PollResultVisibility.AFTER_CLOSE && poll.isOpen()) {
            log.info("[투표 결과 숨김] 투표 마감 후 공개 설정 | pollId={}", pollId);
            return result;
        }

        // 내 선택
        Set<Long> mySelectedOptionIds;
        if (studentId != null) {
            mySelectedOptionIds = pollRepository.findOptionIdsByPollIdAndUserId(pollId, studentId);
            result.setMySelectedOptionIds(mySelectedOptionIds);
            result.setVoted(!mySelectedOptionIds.isEmpty());
            log.debug("[내 선택 로드] pollId={}, studentId={}, 선택 수={}", pollId, studentId, mySelectedOptionIds.size());
        }

        // 옵션 및 투표 결과 로드
        List<PollOptionResponse> metas = pollRepository.findPollOptionResponsesByPollId(pollId);
        boolean anonymous = poll.isAnonymous();
        List<PollVoteDto> votes = anonymous
                ? pollRepository.findAllVoteAnonymousResponseByPollId(pollId)
                : pollRepository.findAllVoteResponseByPollId(pollId);

        Map<Long, List<PollVoterResponse>> votersByOpt = new HashMap<>();
        for (var v : votes) {
            votersByOpt.computeIfAbsent(v.getOptionId(), k -> new ArrayList<>())
                    .add(new PollVoterResponse(v.getStudentId(), v.getStudentName()));
        }

        List<PollResultOptionResponse> options = metas.stream()
                .map(m -> new PollResultOptionResponse(
                        m.getId(),
                        m.getLabel(),
                        votersByOpt.getOrDefault(m.getId(), List.of()).size(),
                        anonymous ? null : votersByOpt.getOrDefault(m.getId(), List.of())
                ))
                .toList();

        result.setOptions(options);
        log.debug("[투표 결과 조회 완료] pollId={}, 옵션 수={}", pollId, options.size());
        return result;
    }

    // 투표 생성
    public PollSummaryResponse create(PollCreateRequest request, Long studentId) {
        log.info("[투표 생성 요청] studentId={}, 제목='{}'", studentId, request.getTitle());

        if (studentId == null || !studentRepository.existsById(studentId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Poll poll = Poll.createFrom(request, studentId);
        if (request.getOptions() != null) {
            request.getOptions().forEach(opt -> poll.addOption(PollOption.create(poll, opt, studentId)));
        }

        pollRepository.save(poll);
        log.info("[투표 생성 성공] pollId={}, 제목='{}', 생성자={}", poll.getId(), poll.getTitle(), studentId);
        return PollSummaryResponse.from(poll);
    }

    // 투표하기
    public void vote(PollVoteRequest request, Long pollId, Long studentId) {
        if (studentId == null || !studentRepository.existsById(studentId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POLL));
        if (!poll.isOpen()) {
            throw new CustomException(ErrorCode.CLOSED_POLL);
        }

        if (request.getOptionIds() == null) {
            throw new CustomException(ErrorCode.INVALID_POLL_NO_SELECTION);
        }
        var requested = new LinkedHashSet<>(request.getOptionIds());
        if (!poll.isMultiple() && requested.size() != 1) {
            throw new CustomException(ErrorCode.INVALID_POLL_SINGLE_SELECTION);
        }
        if (requested.size() > poll.getMaxSelect()) {
            throw new CustomException(ErrorCode.INVALID_POLL_MAX_SELECTION);
        }

        var options = pollOptionRepository.findByPollIdAndIdIn(pollId, requested);
        if (options.size() != requested.size()) {
            throw new CustomException(ErrorCode.INVALID_POLL_OPTION);
        }
        var optionMap = options.stream().collect(Collectors.toMap(PollOption::getId, o -> o));

        var existingVotes = pollVoteRepository.findByPollIdAndVoterId(pollId, studentId);
        var existing = existingVotes.stream().map(v -> v.getOption().getId()).collect(Collectors.toSet());

        if (!existing.isEmpty() && !poll.isAllowRevote()) {
            throw new CustomException(ErrorCode.RE_VOTE_NOT_ALLOWED);
        }

        var toAdd = new HashSet<>(requested);
        var toDel = new HashSet<>(existing);
        toAdd.removeAll(existing);
        toDel.removeAll(requested);

        if (!toDel.isEmpty()) {
            pollVoteRepository.deleteByPollIdAndVoterIdAndOptionIdIn(pollId, studentId, toDel);
        }

        if (!toAdd.isEmpty()) {
            var newVotes = toAdd.stream()
                    .map(optId -> new PollVote(poll, optionMap.get(optId), studentId, LocalDateTime.now()))
                    .toList();
            try {
                pollVoteRepository.saveAll(newVotes);
            } catch (DataIntegrityViolationException e) {
                log.warn("[투표 실패: 중복 요청] pollId={}, studentId={}", pollId, studentId);
                throw new CustomException(ErrorCode.DUPLICATE_VOTE_REQUEST);
            }
        }

        log.info("[투표 성공] pollId={}, studentId={}, 추가={}, 삭제={}", pollId, studentId, toAdd, toDel);
    }

    // 투표 강제 마감 (생성자만)
    public PollSummaryResponse close(Long pollId, Long studentId) {
        if (studentId == null) {
            throw new CustomException(ErrorCode.FORBIDDEN_POLL_CLOSE);
        }

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POLL));

        if (!studentId.equals(poll.getCreatedBy())) {
            throw new CustomException(ErrorCode.FORBIDDEN_POLL_CLOSE);
        }

        poll.close();
        pollRepository.save(poll);
        log.info("[투표 마감 성공] pollId={}, studentId={}", pollId, studentId);
        return PollSummaryResponse.from(poll);
    }

    // 투표 삭제 (생성자만)
    public void delete(Long pollId, Long studentId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POLL));

        if (!studentId.equals(poll.getCreatedBy())) {
            throw new CustomException(ErrorCode.FORBIDDEN_POLL_DELETE);
        }

        pollRepository.delete(poll);
        log.info("[투표 삭제 성공] pollId={}, studentId={}", pollId, studentId);
    }

    // 어드민인지 확인
    private boolean isAdmin(Long studentId) {
        if (studentId == null) {
            return false;
        }
        return studentRepository.findById(studentId)
                .map(student -> student.getRole().isAdmin())
                .orElse(false);
    }
}

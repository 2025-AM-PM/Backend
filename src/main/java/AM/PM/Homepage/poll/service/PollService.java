package AM.PM.Homepage.poll.service;

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
        long t0 = System.nanoTime();
        log.debug("Poll search start | params={}, pageable={}", params, pageable);
        Page<PollSummaryResponse> page = pollRepository.searchByParam(params, pageable);
        log.debug("Poll search end   | totalElements={}, totalPages={}, tookMs={}",
                page.getTotalElements(), page.getTotalPages(), nanosToMs(t0));
        return page;
    }

    // 투표 상세 조회
    @Transactional(readOnly = true)
    public PollDetailResponse getPollDetail(Long pollId, Long studentId) {
        long t0 = System.nanoTime();
        log.debug("Get poll detail start | pollId={}, studentId={}", pollId, studentId);

        var opt = pollRepository.findPollDetailResponseById(pollId);
        if (opt.isEmpty()) {
            log.warn("Poll not found | pollId={}", pollId);
            throw new IllegalArgumentException("찾을 수 없는 투표");
        }
        PollDetailResponse poll = opt.get();

        List<PollOptionResponse> options = pollRepository.findPollOptionResponsesByPollId(pollId);
        poll.setOptions(options);
        log.debug("Loaded poll header & options | pollId={}, optionCount={}", pollId, options.size());

        // 비로그인
        if (studentId == null) {
            poll.setVoted(false);
            log.debug("Unauthenticated viewer, return without my selections | pollId={}, tookMs={}", pollId,
                    nanosToMs(t0));
            return poll;
        }

        Set<Long> mySelectedOptionIds = pollRepository.findOptionIdsByPollIdAndUserId(pollId, studentId);
        poll.setMySelectedOptionIds(mySelectedOptionIds);
        poll.setVoted(!mySelectedOptionIds.isEmpty());
        log.debug("My selections loaded | pollId={}, studentId={}, selectedCount={}, tookMs={}",
                pollId, studentId, mySelectedOptionIds.size(), nanosToMs(t0));
        return poll;
    }

    // 투표 결과 가져오기
    @Transactional(readOnly = true)
    public PollResultResponse getPollResult(Long pollId, Long studentId) {
        long t0 = System.nanoTime();
        log.debug("Get poll result start | pollId={}, studentId={}", pollId, studentId);

        PollResultResponse result = new PollResultResponse();
        var opt = pollRepository.findPollDetailResponseById(pollId);
        if (opt.isEmpty()) {
            log.warn("Poll result not found | pollId={}", pollId);
            throw new IllegalArgumentException("찾을 수 없는 투표");
        }
        PollDetailResponse poll = opt.get();
        result.setPoll(poll);

        // 공개 정책 체크
        PollResultVisibility vis = poll.getResultVisibility();
        log.debug("Result visibility check | pollId={}, visibility={}, open={}", pollId, vis, poll.isOpen());

        if (vis == PollResultVisibility.AUTHENTICATED && studentId == null) {
            log.info("Result hidden: AUTHENTICATED only | pollId={}", pollId);
            return result;
        }
        if (vis == PollResultVisibility.ADMIN_ONLY && !isAdmin(studentId)) {
            log.info("Result hidden: ADMIN_ONLY and user is not admin | pollId={}, studentId={}", pollId, studentId);
            return result;
        }
        if (vis == PollResultVisibility.AFTER_CLOSE && poll.isOpen()) {
            log.info("Result hidden: AFTER_CLOSE and poll is still open | pollId={}", pollId);
            return result;
        }

        // 내 선택(비로그인 제외)
        Set<Long> mySelectedOptionIds = Set.of();
        if (studentId != null) {
            mySelectedOptionIds = pollRepository.findOptionIdsByPollIdAndUserId(pollId, studentId);
            log.debug("My selections for result | pollId={}, studentId={}, selectedCount={}",
                    pollId, studentId, mySelectedOptionIds.size());
            result.setMySelectedOptionIds(mySelectedOptionIds);
        }
        result.setVoted(!mySelectedOptionIds.isEmpty());

        // 1) 전체 옵션 메타
        List<PollOptionResponse> metas = pollRepository.findPollOptionResponsesByPollId(pollId);
        log.debug("Loaded option metas | pollId={}, optionCount={}", pollId, metas.size());

        // 2) 투표 행 가져오기 (익명 여부에 따라 분기)
        boolean anonymous = poll.isAnonymous();
        List<PollVoteDto> votes = anonymous
                ? pollRepository.findAllVoteAnonymousResponseByPollId(pollId)
                : pollRepository.findAllVoteResponseByPollId(pollId);
        log.debug("Loaded votes | pollId={}, anonymous={}, voteRows={}", pollId, anonymous, votes.size());

        // 3) 옵션별 유권자 맵 구성
        Map<Long, List<PollVoterResponse>> votersByOpt = new HashMap<>();
        for (var v : votes) {
            var list = votersByOpt.computeIfAbsent(v.getOptionId(), k -> new ArrayList<>());
            list.add(new PollVoterResponse(v.getStudentId(), v.getStudentName()));
        }
        log.debug("Built votersByOpt map | pollId={}, keyedOptions={}", pollId, votersByOpt.size());

        // 4) 옵션별 결과 조립
        List<PollResultOptionResponse> options = metas.stream()
                .map(m -> {
                    List<PollVoterResponse> voters = votersByOpt.getOrDefault(m.getId(), List.of());
                    return new PollResultOptionResponse(
                            m.getId(),      // 옵션 id
                            m.getLabel(),   // 옵션 내용
                            voters.size(),  // 득표수
                            anonymous ? null : voters // 익명이면 상세 투표자 생략
                    );
                })
                .toList();

        result.setOptions(options);
        log.debug("Assembled result options | pollId={}, optionCount={}, tookMs={}",
                pollId, options.size(), nanosToMs(t0));
        return result;
    }

    // 투표 생성
    public PollSummaryResponse create(PollCreateRequest request, Long studentId) {
        long t0 = System.nanoTime();
        int optionSize = (request.getOptions() == null) ? 0 : request.getOptions().size();
        log.debug(
                "Create poll start | studentId={}, title='{}', optionSize={}, multiple={}, anonymous={}, maxSelect={}",
                studentId, request.getTitle(), optionSize, request.isMultiple(),
                request.isAnonymous(), request.getMaxSelect());

        if (studentId == null || !studentRepository.existsById(studentId)) {
            log.warn("Poll create rejected: unauthenticated or unknown student | studentId={}", studentId);
            throw new IllegalArgumentException("로그인 필수");
        }

        Poll poll = Poll.createFrom(request, studentId);
        if (optionSize > 0) {
            request.getOptions().forEach(opt -> poll.addOption(PollOption.create(poll, opt, studentId)));
        }

        pollRepository.save(poll);
        log.info("Poll created | pollId={}, title='{}', creator={}", poll.getId(), poll.getTitle(), studentId);
        log.debug("Create poll end | pollId={}, tookMs={}", poll.getId(), nanosToMs(t0));

        return PollSummaryResponse.from(poll);
    }

    // 투표
    public void vote(PollVoteRequest request, Long pollId, Long studentId) {
        // 0) 인증
        if (studentId == null || !studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException("로그인 필수");
        }

        // 투표 상태 확인
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("찾을 수 없는 투표"));
        if (!poll.isOpen()) {
            throw new IllegalStateException("마감된 투표");
        }

        // 요청 정제 & 개수 검증
        if (request.getOptionIds() == null) {
            throw new IllegalArgumentException("선택 항목 없음");
        }
        var requested = new LinkedHashSet<>(request.getOptionIds()); // de-dup
        if (!poll.isMultiple() && requested.size() != 1) {
            throw new IllegalArgumentException("단일 선택 투표는 1개만 가능");
        }
        if (requested.size() > poll.getMaxSelect()) {
            throw new IllegalArgumentException("최대 선택 수 초과: " + poll.getMaxSelect());
        }

        // 옵션 유효성: 모두 이 poll의 옵션인지 확인
        var options = pollOptionRepository.findByPollIdAndIdIn(pollId, requested);
        if (options.size() != requested.size()) {
            throw new IllegalArgumentException("유효하지 않은 옵션 포함");
        }
        var optionMap = options.stream()
                .collect(Collectors.toMap(PollOption::getId, o -> o));

        // 기존 내 표 조회
        var existingVotes = pollVoteRepository.findByPollIdAndVoterId(pollId, studentId);
        var existing = existingVotes.stream()
                .map(v -> v.getOption().getId())
                .collect(Collectors.toSet());

        // 재투표 정책
        if (!existing.isEmpty() && !poll.isAllowRevote()) {
            throw new IllegalStateException("재투표 불가");
        }

        // diff 계산
        var toAdd = new HashSet<>(requested);
        var toDel = new HashSet<>(existing);
        toAdd.removeAll(existing);
        toDel.removeAll(requested);

        // 삭제
        if (!toDel.isEmpty()) {
            pollVoteRepository.deleteByPollIdAndVoterIdAndOptionIdIn(pollId, studentId, toDel);
        }

        // 추가
        if (toAdd.isEmpty()) {
            return;
        }

        var newVotes = toAdd.stream()
                .map(optId ->
                        new PollVote(poll, optionMap.get(optId), studentId, LocalDateTime.now()))
                .toList();
        try {
            pollVoteRepository.saveAll(newVotes);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("이미 처리된 요청입니다.", e);
        }
        log.info("Poll vote | pollId={}, studentId={}, add={}, del={}", pollId, studentId, toAdd, toDel);
    }

    // 투표 강제 마감 (생성자만)
    public PollSummaryResponse close(Long pollId, Long studentId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표"));

        if (!studentId.equals(poll.getCreatedBy())) {
            throw new IllegalArgumentException("투표 생성자만 마감 가능");
        }

        poll.close();
        pollRepository.save(poll);

        return PollSummaryResponse.from(poll);
    }

    // 어드민인지 확인
    private boolean isAdmin(Long studentId) {
        if (studentId == null) {
            return false;
        }
        boolean admin = studentRepository.existsByIdAndStudentRole(studentId, "ROLE_ADMIN");
        log.debug("Admin check | studentId={}, isAdmin={}", studentId, admin);
        return admin;
    }

    private long nanosToMs(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000L;
    }
}
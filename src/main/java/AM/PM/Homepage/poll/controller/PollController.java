package AM.PM.Homepage.poll.controller;

import AM.PM.Homepage.poll.request.PollCreateRequest;
import AM.PM.Homepage.poll.request.PollSearchParam;
import AM.PM.Homepage.poll.request.PollVoteRequest;
import AM.PM.Homepage.poll.response.PollDetailResponse;
import AM.PM.Homepage.poll.response.PollResultResponse;
import AM.PM.Homepage.poll.response.PollSummaryResponse;
import AM.PM.Homepage.poll.service.PollService;
import AM.PM.Homepage.security.UserAuth;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/polls")
public class PollController {

    private final PollService pollService;

    // 투표 검색
    @GetMapping
    public ResponseEntity<Page<PollSummaryResponse>> searchPoll(
            @Valid @ModelAttribute PollSearchParam params,
            Pageable pageable
    ) {
        Page<PollSummaryResponse> response = pollService.searchPoll(params, pageable);
        return ResponseEntity.ok(response);
    }

    // 투표 상세 조회
    @GetMapping("/{pollId}")
    public ResponseEntity<PollDetailResponse> getPoll(
            @PathVariable Long pollId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long studentId = userAuth == null ? null : userAuth.getId();
        PollDetailResponse response = pollService.getPollDetail(pollId, studentId);
        return ResponseEntity.ok(response);
    }

    // 투표 결과 집계
    @GetMapping("/{pollId}/results")
    public ResponseEntity<PollResultResponse> getPollResults(
            @PathVariable Long pollId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long studentId = userAuth == null ? null : userAuth.getId();
        PollResultResponse response = pollService.getPollResult(pollId, studentId);
        return ResponseEntity.ok(response);
    }

    // 투표 생성
    @PostMapping
    public ResponseEntity<PollSummaryResponse> createPoll(
            @Valid @RequestBody PollCreateRequest request,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        PollSummaryResponse response = pollService.create(request, userAuth.getId());
        return ResponseEntity.created(URI.create("/api/polls/" + response.getId())).body(response);
    }

    // 투표
    @PostMapping("/{pollId}/vote")
    public ResponseEntity<Void> vote(
            @Valid @RequestBody PollVoteRequest request,
            @PathVariable Long pollId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        pollService.vote(request, pollId, userAuth.getId());
        return ResponseEntity.noContent().build();
    }

    // 투표 강제 마감 (생성자만)
    @PostMapping("/{pollId}/close")
    public ResponseEntity<PollSummaryResponse> closePoll(
            @PathVariable Long pollId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        PollSummaryResponse response = pollService.close(pollId, userAuth.getId());
        return ResponseEntity.ok(response);
    }

    // 투표 삭제
    @DeleteMapping("/{pollId}")
    public ResponseEntity<Void> deletePoll(
            @PathVariable Long pollId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long studentId = userAuth == null ? null : userAuth.getId();
        pollService.delete(pollId, studentId);
        return ResponseEntity.noContent().build();
    }
}

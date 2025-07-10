package AM.PM.Homepage.notice.controller;

import AM.PM.Homepage.notice.request.NoticeCreateRequest;
import AM.PM.Homepage.notice.request.NoticeUpdateRequest;
import AM.PM.Homepage.notice.response.NoticeDetailResponse;
import AM.PM.Homepage.notice.response.NoticeSummaryResponse;
import AM.PM.Homepage.notice.service.NoticeService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDetailResponse> getNotice(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNotice(id));
    }

    // 목록 조회 (페이지네이션)
    @GetMapping
    public ResponseEntity<Page<NoticeSummaryResponse>> getNotices(Pageable pageable) {
        return ResponseEntity.ok(noticeService.getNotices(pageable));
    }

    // 생성
    @PostMapping
    public ResponseEntity<NoticeSummaryResponse> createNotice(
            @Valid @RequestBody NoticeCreateRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        NoticeSummaryResponse response = noticeService.createNotice(request);

        URI uri = uriBuilder.path("/api/notices/{id}").buildAndExpand(response.getId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<NoticeSummaryResponse> updateNotice(
            @PathVariable Long id,
            @Valid @RequestBody NoticeUpdateRequest request
    ) {
        return ResponseEntity.ok(noticeService.updateNotice(id, request));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}

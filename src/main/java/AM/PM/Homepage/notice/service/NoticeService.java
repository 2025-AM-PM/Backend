package AM.PM.Homepage.notice.service;

import AM.PM.Homepage.notice.entity.Notice;
import AM.PM.Homepage.notice.repository.NoticeRepository;
import AM.PM.Homepage.notice.request.NoticeCreateRequest;
import AM.PM.Homepage.notice.request.NoticeUpdateRequest;
import AM.PM.Homepage.notice.response.NoticeDetailResponse;
import AM.PM.Homepage.notice.response.NoticeSummaryResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public NoticeDetailResponse getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지를 찾을 수 없습니다."));

        // 조회수 증가
        notice.increaseViews();
        return NoticeDetailResponse.from(notice);
    }

    @Transactional(readOnly = true)
    public Page<NoticeSummaryResponse> getNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable).map(NoticeSummaryResponse::from);
    }

    @Transactional
    public NoticeSummaryResponse createNotice(NoticeCreateRequest request) {
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .noticeType(request.getNoticeType())
                .build();

        Notice saved = noticeRepository.save(notice);
        return NoticeSummaryResponse.from(saved);
    }

    @Transactional
    public NoticeSummaryResponse updateNotice(Long id, NoticeUpdateRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지를 찾을 수 없습니다."));

        notice.update(request.getTitle(), request.getContent(), request.getNoticeType());

        return NoticeSummaryResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지를 찾을 수 없습니다."));

        noticeRepository.delete(notice);
    }
}

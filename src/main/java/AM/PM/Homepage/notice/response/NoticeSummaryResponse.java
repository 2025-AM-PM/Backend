package AM.PM.Homepage.notice.response;

import AM.PM.Homepage.notice.entity.Notice;
import AM.PM.Homepage.notice.entity.NoticeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeSummaryResponse {

    private Long id;
    private String title;
    private NoticeType noticeType;
    private Integer views;
    private LocalDateTime createdAt;

    public static NoticeSummaryResponse from(Notice notice) {
        return NoticeSummaryResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .noticeType(notice.getNoticeType())
                .views(notice.getViews())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}

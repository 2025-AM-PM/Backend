package AM.PM.Homepage.notice.response;

import AM.PM.Homepage.notice.entity.Notice;
import AM.PM.Homepage.notice.entity.NoticeType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoticeDetailResponse {

    private Long id;
    private String title;
    private String content;
    private NoticeType noticeType;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoticeDetailResponse from(Notice notice) {
        return NoticeDetailResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .noticeType(notice.getNoticeType())
                .views(notice.getViews())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
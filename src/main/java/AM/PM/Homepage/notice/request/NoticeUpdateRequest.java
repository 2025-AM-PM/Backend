package AM.PM.Homepage.notice.request;

import AM.PM.Homepage.notice.entity.NoticeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeUpdateRequest {

    private String title;
    private String content;
    private NoticeType noticeType;
    private String url;
}

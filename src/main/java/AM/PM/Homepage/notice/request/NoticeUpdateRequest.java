package AM.PM.Homepage.notice.request;

import AM.PM.Homepage.util.constant.NoticeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeUpdateRequest {

    private String title;
    private String content;
    private NoticeType noticeType;
}

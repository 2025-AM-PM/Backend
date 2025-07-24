package AM.PM.Homepage.notice.request;

import AM.PM.Homepage.notice.entity.NoticeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeCreateRequest {

    @NotBlank
    private String title;

    private String content;

    @NotNull
    private NoticeType noticeType;

    private String url;
}

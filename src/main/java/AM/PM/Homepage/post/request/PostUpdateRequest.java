package AM.PM.Homepage.post.request;

import AM.PM.Homepage.post.domain.PostCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PostUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "설명은 필수입니다.")
    private String content;

    @NotBlank(message = "카테고리 필수입니다.")
    private PostCategory category;
}

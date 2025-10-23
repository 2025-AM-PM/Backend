package AM.PM.Homepage.post.response;

import AM.PM.Homepage.post.domain.Post;
import AM.PM.Homepage.post.domain.PostCategory;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostSummaryResponse {

    private Long id;
    private String title;
    private PostCategory category;
    private Long likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getLikes(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getCreatedBy(),
                post.getUpdatedBy()
        );
    }
}

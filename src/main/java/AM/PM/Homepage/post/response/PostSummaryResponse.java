package AM.PM.Homepage.post.response;

import AM.PM.Homepage.post.domain.Post;
import AM.PM.Homepage.post.domain.PostCategory;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PostSummaryResponse {

    private Long id;
    private String title;
    private PostCategory category;
    private Long likes;
    private Long views;
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
                post.getViews(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getCreatedBy(),
                post.getUpdatedBy()
        );
    }

    @QueryProjection
    public PostSummaryResponse(Long id, String title, PostCategory category, Long likes, Long views,
                               LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, String updatedBy) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.likes = likes;
        this.views = views;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}

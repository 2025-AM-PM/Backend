package AM.PM.Homepage.post.response;

import AM.PM.Homepage.post.domain.Post;
import AM.PM.Homepage.post.domain.PostCategory;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostDetailResponse {

    private Long id;
    private String title;
    private String content;
    private PostCategory category;
    private Long likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getLikes(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getCreatedBy(),
                post.getUpdatedBy()
        );
    }
}

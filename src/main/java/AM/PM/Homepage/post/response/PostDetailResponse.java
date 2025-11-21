package AM.PM.Homepage.post.response;

import AM.PM.Homepage.member.student.response.StudentResponse;
import AM.PM.Homepage.post.domain.PostCategory;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PostDetailResponse {

    private Long id;
    private String title;
    private String content;
    private PostCategory category;
    private Long likes;
    private Long views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private StudentResponse createBy;
    private StudentResponse updatedBy;

    @QueryProjection

    public PostDetailResponse(Long id, String title, String content, PostCategory category,
                              Long likes, Long views, LocalDateTime createdAt, LocalDateTime updatedAt,
                              StudentResponse createBy, StudentResponse updatedBy) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.likes = likes;
        this.views = views;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createBy = createBy;
        this.updatedBy = updatedBy;
    }
}

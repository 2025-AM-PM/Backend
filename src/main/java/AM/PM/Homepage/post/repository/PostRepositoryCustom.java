package AM.PM.Homepage.post.repository;

import AM.PM.Homepage.post.domain.PostCategory;
import AM.PM.Homepage.post.response.PostDetailResponse;
import AM.PM.Homepage.post.response.PostSummaryResponse;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

    Page<PostSummaryResponse> search(String title, PostCategory createdBy, Pageable pageable);

    Optional<PostDetailResponse> findByIdWithStudent(Long postId);
}

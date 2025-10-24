package AM.PM.Homepage.post.repository;

import AM.PM.Homepage.post.response.PostSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

    Page<PostSummaryResponse> search(String title, String createdBy, Pageable pageable);
}

package AM.PM.Homepage.post.repository;

import AM.PM.Homepage.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsByIdAndStudent_Id(Long exhibitId, Long studentId);
}

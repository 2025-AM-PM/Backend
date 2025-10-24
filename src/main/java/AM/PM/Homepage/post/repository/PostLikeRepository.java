package AM.PM.Homepage.post.repository;

import AM.PM.Homepage.post.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndStudentId(Long postId, Long studentId);

    void deleteByPostIdAndStudentId(Long postId, Long studentId);

    long countByPostId(Long postId);
}

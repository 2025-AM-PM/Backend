package AM.PM.Homepage.post.repository;

import AM.PM.Homepage.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    boolean existsByIdAndStudent_Id(Long postId, Long studentId);

    @Modifying
    @Query("update Post p set p.likes = p.likes + :delta where p.id = :postId")
    int addLikeCount(@Param("postId") Long postId, @Param("delta") long delta);

    @Modifying
    @Query("update Post p set p.views = p.views + 1 where p.id = :postId")
    int increaseView(@Param("postId") Long postId);
}

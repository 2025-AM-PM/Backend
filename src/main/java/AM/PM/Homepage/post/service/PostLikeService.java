package AM.PM.Homepage.post.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.post.domain.Post;
import AM.PM.Homepage.post.domain.PostLike;
import AM.PM.Homepage.post.repository.PostLikeRepository;
import AM.PM.Homepage.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public void toggleLike(Long postId, Long studentId) {
        boolean exists = postLikeRepository.existsByPostIdAndStudentId(postId, studentId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST, "postId=" + postId));

        if (exists) {
            postLikeRepository.deleteByPostIdAndStudentId(postId, studentId);
            post.likesCountDown();
        } else {
            PostLike like = new PostLike(post, new Student(studentId));
            postLikeRepository.save(like);
            post.likesCountUp();
        }
    }
}

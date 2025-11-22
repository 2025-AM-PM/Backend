package AM.PM.Homepage.post.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.post.domain.Post;
import AM.PM.Homepage.post.domain.PostLike;
import AM.PM.Homepage.post.repository.PostLikeRepository;
import AM.PM.Homepage.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public void toggleLike(Long postId, Long studentId) {
        log.info("게시글 좋아요 토글 시작: 게시글 ID={}, 학생 ID={}", postId, studentId);

        boolean exists = postLikeRepository.existsByPostIdAndStudentId(postId, studentId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST, "postId=" + postId));

        if (exists) {
            postLikeRepository.deleteByPostIdAndStudentId(postId, studentId);
            post.likesCountDown();
            log.info("게시글 좋아요 취소 완료: 게시글 ID={}, 학생 ID={}", postId, studentId);
        } else {
            PostLike like = new PostLike(post, new Student(studentId));
            postLikeRepository.save(like);
            post.likesCountUp();
            log.info("게시글 좋아요 추가 완료: 게시글 ID={}, 학생 ID={}", postId, studentId);
        }
    }
}

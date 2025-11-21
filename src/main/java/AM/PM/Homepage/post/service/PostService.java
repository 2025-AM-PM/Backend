package AM.PM.Homepage.post.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.post.domain.Post;
import AM.PM.Homepage.post.repository.PostRepository;
import AM.PM.Homepage.post.request.PostCreateRequest;
import AM.PM.Homepage.post.request.PostUpdateRequest;
import AM.PM.Homepage.post.response.PostDetailResponse;
import AM.PM.Homepage.post.response.PostSummaryResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final StudentRepository studentRepository;

    public Page<PostSummaryResponse> searchPost(String title, String createdBy, Pageable pageable) {
        return postRepository.search(title, createdBy, pageable);
    }

    public PostDetailResponse getPost(Long postId) {
        return postRepository.findByIdWithStudent(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
    }

    public PostSummaryResponse createPost(PostCreateRequest request, Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT, "studentId=" + studentId));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .student(student)
                .build();

        Post saved = postRepository.save(post);

        return PostSummaryResponse.from(saved);
    }

    public PostSummaryResponse updatePost(Long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST, "postId=" + postId));

        post.update(request);
        postRepository.save(post);

        return PostSummaryResponse.from(post);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST, "postId=" + postId));
        postRepository.delete(post);
    }
}

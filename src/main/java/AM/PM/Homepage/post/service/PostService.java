package AM.PM.Homepage.post.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.post.domain.Post;
import AM.PM.Homepage.post.domain.PostCategory;
import AM.PM.Homepage.post.repository.PostRepository;
import AM.PM.Homepage.post.request.PostCreateRequest;
import AM.PM.Homepage.post.request.PostUpdateRequest;
import AM.PM.Homepage.post.response.PostDetailResponse;
import AM.PM.Homepage.post.response.PostSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final StudentRepository studentRepository;

    public Page<PostSummaryResponse> searchPost(String title, PostCategory category, Pageable pageable) {
        log.info("게시글 검색 시작: 제목={}, 작성자={}, 페이지={}, 크기={}",
                title, category, pageable.getPageNumber(), pageable.getPageSize());

        Page<PostSummaryResponse> result = postRepository.search(title, category, pageable);

        log.info("게시글 검색 완료: 조회된 게시글={}개, 전체={}개, 전체 페이지={}",
                result.getNumberOfElements(), result.getTotalElements(), result.getTotalPages());
        return result;
    }

    public PostDetailResponse getPost(Long postId) {
        log.info("게시글 조회 시작: 게시글 ID={}", postId);

        PostDetailResponse response = postRepository.findByIdWithStudent(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

        log.info("게시글 조회 완료: 게시글 ID={}, 제목={}", postId, response.getTitle());
        return response;
    }

    public PostSummaryResponse createPost(PostCreateRequest request, Long studentId) {
        log.info("게시글 생성 시작: 학생 ID={}, 제목={}, 카테고리={}",
                studentId, request.getTitle(), request.getCategory());

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT, "studentId=" + studentId));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .student(student)
                .build();

        Post saved = postRepository.save(post);

        log.info("게시글 생성 완료: 게시글 ID={}, 학생 ID={}, 제목={}",
                saved.getId(), studentId, saved.getTitle());
        return PostSummaryResponse.from(saved);
    }

    public PostSummaryResponse updatePost(Long postId, PostUpdateRequest request) {
        log.info("게시글 수정 시작: 게시글 ID={}, 새 제목={}", postId, request.getTitle());

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST, "postId=" + postId));

        post.update(request);
        postRepository.save(post);

        log.info("게시글 수정 완료: 게시글 ID={}, 제목={}", postId, post.getTitle());
        return PostSummaryResponse.from(post);
    }

    public void deletePost(Long postId) {
        log.info("게시글 삭제 시작: 게시글 ID={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST, "postId=" + postId));

        postRepository.delete(post);
        log.info("게시글 삭제 완료: 게시글 ID={}", postId);
    }
}


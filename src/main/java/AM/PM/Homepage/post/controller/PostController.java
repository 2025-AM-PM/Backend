package AM.PM.Homepage.post.controller;

import AM.PM.Homepage.post.request.PostCreateRequest;
import AM.PM.Homepage.post.request.PostUpdateRequest;
import AM.PM.Homepage.post.response.PostDetailResponse;
import AM.PM.Homepage.post.response.PostSummaryResponse;
import AM.PM.Homepage.post.service.PostService;
import AM.PM.Homepage.security.UserAuth;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 페이지네이션으로 게시글 가져오기
    @GetMapping
    public ResponseEntity<Page<PostSummaryResponse>> getPosts(Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsByPage(pageable));
    }

    // 특정 게시글 id로 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPost(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    // TODO 구현: 검색
    @GetMapping("/search")
    public ResponseEntity<List<PostSummaryResponse>> search(

    ) {
        return ResponseEntity.ok(postService.search());
    }

    // 게시글 작성. 로그인 한 유저만
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostSummaryResponse> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        PostSummaryResponse response = postService.createPost(request, userAuth.getId());
        return ResponseEntity.created(URI.create("/api/posts/" + response.getId())).body(response);
    }

    // TODO 구현: 좋아요 클릭. 로그인 한 유저만

    // 게시글 수정. 본인 or 관리자만
    @PutMapping("/{postId}")
    @PreAuthorize("@postAuthz.isOwner(#postId, authentication) or hasAnyRole('SYSTEM_ADMIN')")
    public ResponseEntity<PostSummaryResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        return ResponseEntity.ok(postService.updatePost(postId, request));
    }

    // 게시글 삭제. 본인 or 관리자만
    @DeleteMapping("/{postId}")
    @PreAuthorize("@postAuthz.isOwner(#postId, authentication) or hasAnyRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId
    ) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}

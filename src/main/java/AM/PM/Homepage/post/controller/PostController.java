package AM.PM.Homepage.post.controller;

import AM.PM.Homepage.post.domain.PostCategory;
import AM.PM.Homepage.post.request.PostCreateRequest;
import AM.PM.Homepage.post.request.PostUpdateRequest;
import AM.PM.Homepage.post.response.PostDetailResponse;
import AM.PM.Homepage.post.response.PostSummaryResponse;
import AM.PM.Homepage.post.service.PostLikeService;
import AM.PM.Homepage.post.service.PostService;
import AM.PM.Homepage.security.UserAuth;
import jakarta.validation.Valid;
import java.net.URI;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;

    // 게시글 검색 페이지네이션
    @GetMapping
    public ResponseEntity<Page<PostSummaryResponse>> search(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "category", required = false) PostCategory category,
            Pageable pageable
    ) {
        return ResponseEntity.ok(postService.searchPost(title, category, pageable));
    }

    // 특정 게시글 id로 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPost(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(postService.getPost(postId));
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

    @PostMapping("/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        postLikeService.toggleLike(postId, userAuth.getId());
        return ResponseEntity.noContent().build();
    }

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

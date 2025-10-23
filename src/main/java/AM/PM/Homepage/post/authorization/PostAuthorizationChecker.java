package AM.PM.Homepage.post.authorization;

import AM.PM.Homepage.post.repository.PostRepository;
import AM.PM.Homepage.security.UserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("postAuthz")
@RequiredArgsConstructor
public class PostAuthorizationChecker {

    private final PostRepository postRepository;

    // post 작성자인지 확인
    public boolean isOwner(Long postId, Authentication authentication) {
        UserAuth userAuth = (UserAuth) authentication;
        Long studentId = userAuth.getId();
        if (studentId == null || postId == null) {
            return false;
        }

        return postRepository.existsByIdAndStudent_Id(postId, studentId);
    }
}

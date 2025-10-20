package AM.PM.Homepage.member.refreshtoken.controller;

import AM.PM.Homepage.member.refreshtoken.service.RefreshTokenService;
import AM.PM.Homepage.security.UserAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/reissue")
    public ResponseEntity<Void> reissueRefreshToken(
            @AuthenticationPrincipal UserAuth userAuth,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        refreshTokenService.reissuedAccessToken(userAuth.getId(), request, response);
        return ResponseEntity.ok().build();
    }
}

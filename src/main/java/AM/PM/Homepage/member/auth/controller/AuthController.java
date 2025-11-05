package AM.PM.Homepage.member.auth.controller;

import AM.PM.Homepage.member.auth.request.LoginRequest;
import AM.PM.Homepage.member.auth.response.LoginSuccessResponse;
import AM.PM.Homepage.member.auth.response.ReissueResult;
import AM.PM.Homepage.member.auth.service.AuthService;
import AM.PM.Homepage.member.auth.service.RefreshTokenService;
import AM.PM.Homepage.member.student.request.StudentSignupRequest;
import AM.PM.Homepage.security.UserAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REFRESH_COOKIE = "refresh";

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    // 회원가입 신청
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid StudentSignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 로그인 (AT/RT 발급 + RT 저장)
    @PostMapping("/login")
    public ResponseEntity<LoginSuccessResponse> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId
    ) {
        return authService.login(request, deviceId);
    }

    // 로그아웃(단일 디바이스): AT 블랙리스트 + 디바이스 RT 폐기 + RT 쿠키 삭제
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserAuth principal,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId
    ) {
        final Long studentId = (principal != null) ? principal.getId() : null;
        final String accessToken = extractBearer(authorization);

        refreshTokenService.logout(studentId, deviceId, accessToken);

        ResponseCookie delete = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
//                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header("Set-Cookie", delete.toString())
                .build();
    }

    // 전체 로그아웃: AT 블랙리스트 + 모든 RT 폐기 + RT 쿠키 삭제
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal UserAuth principal,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        final Long studentId = (principal != null) ? principal.getId() : null;
        final String accessToken = extractBearer(authorization);

        refreshTokenService.logoutAll(studentId, accessToken);

        ResponseCookie delete = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(0).build();

        return ResponseEntity.noContent()
                .header("Set-Cookie", delete.toString())
                .build();
    }

    // access 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        ReissueResult reissueResult = refreshTokenService.reissue(request, response);
        return ResponseEntity.noContent()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reissueResult.getAccessToken())
                .header("X-Device-Id", reissueResult.getDeviceId())
                .header(HttpHeaders.SET_COOKIE, reissueResult.getRefreshCookie().toString())
                .build();
    }

    private String extractBearer(String authorization) {
        if (authorization == null) {
            return null;
        }
        return authorization.startsWith(BEARER_PREFIX) ? authorization.substring(BEARER_PREFIX.length()) : null;
    }
}

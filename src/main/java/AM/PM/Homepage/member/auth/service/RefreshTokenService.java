package AM.PM.Homepage.member.auth.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.common.redis.AuthRedisStore;
import AM.PM.Homepage.member.auth.response.ReissueResult;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.security.jwt.JwtUtil;
import AM.PM.Homepage.util.CookieProvider;
import AM.PM.Homepage.util.constant.JwtTokenType;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private static final String REFRESH_COOKIE = "refresh";

    private final StudentRepository studentRepository;
    private final CookieProvider provider;
    private final JwtUtil jwtUtil;
    private final AuthRedisStore store;

    // 로그인 직후/회전 직후 RT 해시 등록
    @Transactional
    public void registerRefreshToken(long studentId, String deviceId, String refreshToken) {
        String hash = AuthRedisStore.sha256Base64(refreshToken);
        store.saveRefresh(studentId, deviceId, hash, jwtUtil.getRefreshTtl());
    }

    /**
     * 액세스 재발급(회전)
     * - RT(쿠키)만으로 처리
     * - 저장된 (studentId, deviceId) 해시와 일치해야 함
     * - 회전: 기존 RT 삭제 → 새 RT 저장
     */
    @Transactional
    public ReissueResult reissue(HttpServletRequest request, HttpServletResponse response) {
        log.info("[리프레시 토큰 재발급 요청] 쿠키에서 리프레시 토큰 추출 시작");
        String refreshToken = extractRefreshToken(request.getCookies());

        // 만료/카테고리 체크
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }

        String category = jwtUtil.getCategory(refreshToken);
        if (!JwtTokenType.REFRESH_TOKEN.getValue().equals(category)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_CATEGORY);
        }

        // 식별 정보
        Long studentId = jwtUtil.getId(refreshToken);
        String deviceId = jwtUtil.getDeviceId(refreshToken);
        if (studentId == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        if (deviceId == null || deviceId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 저장된 RT 해시 비교(재사용/탈취 방지)
        String storedHash = store.getRefreshHash(studentId, deviceId);
        String givenHash = AuthRedisStore.sha256Base64(refreshToken);
        if (storedHash == null || !storedHash.equals(givenHash)) {
            // 정책: 전량 폐기
            store.deleteAllRefresh(studentId);
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));

        // 새 페어 발급
        String newAccess = jwtUtil.generateAccessToken(studentId, student.getStudentNumber(), student.getRole());
        String newRefresh = jwtUtil.generateRefreshToken(studentId, student.getStudentNumber(), student.getRole(), deviceId);
        log.info("[리프레시 토큰 재발급 성공] 새 토큰 생성 완료: studentId={}, deviceId={}", studentId, deviceId);

        // 회전
        store.deleteRefresh(studentId, deviceId);
        registerRefreshToken(studentId, deviceId, newRefresh);
        log.info("[리프레시 토큰 재발급 성공] 기존 리프레시 토큰 삭제 및 신규 해시 저장 완료: studentId={}, deviceId={}", studentId, deviceId);

        // 응답 세팅
        ResponseCookie cookie = buildRefreshCookie(newRefresh, false, "Lax");
        log.info("[리프레시 토큰 재발급 완료] studentId={}, deviceId={}", studentId, deviceId);
        return new ReissueResult(newAccess, cookie, deviceId);
    }

    private ResponseCookie buildRefreshCookie(String refreshToken, boolean secure, String sameSite) {
        return ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(jwtUtil.getRefreshTtl())
                .build();
    }

    private String extractRefreshToken(Cookie[] cookies) {
        if (cookies == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }
        for (Cookie c : cookies) {
            if (REFRESH_COOKIE.equals(c.getName())) {
                if (c.getValue() == null || c.getValue().isBlank()) {
                    throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
                }
                log.info("[리프레시 토큰 추출 성공] refresh 쿠키 발견");
                return c.getValue();
            }
        }
        throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
    }

    // 단일 디바이스 로그아웃
    @Transactional
    public ResponseCookie logout(Long studentId, String deviceId, String accessToken) {
        log.info("[로그아웃 요청] studentId={}, deviceId={}", studentId, deviceId);
        // AT 블랙리스트(남은 TTL)
        if (accessToken != null && !accessToken.isBlank()) {
            jwtUtil.blacklistAccess(accessToken);
            log.info("[로그아웃] access 블랙리스트 등록 완료: jti={}", jwtUtil.getJti(accessToken));
        }
        // RT 폐기
        if (studentId != null && deviceId != null && !deviceId.isBlank()) {
            store.deleteRefresh(studentId, deviceId);
            log.info("[로그아웃] refresh 삭제: studentId={}, deviceId={}", studentId, deviceId);
        }
        log.info("[로그아웃] 완료: studentId={}, deviceId={}", studentId, deviceId);
        return clearRefreshCookie(false, "Lax");
    }

    // 전체 로그아웃
    @Transactional
    public ResponseCookie logoutAll(Long studentId, String accessToken) {
        log.info("[전체 로그아웃 요청] studentId={}", studentId);
        if (accessToken != null && !accessToken.isBlank()) {
            jwtUtil.blacklistAccess(accessToken);
            String jti = jwtUtil.getJti(accessToken);
            log.info("[전체 로그아웃] access 블랙리스트 등록 완료: jti={}", jti);
        }
        if (studentId != null) {
            store.deleteAllRefresh(studentId);
            log.info("[전체 로그아웃] refresh 삭제: studentId={}", studentId);
        }
        log.info("[전체 로그아웃] 완료: studentId={}", studentId);
        return clearRefreshCookie(false, "Lax");
    }

    private ResponseCookie clearRefreshCookie(boolean secure, String sameSite) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(REFRESH_COOKIE, "");
        b.httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0);
        return b.build();
    }
}

package AM.PM.Homepage.member.signupapplication.controller;

import AM.PM.Homepage.member.signupapplication.request.SignupApplicationRequest;
import AM.PM.Homepage.member.signupapplication.response.SignupApplicationResponse;
import AM.PM.Homepage.member.signupapplication.service.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 어드민 전용 Signup Controller
 * 회장, 부회장, 시스템 관리자만 사용 가능
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/signup")
public class SignupAdminController {

    private final SignupService signupService;

    // 회원가입 신청 목록 조회

    // 회원가입 신청 (일괄) 승인
    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SYSTEM_ADMIN')")
    public ResponseEntity<SignupApplicationResponse> approveSignup(
            @RequestBody SignupApplicationRequest request
    ) {
        SignupApplicationResponse response = signupService.approveSignup(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SYSTEM_ADMIN')")
    public ResponseEntity<SignupApplicationResponse> rejectSignup(
            @RequestBody SignupApplicationRequest request
    ) {
        SignupApplicationResponse response = signupService.rejectSignup(request);
        return ResponseEntity.ok().body(response);
    }
}

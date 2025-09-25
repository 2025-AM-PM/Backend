package AM.PM.Homepage.member.signupapplication.controller;

import AM.PM.Homepage.member.signupapplication.domain.SignupApplicationStatus;
import AM.PM.Homepage.member.signupapplication.request.SignupApplicationRequest;
import AM.PM.Homepage.member.signupapplication.response.SignupApplicationProcessResponse;
import AM.PM.Homepage.member.signupapplication.response.SignupApplicationResponse;
import AM.PM.Homepage.member.signupapplication.service.SignupService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/signup")
public class SignupAdminController {

    private final SignupService signupService;

    // 회원가입 신청 목록 조회
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'PRESIDENT', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<SignupApplicationResponse>> getSignupApplications(
            @RequestParam SignupApplicationStatus status
    ) {
        List<SignupApplicationResponse> response = signupService.getSignupApplications(status);
        return ResponseEntity.ok(response);
    }

    // 회원가입 신청 (일괄) 승인
    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SYSTEM_ADMIN')")
    public ResponseEntity<SignupApplicationProcessResponse> approveSignup(
            @RequestBody SignupApplicationRequest request
    ) {
        SignupApplicationProcessResponse response = signupService.approveSignup(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SYSTEM_ADMIN')")
    public ResponseEntity<SignupApplicationProcessResponse> rejectSignup(
            @RequestBody SignupApplicationRequest request
    ) {
        SignupApplicationProcessResponse response = signupService.rejectSignup(request);
        return ResponseEntity.ok().body(response);
    }
}

package AM.PM.Homepage.member.signupapplication.controller;

import AM.PM.Homepage.member.signupapplication.service.SignupService;
import AM.PM.Homepage.member.student.request.StudentSignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/signup")
public class SignupController {

    private final SignupService signupService;

    // 회원가입 신청
    @PostMapping
    public ResponseEntity<Void> signup(
            @RequestBody StudentSignupRequest request
    ) {
        signupService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
package AM.PM.Homepage.member.student.controller;

import AM.PM.Homepage.member.algorithmprofile.request.VerificationCodeRequest;
import AM.PM.Homepage.member.student.request.PasswordChangeRequest;
import AM.PM.Homepage.member.student.response.LoginSuccessResponse;
import AM.PM.Homepage.member.student.response.StudentInformationResponse;
import AM.PM.Homepage.member.student.response.StudentResponse;
import AM.PM.Homepage.member.student.service.StudentService;
import AM.PM.Homepage.security.UserAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/mypage")
    public ResponseEntity<StudentResponse> showMyPage(@AuthenticationPrincipal UserAuth userAuth) {
        return ResponseEntity.ok(studentService.showStudentInformation(userAuth.getId()));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginSuccessResponse> loadStudentInfo(@AuthenticationPrincipal UserAuth auth) {
        return ResponseEntity.ok(studentService.loadStudentInfo(auth.getId()));
    }

    @PostMapping("/modify/password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest,
                                               @AuthenticationPrincipal UserAuth userAuth) {
        studentService.changeStudentPassword(userAuth.getId(), passwordChangeRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/issue")
    public ResponseEntity<String> issueVerificationCode(@AuthenticationPrincipal UserAuth userAuth) {
        String verificationCode = studentService.issueVerificationCode(userAuth.getId());
        return ResponseEntity.ok(verificationCode);
    }

    @PostMapping("/info")
    public ResponseEntity<StudentInformationResponse> showStudentInformation(
            @RequestBody VerificationCodeRequest verificationCodeRequest,
            @AuthenticationPrincipal UserAuth userAuth) {
        if (!studentService.verificationStudentCode(userAuth.getId(), verificationCodeRequest)) {
            return ResponseEntity.badRequest().build();
        }
        StudentInformationResponse studentInformationResponse
                = studentService.linkAlgorithmProfileToStudent(userAuth.getId(),
                verificationCodeRequest.getSolvedAcNickname());

        return ResponseEntity.ok(studentInformationResponse);
    }

    @PostMapping("/info/test")
    public ResponseEntity<StudentInformationResponse> showStudentInformationForTest(
            @RequestBody VerificationCodeRequest request,
            @AuthenticationPrincipal UserAuth userAuth) {

        return ResponseEntity.ok(
                studentService.showStudentInformationForTest(request.getSolvedAcNickname(), userAuth.getUsername()));
    }
}

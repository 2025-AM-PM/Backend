package AM.PM.Homepage.member.student.controller;

import AM.PM.Homepage.member.student.request.PasswordChangeRequest;
import AM.PM.Homepage.member.student.request.StudentSignupRequest;
import AM.PM.Homepage.member.student.request.VerificationCodeRequest;
import AM.PM.Homepage.member.student.response.StudentInformationResponse;
import AM.PM.Homepage.member.student.service.StudentService;
import AM.PM.Homepage.security.UserAuth;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/")
@Slf4j
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;


    @GetMapping("intro")
    public ResponseEntity<?> showMainPage() {
        return null;
    }

    @PostMapping("signup")
    public ResponseEntity<Void> signup(
            @RequestBody StudentSignupRequest request
    ) {
        Long studentId = studentService.signup(request);

        return ResponseEntity.created(URI.create("/api/student/" + studentId)).build();
    }

    @PostMapping("modify/password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest,
                                               @AuthenticationPrincipal UserAuth userAuth) {

        if (!studentService.checkPasswordMatch(userAuth.getPassword(), passwordChangeRequest)) {
            return ResponseEntity.badRequest().build();
        }

        studentService.changeStudentPassword(userAuth.getId(), passwordChangeRequest.getNewPassword());

        return ResponseEntity.ok().build();
    }


    @PostMapping("issue")
    public ResponseEntity<String> issueVerificationCode(@AuthenticationPrincipal UserAuth userAuth) {
        String verificationCode = studentService.issueVerificationCode(userAuth.getId());
        return ResponseEntity.ok(verificationCode);
    }

    @PostMapping("info")
    public ResponseEntity<StudentInformationResponse> showStudentInformation(
            @RequestBody VerificationCodeRequest verificationCodeRequest,
            @AuthenticationPrincipal UserAuth userAuth) {

        if (studentService.verificationStudentCode(userAuth.getId(), verificationCodeRequest)) {
            return ResponseEntity.badRequest().build();
        }
        ;

        StudentInformationResponse studentInformationResponse
                = studentService.linkAlgorithmProfileToStudent(userAuth.getId(), userAuth.getUsername());

        return ResponseEntity.ok(studentInformationResponse);
    }

    @PostMapping("info/test")
    public ResponseEntity<StudentInformationResponse> showStudentInformationForTest(
            @RequestBody VerificationCodeRequest request,
            @AuthenticationPrincipal UserAuth userAuth) {

        return ResponseEntity.ok(
                studentService.showStudentInformationForTest(request.getSolvedAcNickname(), userAuth.getUsername()));
    }

}

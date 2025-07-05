package AM.PM.Homepage.member.student.controller;

import AM.PM.Homepage.member.student.response.StudentInformationResponse;
import AM.PM.Homepage.member.student.service.AlgorithmProfileService;
import AM.PM.Homepage.member.student.service.StudentService;
import AM.PM.Homepage.security.UserAuth;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final AlgorithmProfileService algorithmProfileService;


    @GetMapping("info")
    public ResponseEntity<StudentInformationResponse> showStudentInformation(@RequestBody String solvedAcNickname,
                                                                             @AuthenticationPrincipal UserAuth userAuth) {

        StudentInformationResponse studentInformationResponse
                = studentService.showStudentInformation(solvedAcNickname, userAuth.getUsername());

        return ResponseEntity.ok(studentInformationResponse);
    }


}

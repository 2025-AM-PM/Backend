package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.request.VerificationCodeRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @Test
    @DisplayName("사용자가 프로필 소개란에 입력한 토큰값과 유저의 토큰값을 비교하여 검증한다.")
    void varification_users_token() {

        VerificationCodeRequest verificationCodeRequest = new VerificationCodeRequest();
        verificationCodeRequest.setStudentName("imnotyourocean");

        assertTrue(studentService.verificationStudentCode(12L, verificationCodeRequest));
    }

/*    @Test
    @DisplayName("solvedAc 데이터가 사용자에게 잘 들어갔는지 확인")
    void check_solvedAc_data_to_student() {
        Student student = studentService.linkAlgorithmProfileToStudent(12L, "imnotyourocean");
        assertNotNull(student.getBaekjoonTier());
    }*/
}
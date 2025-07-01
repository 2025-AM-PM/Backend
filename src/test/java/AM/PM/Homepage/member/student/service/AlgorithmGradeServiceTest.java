package AM.PM.Homepage.member.student.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
class AlgorithmGradeServiceTest {

    @Autowired
    private AlgorithmProfileService service;


    @Test
    @DisplayName("Solved.ac API를 사용하여 사용자의 정보를 불러온다.")
    void useSolvedAcApi_For_GetUserInformation() {

        String username = "imnotyourocean";

        var solvedAcMono = service.fetchSolvedAcInformation(username);

        assertThat(solvedAcMono.getSolvedCount()).isEqualTo(6);
        assertThat(solvedAcMono.getTier()).isEqualTo(1);
        assertThat(solvedAcMono.getRating()).isEqualTo(46);

    }

    @Test
    @DisplayName("Solved.ac API를 사용하여 사용자 인증을 위해 자기소개란의 토큰을 받아온다.")
    void useSolvedAcApi_For_GetUserBio() {

        String username = "imnotyourocean";

        var solvedAcMono = service.fetchSolvedBio(username);

        // 일단은 null값
        assertThat(solvedAcMono.getVerificationToken()).isNull();

        // 추후 추가 예정
//        assertThat(solvedAcMono.getVerificationToken()).isEqualTo("token value");

    }

}
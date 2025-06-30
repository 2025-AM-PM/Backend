package AM.PM.Homepage.member.student.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;


@SpringBootTest
class AlgorithmGradeServiceTest {

    @Autowired
    private AlgorithmGradeService service;


    @Test
    @DisplayName("Solved.ac API를 사용하여 사용자의 정보를 불러온다.")
    void useSolvedAcApi_For_GetUserInformation() {

        String username = "imnotyourocean";

        var solvedAcMono = service.getSolvedAcInformation(username);

        // StepVerifier를 사용하여 테스트 진행
        StepVerifier.create(solvedAcMono)
                .assertNext(solved -> {
                    assertThat(solved.getSolvedCount()).isEqualTo(6);
                    assertThat(solved.getTier()).isEqualTo(1);
                    assertThat(solved.getRating()).isEqualTo(46);
                })
                .verifyComplete();
    }

}
package AM.PM.Homepage.member.algorithmprofile.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.algorithmprofile.domain.AlgorithmProfile;
import AM.PM.Homepage.member.algorithmprofile.repository.AlgorithmGradeRepository;
import AM.PM.Homepage.member.algorithmprofile.request.VerificationCodeRequest;
import AM.PM.Homepage.member.algorithmprofile.response.AlgorithmProfileResponse;
import AM.PM.Homepage.member.algorithmprofile.response.SolvedAcInformationResponse;
import AM.PM.Homepage.member.algorithmprofile.response.VerificationCodeResponse;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.response.StudentInformationResponse;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AlgorithmProfileService {

    private static final String SOLVED_AC_URL = "https://solved.ac/api/v3";
    private static final String SOLVED_AC_URI = "/user/show";
    private static final String SOLVED_AC_QUERY_PARAM = "handle";

    private final StudentRepository studentRepository;
    private final AlgorithmGradeRepository algorithmGradeRepository;

    private final RestClient restClient = RestClient.builder()
            .baseUrl(SOLVED_AC_URL)
            .build();

    public SolvedAcInformationResponse fetchSolvedAcInformation(String solvedAcNickname) {
        return performApiRequest(solvedAcNickname, SolvedAcInformationResponse.class);
    }

    public VerificationCodeResponse fetchSolvedBio(String solvedAcNickname) {
        return performApiRequest(solvedAcNickname, VerificationCodeResponse.class);
    }

    public void registerAlgorithmGrade(AlgorithmProfile algorithmProfile) {
        algorithmGradeRepository.save(algorithmProfile);
    }

    private <T> T performApiRequest(String solvedAcNickname, Class<T> classes) {
        return this.restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(SOLVED_AC_URI)
                        .queryParam(SOLVED_AC_QUERY_PARAM, solvedAcNickname)
                        .build())
                .retrieve()
                .body(classes);
    }


    @Transactional(readOnly = true)
    public boolean verificationStudentCode(Long studentId, VerificationCodeRequest request) {
        log.info("[Solved.ac 인증코드 검증] studentId={}, nickname={}", studentId, request.getSolvedAcNickname());
        VerificationCodeResponse solved = fetchSolvedBio(request.getSolvedAcNickname());
        boolean result = Objects.equals(issueVerificationCode(studentId), solved.getBio());
        log.info("[Solved.ac 인증코드 결과] studentId={}, result={}", studentId, result);
        return result;
    }

    @Transactional(readOnly = true)
    public String issueVerificationCode(Long studentId) {
        // 저장된 코드가 없을 수 있으므로 null 허용 (검증 로직에서 처리)
        return studentRepository.findVerificationCodeById(studentId);
    }

    @Transactional(readOnly = true)
    public StudentInformationResponse showStudentInformationForTest(String solvedAcNickname, String studentNumber) {
        log.info("[학생/솔브드 테스트 조회] nickname={}, studentNumber={}", solvedAcNickname, studentNumber);
        SolvedAcInformationResponse solved = fetchSolvedAcInformation(solvedAcNickname);
        return StudentInformationResponse.builder()
                .studentNumber(studentNumber)
                .solvedAcInformationResponse(solved)
                .build();
    }

    public StudentInformationResponse linkAlgorithmProfileToStudent(Long studentId, String solvedAcNickname) {
        log.info("[솔브드 연동 요청] studentId={}, nickname={}", studentId, solvedAcNickname);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STUDENT));

        SolvedAcInformationResponse solved = fetchSolvedAcInformation(solvedAcNickname);
        AlgorithmProfile algorithmProfile = AlgorithmProfile.from(solved);

        registerAlgorithmGrade(algorithmProfile);
        student.linkAlgorithmProfile(algorithmProfile);

        log.info("[솔브드 연동 완료] studentId={}, tier={}", studentId, solved.getTier());
        return StudentInformationResponse.builder()
                .studentNumber(student.getStudentNumber())
                .solvedAcInformationResponse(solved)
                .build();
    }

    // solved.ac 인증된 학생 중, 최고 티어 10명 가져오기
    public List<AlgorithmProfileResponse> getTopTiers() {
        return algorithmGradeRepository.findTopTier(10);
    }
}

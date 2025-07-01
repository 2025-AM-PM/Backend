package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.domain.AlgorithmProfile;
import AM.PM.Homepage.member.student.repository.AlgorithmGradeRepository;
import AM.PM.Homepage.member.student.request.VerificationCodeRequest;
import AM.PM.Homepage.member.student.response.SolvedAcResponse;
import AM.PM.Homepage.member.student.response.VerificationTokenResponse;
import AM.PM.Homepage.util.VerificationTokenGenerator;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AlgorithmProfileService {

    private final RestClient restClient;
    private final AlgorithmGradeRepository algorithmGradeRepository;
    private final VerificationTokenGenerator generator;
    private final static String SOLVED_AC_URL = "https://solved.ac/api/v3";
    private final static String SOLVED_AC_URI = "/user/show";
    private final static String SOLVED_AC_QUERY_PARAM = "handle";


    public AlgorithmProfileService(AlgorithmGradeRepository algorithmGradeRepository, VerificationTokenGenerator generator) {
        this.algorithmGradeRepository = algorithmGradeRepository;
        this.generator = generator;
        this.restClient = RestClient.builder()
                .baseUrl(SOLVED_AC_URL)
                .build();
    }

    public boolean confirmVerification(VerificationCodeRequest verificationCodeRequest) {
        return generator.issuedVerificationToken().equals(verificationCodeRequest.getVerificationCode());
    }

    public SolvedAcResponse fetchSolvedAcInformation(String solvedAcNickname) {
        return performApiRequest(solvedAcNickname, SolvedAcResponse.class);
    }

    public VerificationTokenResponse fetchSolvedBio(String solvedAcNickname) {
        return performApiRequest(solvedAcNickname, VerificationTokenResponse.class);
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




}

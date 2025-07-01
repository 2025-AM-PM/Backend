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
    private final static String SOLVED_AC_URL = "https://solved.ac/api/v3";
    private final static String SOLVED_AC_URI = "/user/show";
    private final static String SOLVED_AC_QUERY_PARAM = "handle";


    public AlgorithmProfileService(AlgorithmGradeRepository algorithmGradeRepository) {
        this.algorithmGradeRepository = algorithmGradeRepository;
        this.restClient = RestClient.builder()
                .baseUrl(SOLVED_AC_URL)
                .build();
    }

    public boolean confirmVerification(VerificationCodeRequest verificationCodeRequest) {
        return VerificationTokenGenerator.issuedVerificationToken().equals(verificationCodeRequest.getVerificationCode());
    }

    public SolvedAcResponse fetchSolvedAcInformation(String username) {
        return performApiRequest(username, SolvedAcResponse.class);
    }

    public VerificationTokenResponse fetchSolvedBio(String username) {
        return performApiRequest(username, VerificationTokenResponse.class);
    }

    public SolvedAcResponse registerAlgorithmGrade(String username) {

        SolvedAcResponse solvedAcInformation = fetchSolvedAcInformation(username);
        AlgorithmProfile algorithmGrade = AlgorithmProfile.builder()
                .solvedCount(solvedAcInformation.getSolvedCount())
                .rating(solvedAcInformation.getRating())
                .tier(solvedAcInformation.getTier())
                .build();
        algorithmGradeRepository.save(algorithmGrade);

        return solvedAcInformation;

    }

    private <T> T performApiRequest(String username, Class<T> classes) {
        return this.restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(SOLVED_AC_URI)
                        .queryParam(SOLVED_AC_QUERY_PARAM, username)
                        .build())
                .retrieve()
                .body(classes);
    }




}

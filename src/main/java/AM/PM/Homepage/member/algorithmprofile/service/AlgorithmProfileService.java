package AM.PM.Homepage.member.algorithmprofile.service;

import AM.PM.Homepage.member.algorithmprofile.domain.AlgorithmProfile;
import AM.PM.Homepage.member.algorithmprofile.repository.AlgorithmGradeRepository;
import AM.PM.Homepage.member.algorithmprofile.response.SolvedAcInformationResponse;
import AM.PM.Homepage.member.algorithmprofile.response.VerificationCodeResponse;
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




}

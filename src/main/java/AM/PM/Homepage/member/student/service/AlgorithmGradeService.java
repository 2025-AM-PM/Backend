package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.response.SolvedAcResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AlgorithmGradeService {

    private final WebClient webClient;
    private final static String SOLVED_AC_URL = "https://solved.ac/api/v3";
    private final static String SOLVED_AC_URI = "/user/show";
    private final static String SOLVED_AC_QUERY_PARAM = "handle";


    public AlgorithmGradeService() {
        this.webClient = WebClient.builder()
                .baseUrl(SOLVED_AC_URL)
                .build();
    }

    public Mono<SolvedAcResponse> getSolvedAcInformation(String username) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(SOLVED_AC_URI)
                        .queryParam(SOLVED_AC_QUERY_PARAM, username) // "?handle={username}" 부분을 이렇게 추가
                        .build())
                .retrieve()
                .bodyToMono(SolvedAcResponse.class);

    }


}

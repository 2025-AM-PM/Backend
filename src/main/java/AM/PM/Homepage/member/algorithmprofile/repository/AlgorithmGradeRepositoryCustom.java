package AM.PM.Homepage.member.algorithmprofile.repository;

import AM.PM.Homepage.member.algorithmprofile.response.AlgorithmProfileResponse;
import java.util.List;

public interface AlgorithmGradeRepositoryCustom {

    List<AlgorithmProfileResponse> findTopTier(int count);
}

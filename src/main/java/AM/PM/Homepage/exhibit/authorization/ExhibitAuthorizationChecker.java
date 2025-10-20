package AM.PM.Homepage.exhibit.authorization;

import AM.PM.Homepage.exhibit.repository.ExhibitRepository;
import AM.PM.Homepage.security.UserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("exhibitAuthz")
@RequiredArgsConstructor
public class ExhibitAuthorizationChecker {

    private final ExhibitRepository exhibitRepository;

    // 해당 exhibit의 소유자인지 검증
    public boolean isOwner(Long exhibitId, UserAuth userAuth) {
        Long studentId = userAuth.getId();
        if (studentId == null) {
            return false;
        }

        return exhibitRepository.existsByIdAndStudent_Id(exhibitId, studentId);
    }
}

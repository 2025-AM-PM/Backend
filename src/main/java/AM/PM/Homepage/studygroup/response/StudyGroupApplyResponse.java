package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.studygroup.entity.StudyGroupApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudyGroupApplyResponse {

    private Long id;

    public static StudyGroupApplyResponse from(StudyGroupApplication application) {
        return new StudyGroupApplyResponse(application.getId());
    }
}

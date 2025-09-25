package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.studygroup.entity.StudyGroupApplicationStatus;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApplicationApproveResponse {

    private Long id;
    private StudyGroupApplicationStatus status;

    public static ApplicationApproveResponse from(StudyGroupApplication application) {
        return new ApplicationApproveResponse(application.getId(), application.getStatus());
    }
}

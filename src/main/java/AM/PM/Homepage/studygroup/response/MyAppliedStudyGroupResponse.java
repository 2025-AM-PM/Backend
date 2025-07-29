package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.studygroup.entity.ApplicationStatus;
import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyAppliedStudyGroupResponse {
    private Long studyGroupId;
    private String title;
    private String description;
    private ApplicationStatus status;
    private String leaderName;

    public static MyAppliedStudyGroupResponse from(StudyGroupApplication application) {
        StudyGroup studyGroup = application.getStudyGroup();

        return new MyAppliedStudyGroupResponse(
                studyGroup.getId(),
                studyGroup.getTitle(),
                studyGroup.getDescription(),
                application.getStatus(),
                studyGroup.getLeaderName()
        );
    }
}

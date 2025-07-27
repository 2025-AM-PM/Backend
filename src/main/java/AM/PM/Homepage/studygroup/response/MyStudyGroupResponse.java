package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyStudyGroupResponse {

    private Long id;
    private String title;
    private String description;
    private int maxMember;
    private StudyGroupStatus status;
    private String leaderName;

    public static MyStudyGroupResponse from(StudyGroup studyGroup) {
        return new MyStudyGroupResponse(
                studyGroup.getId(),
                studyGroup.getTitle(),
                studyGroup.getDescription(),
                studyGroup.getMaxMember(),
                studyGroup.getStatus(),
                studyGroup.getLeaderName()
        );
    }
}

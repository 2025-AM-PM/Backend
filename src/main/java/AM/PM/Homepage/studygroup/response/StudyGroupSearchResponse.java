package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudyGroupSearchResponse {

    private Long id;
    private String title;
    private String description;
    private int maxMember;
    private StudyGroupStatus status;
    private String leader;

    public static StudyGroupSearchResponse from(StudyGroup studyGroup, String leaderName) {
        return new StudyGroupSearchResponse(
                studyGroup.getId(),
                studyGroup.getTitle(),
                studyGroup.getDescription(),
                studyGroup.getMaxMember(),
                studyGroup.getStatus(),
                leaderName
        );
    }
}

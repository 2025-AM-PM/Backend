package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.studygroup.entity.StudyGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudyGroupUpdateResponse {

    private Long id;
    private String title;
    private String description;

    public static StudyGroupUpdateResponse from(StudyGroup studyGroup) {
        return new StudyGroupUpdateResponse(
                studyGroup.getId(),
                studyGroup.getTitle(),
                studyGroup.getDescription()
        );
    }
}

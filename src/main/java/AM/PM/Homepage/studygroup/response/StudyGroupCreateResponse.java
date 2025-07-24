package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.studygroup.entity.StudyGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudyGroupCreateResponse {

    private Long id;
    private String title;
    private String description;

    public static StudyGroupCreateResponse from(StudyGroup studyGroup) {
        return new StudyGroupCreateResponse(
                studyGroup.getId(),
                studyGroup.getTitle(),
                studyGroup.getDescription()
        );
    }
}

package AM.PM.Homepage.studygroup.request;

import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudyGroupUpdateRequest {

    private String title;
    private String description;
    private int maxMember;
    private StudyGroupStatus status;
}

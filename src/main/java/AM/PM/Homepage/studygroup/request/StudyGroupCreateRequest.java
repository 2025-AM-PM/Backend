package AM.PM.Homepage.studygroup.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudyGroupCreateRequest {

    private String title;
    private String description;
    private int maxMember;
}

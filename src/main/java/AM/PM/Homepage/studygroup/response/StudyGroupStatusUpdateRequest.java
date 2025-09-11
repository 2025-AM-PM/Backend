package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyGroupStatusUpdateRequest {

    private StudyGroupStatus status;
}

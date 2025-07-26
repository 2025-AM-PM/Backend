package AM.PM.Homepage.studygroup.response;

import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.entity.StudyGroupMember;
import AM.PM.Homepage.studygroup.entity.StudyGroupStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StudyGroupDetailResponse {

    private Long id;
    private String title;
    private String description;
    private int maxMember;
    private StudyGroupStatus status;
    private String leader;
    private List<String> members;

    public static StudyGroupDetailResponse from(StudyGroup studyGroup, List<StudyGroupMember> members) {
        return new StudyGroupDetailResponse(
                studyGroup.getId(),
                studyGroup.getTitle(),
                studyGroup.getDescription(),
                studyGroup.getMaxMember(),
                studyGroup.getStatus(),
                studyGroup.getLeader().getStudent().getStudentName(),
                members.stream()
                        .map(m -> m.getStudent().getStudentName())
                        .toList()
        );
    }
}

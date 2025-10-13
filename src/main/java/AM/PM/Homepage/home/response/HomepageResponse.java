package AM.PM.Homepage.home.response;

import AM.PM.Homepage.exhibit.entity.Exhibit;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import AM.PM.Homepage.notice.entity.Notice;
import AM.PM.Homepage.notice.response.NoticeSummaryResponse;
import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.response.StudyGroupSearchResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HomepageResponse {

    private List<ExhibitSummaryResponse> exhibits;
    private List<StudyGroupSearchResponse> studyGroups;
    private List<NoticeSummaryResponse> notices;

    public static HomepageResponse from(
            List<Exhibit> exhibits,
            List<StudyGroup> studyGroups,
            List<Notice> notices
    ) {
        return new HomepageResponse(
                exhibits.stream()
                        .map(ExhibitSummaryResponse::from)
                        .toList(),
                studyGroups.stream()
                        .map(StudyGroupSearchResponse::from)
                        .toList(),
                notices.stream()
                        .map(NoticeSummaryResponse::from)
                        .toList()
        );
    }
}

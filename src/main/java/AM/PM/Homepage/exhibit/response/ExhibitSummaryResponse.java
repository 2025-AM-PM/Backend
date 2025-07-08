package AM.PM.Homepage.exhibit.response;

import AM.PM.Homepage.exhibit.entity.Exhibit;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExhibitSummaryResponse {

    private Long id;
    private String studentName;
    private String title;
    private String exhibitUrl;
    private Integer likes;
    private String thumbnailUrl;
    private LocalDateTime createdAt;

    public static ExhibitSummaryResponse from(Exhibit exhibit) {
        return ExhibitSummaryResponse.builder()
                .id(exhibit.getId())
                .studentName(exhibit.getStudentName())
                .title(exhibit.getTitle())
                .exhibitUrl(exhibit.getExhibitUrl())
                .likes(exhibit.getLikes())
                .thumbnailUrl(exhibit.getThumbnailPath())
                .createdAt(exhibit.getCreatedAt())
                .build();
    }
}

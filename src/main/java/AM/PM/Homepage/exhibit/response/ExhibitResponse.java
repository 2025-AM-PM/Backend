package AM.PM.Homepage.exhibit.response;

import AM.PM.Homepage.exhibit.entity.Exhibit;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExhibitResponse {

    private Long id;
    private String studentName;
    private String studentNumber;
    private String title;
    private String description;
    private String exhibitUrl;
    private Integer likes;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExhibitResponse from(Exhibit exhibit) {
        return ExhibitResponse.builder()
                .id(exhibit.getId())
                .studentName(exhibit.getStudentName())
                .studentNumber(exhibit.getStudentNumber())
                .title(exhibit.getTitle())
                .description(exhibit.getDescription())
                .exhibitUrl(exhibit.getExhibitUrl())
                .likes(exhibit.getLikes())
                .imageUrls(exhibit.getAllImagePath())
                .createdAt(exhibit.getCreatedAt())
                .updatedAt(exhibit.getUpdatedAt())
                .build();
    }
}

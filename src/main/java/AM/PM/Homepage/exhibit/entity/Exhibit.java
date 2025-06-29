package AM.PM.Homepage.exhibit.entity;

import AM.PM.Homepage.config.base.BaseEntity;
import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "exhibits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exhibit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @Column(name = "title", nullable = false)
    private String title;

    @Getter
    @Column(name = "description", nullable = false)
    private String description;

    @Getter
    @Column(name = "exhibit_url")
    private String exhibitUrl;

    @Getter
    @Column(name = "github_url")
    private String githubUrl;

    @Getter
    @Column(name = "likes", nullable = false)
    @ColumnDefault("0")
    private Integer likes;

    @Column(name = "exhibit_images")
    @OneToMany(mappedBy = "exhibit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExhibitImage> images = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    public String getStudentName() {
        return student.getStudentName();
    }

    public String getStudentNumber() {
        return student.getStudentNumber();
    }

    public List<String> getImageUrls() {
        return images.stream()
                .map(ExhibitImage::getUploadImageUrl)
                .toList();
    }

    public String getThumbnailUrl() {
        return images.getFirst().getUploadImageUrl();
    }
}


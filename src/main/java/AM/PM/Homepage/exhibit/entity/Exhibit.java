package AM.PM.Homepage.exhibit.entity;

import AM.PM.Homepage.common.entity.BaseTimeEntity;
import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "exhibits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exhibit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "exhibit_url")
    private String exhibitUrl;

    @Column(name = "likes", nullable = false)
    @ColumnDefault("0")
    private Integer likes;

    @Column(name = "exhibit_images")
    @OneToMany(mappedBy = "exhibit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExhibitImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Builder
    public Exhibit(String title, String description, String exhibitUrl, Student student) {
        this.title = title;
        this.description = description;
        this.exhibitUrl = exhibitUrl;
        this.student = student;
        this.likes = 0;
    }

    public Long getStudentId() {
        return student.getId();
    }

    public String getStudentName() {
        return student.getStudentName();
    }

    public String getStudentNumber() {
        return student.getStudentNumber();
    }

    public List<String> getAllImagePath() {
        return images.stream()
                .map(ExhibitImage::getUploadImagePath)
                .toList();
    }

    public String getThumbnailPath() {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.getFirst().getUploadImagePath();
    }

    public void addImage(ExhibitImage image) {
        this.images.add(image);

        if (image.getExhibit() != this) {
            image.setExhibit(this);
        }
    }

    public void clearImages() {
        this.images.clear();
    }

    public void update(String title,
                       String description,
                       String exhibitUrl
    ) {
        this.title = title;
        this.description = description;
        this.exhibitUrl = exhibitUrl;
    }
}


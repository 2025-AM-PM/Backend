package AM.PM.Homepage.exhibit.entity;

import AM.PM.Homepage.common.entity.BaseTimeEntity;
import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Builder
    private Exhibit(String title, String description, String exhibitUrl, Student student) {
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

    public void update(String title,
                       String description,
                       String exhibitUrl
    ) {
        this.title = title;
        this.description = description;
        this.exhibitUrl = exhibitUrl;
    }
}


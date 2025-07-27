package AM.PM.Homepage.studygroup.entity;

import AM.PM.Homepage.common.entity.BaseEntity;
import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_group_applications")
public class StudyGroupApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Builder
    protected StudyGroupApplication(StudyGroup studyGroup, Student student, ApplicationStatus status) {
        this.studyGroup = studyGroup;
        this.student = student;
        this.status = status;
    }

    public void approve(){
        this.status = ApplicationStatus.APPROVED;
    }

    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }
}

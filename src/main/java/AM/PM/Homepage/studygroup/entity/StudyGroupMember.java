package AM.PM.Homepage.studygroup.entity;

import AM.PM.Homepage.common.entity.BaseTimeEntity;
import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_group_members")
public class StudyGroupMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn()
    private Student student;

    @Enumerated(EnumType.STRING)
    private StudyGroupRole role;

    @Builder
    public StudyGroupMember(StudyGroup studyGroup, Student student, StudyGroupRole role) {
        this.studyGroup = studyGroup;
        this.student = student;
        this.role = role;
    }
}

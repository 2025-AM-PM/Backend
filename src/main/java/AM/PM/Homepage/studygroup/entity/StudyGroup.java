package AM.PM.Homepage.studygroup.entity;

import AM.PM.Homepage.common.entity.BaseEntity;
import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_groups")
public class StudyGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ColumnDefault("10")
    @Column(nullable = false)
    private int maxMember;

    @Enumerated(EnumType.STRING)
    private StudyGroupStatus status;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private StudyGroupMember leader;

    @Builder
    protected StudyGroup(String title,
                      String description,
                      int maxMember,
                      StudyGroupStatus status,
                      StudyGroupMember leader
    ) {
        this.title = title;
        this.description = description;
        this.maxMember = maxMember;
        this.status = status;
        this.leader = leader;
    }

    public boolean isLeader(Student student) {
        return this.leader.getId().equals(student.getId());
    }
}

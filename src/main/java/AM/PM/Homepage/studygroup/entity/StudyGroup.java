package AM.PM.Homepage.studygroup.entity;

import AM.PM.Homepage.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

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

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyGroupApplication> applications;

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyGroupMember> members;

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

    public boolean isNotLeader(Long userId) {
        return !this.leader.getStudent().getId().equals(userId);
    }

    public void update(
            String title,
            String description,
            int maxMember,
            StudyGroupStatus status
    ) {
        this.title = title;
        this.description = description;
        this.maxMember = maxMember;
        this.status = status;
    }

    public String getLeaderName() {
        return this.leader.getStudent().getStudentName();
    }
}

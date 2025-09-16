package AM.PM.Homepage.poll.entity;

import AM.PM.Homepage.common.entity.BaseTimeEntity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 투표 항목
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "poll_options")
public class PollOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    // student id, 없으면 null
    @Column(name = "create_by")
    private Long createdBy;

    private PollOption(Poll poll, String label, Long createdBy) {
        this.poll = poll;
        this.label = label;
        this.createdBy = createdBy;
    }

    public static PollOption create(Poll poll, String label, Long createdBy) {
        return new PollOption(poll, label, createdBy);
    }
}

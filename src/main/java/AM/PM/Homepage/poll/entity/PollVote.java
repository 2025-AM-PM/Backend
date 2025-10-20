package AM.PM.Homepage.poll.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 투표권
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "poll_votes")
public class PollVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_option_id", nullable = false)
    private PollOption option;

    @Column(name = "voter_id", nullable = false, updatable = false)
    private Long voterId; // Student ID

    @Column(name = "voted_at", nullable = false, updatable = false)
    private LocalDateTime votedAt;

    public PollVote(Poll poll, PollOption option, Long voterId, LocalDateTime votedAt) {
        this.poll = poll;
        this.option = option;
        this.voterId = voterId;
        this.votedAt = votedAt;
    }
}

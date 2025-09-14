package AM.PM.Homepage.poll.entity;

import AM.PM.Homepage.common.entity.BaseTimeEntity;
import AM.PM.Homepage.poll.request.PollCreateRequest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 투표
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "polls")
public class Poll extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PollStatus status = PollStatus.OPEN;

    // 옵션
    // 최대 선택 개수 (기본 1개)
    @PositiveOrZero
    @Column(name = "max_select_opt", nullable = false)
    private int maxSelect = 1;

    // 다중 선택
    @Column(name = "multiple", nullable = false)
    private boolean multiple;

    // 익명 투표
    @Column(name = "anonymous", nullable = false)
    private boolean anonymous;

    // 투표 항목 추가 가능 여부
    @Column(name = "allow_add_opt", nullable = false)
    private boolean allowAddOption;

    // 재투표 가능 여부
    @Column(name = "allow_revote", nullable = false)
    private boolean allowRevote;

    @Column(name = "deadline_at", nullable = false)
    private LocalDateTime deadlineAt;

    // Student id
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PollOption> options = new ArrayList<>();

    private Poll(String title, String description, PollStatus status, int maxSelect, boolean multiple, boolean anonymous,
                boolean allowAddOption, boolean allowRevote, LocalDateTime deadlineAt, Long createdBy) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.maxSelect = maxSelect;
        this.multiple = multiple;
        this.anonymous = anonymous;
        this.allowAddOption = allowAddOption;
        this.allowRevote = allowRevote;
        this.deadlineAt = deadlineAt;
        this.createdBy = createdBy;
    }

    public static Poll createFrom(PollCreateRequest request, Long userId) {
        return new Poll(
                request.getTitle(),
                request.getDescription(),
                PollStatus.OPEN,
                request.getMaxSelect(),
                request.isMultiple(),
                request.isAnonymous(),
                request.isAllowAddOption(),
                request.isAllowRevote(),
                request.getDeadlineAt(),
                userId
        );
    }

    public void addOption(PollOption option) {
        this.options.add(option);
        option.setPoll(this);
    }

    public boolean isOpen() {
        boolean checkDeadline = LocalDateTime.now().isBefore(deadlineAt);
        return checkDeadline && status == PollStatus.OPEN;
    }

    public void close() {
        this.status = PollStatus.CLOSED;
    }
}

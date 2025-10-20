package AM.PM.Homepage.poll.response;

import AM.PM.Homepage.poll.entity.PollResultVisibility;
import AM.PM.Homepage.poll.entity.PollStatus;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class PollDetailResponse {

    private Long id;
    private String title;
    private String description;
    private PollStatus status;
    private int maxSelect;
    private boolean multiple;
    private boolean anonymous;
    private boolean allowRevote;
    private PollResultVisibility resultVisibility;
    private LocalDateTime deadlineAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private boolean open;                       // 현재 투표 가능한지

    private List<PollOptionResponse> options;

    private boolean voted;                      // 내가 투표했는지
    private Set<Long> mySelectedOptionIds;      // 내가 선택한 옵션

    @QueryProjection
    public PollDetailResponse(Long id, String title, String description, PollStatus status, int maxSelect,
                              boolean multiple, boolean anonymous, boolean allowRevote,
                              PollResultVisibility resultVisibility, LocalDateTime deadlineAt, Long createdBy,
                              LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime closedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.maxSelect = maxSelect;
        this.multiple = multiple;
        this.anonymous = anonymous;
        this.allowRevote = allowRevote;
        this.resultVisibility = resultVisibility;
        this.deadlineAt = deadlineAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.closedAt = closedAt;
        this.open = isOpen(status, deadlineAt);
    }

    private static boolean isOpen(PollStatus status, LocalDateTime deadlineAt) {
        return status == PollStatus.OPEN
               && deadlineAt != null
               && deadlineAt.isAfter(LocalDateTime.now());
    }
}
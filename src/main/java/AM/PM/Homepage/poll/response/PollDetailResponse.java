package AM.PM.Homepage.poll.response;

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
    private boolean allowAddOption;
    private boolean allowRevote;

    private LocalDateTime deadlineAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private long totalVotes;
    private List<PollOptionResponse> options;
    private Set<Long> mySelectedOptionIds;

    public static PollDetailResponse of(
            PollDetailResponse detail,
            long totalVotes,
            List<PollOptionResponse> options,
            Set<Long> mySelectedOptionIds
    ) {
        detail.setTotalVotes(totalVotes);
        detail.setOptions(options);
        detail.setMySelectedOptionIds(mySelectedOptionIds);
        return detail;
    }

    @QueryProjection
    public PollDetailResponse(Long id, String title, String description, PollStatus status, int maxSelect,
                              boolean multiple, boolean anonymous, boolean allowAddOption, boolean allowRevote,
                              LocalDateTime deadlineAt, Long createdBy, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this.id = id;
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
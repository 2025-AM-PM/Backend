package AM.PM.Homepage.poll.response;

import AM.PM.Homepage.poll.entity.Poll;
import AM.PM.Homepage.poll.entity.PollStatus;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PollSummaryResponse {

    private final Long id;
    private final String title;
    private final PollStatus status;
    private final Integer optionCount;
    private final Integer maxSelect;
    private final Boolean multiple;
    private final Boolean anonymous;
    private final Boolean allowAddOption;
    private final Boolean allowRevote;
    private final LocalDateTime deadlineAt;
    private final Long createdBy;
    private final LocalDateTime createdAt;

    public static PollSummaryResponse from(Poll poll) {
        return new PollSummaryResponse(
                poll.getId(),
                poll.getTitle(),
                poll.getStatus(),
                poll.getOptions().size(),
                poll.getMaxSelect(),
                poll.isMultiple(),
                poll.isAnonymous(),
                poll.isAllowAddOption(),
                poll.isAllowRevote(),
                poll.getDeadlineAt(),
                poll.getCreatedBy(),
                poll.getCreatedAt()
        );
    }

    @QueryProjection
    public PollSummaryResponse(Long id,
                               String title,
                               PollStatus status,
                               Integer optionCount,
                               Integer maxSelect,
                               Boolean multiple,
                               Boolean anonymous,
                               Boolean allowAddOption,
                               Boolean allowRevote,
                               LocalDateTime deadlineAt,
                               Long createdBy,
                               LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.optionCount = optionCount;
        this.maxSelect = maxSelect;
        this.multiple = multiple;
        this.anonymous = anonymous;
        this.allowAddOption = allowAddOption;
        this.allowRevote = allowRevote;
        this.deadlineAt = deadlineAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
}

package AM.PM.Homepage.poll.request;

import AM.PM.Homepage.poll.entity.PollResultVisibility;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PollCreateRequest {

    @NotBlank(message = "제목은 필수")
    @Size(max = 200, message = "최대 200자")
    private String title;

    @Size(max = 2000, message = "최대 2000자")
    private String description;

    @Min(value = 1, message = "최소 1 이상")
    @Max(value = 10, message = "최대 10까지")
    private int maxSelect = 1;

    private boolean multiple;

    private boolean anonymous;

    private boolean allowRevote = true;

    private PollResultVisibility resultVisibility;

    @NotNull(message = "마감 시각은 필수")
    @Future(message = "마감 시각은 현재 이후여야 함")
    private LocalDateTime deadlineAt;

    @NotNull(message = "옵션 목록은 필수")
    @Size(min = 1, max = 50, message = "옵션은 1~50개")
    private List<@NotBlank(message = "옵션은 비어있을 수 없음")
    @Size(max = 200, message = "옵션은 최대 200자") String> options;
}

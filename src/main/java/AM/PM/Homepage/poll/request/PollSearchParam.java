package AM.PM.Homepage.poll.request;

import AM.PM.Homepage.poll.entity.PollStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class PollSearchParam {

    @Size(max = 100, message = "검색어는 최대 100자")
    private String query;

    private PollStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime deadlineFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime deadlineTo;
}

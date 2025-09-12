package AM.PM.Homepage.member.student.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Setter
@Getter
public class AuthenticationRequest {

    @NotNull
    @Pattern(regexp = "^[0-9]{9}$")
    private String studentNumber;

    @Min(6) @Max(25)
    private String studentPassword;

}

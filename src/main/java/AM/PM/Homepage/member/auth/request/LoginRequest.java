package AM.PM.Homepage.member.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotNull
    @NotBlank
    @NotEmpty
    private String studentNumber;

    @NotNull
    @NotBlank
    @NotEmpty
    private String studentPassword;
}

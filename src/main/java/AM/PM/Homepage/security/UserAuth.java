package AM.PM.Homepage.security;

import AM.PM.Homepage.member.student.domain.StudentRole;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
public class UserAuth implements UserDetails {

    private final Long id;
    private final String studentNumber;
    private final String encodedPassword;
    private final String studentName;
    private final StudentRole role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getAuthority()));
    }

    // 비밀번호 사용 안 함
    @Override
    public String getPassword() {
        return encodedPassword;
    }

    @Override
    public String getUsername() {
        return studentNumber;
    }
}
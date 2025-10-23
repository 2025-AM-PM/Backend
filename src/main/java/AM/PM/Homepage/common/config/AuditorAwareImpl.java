package AM.PM.Homepage.common.config;

import AM.PM.Homepage.security.UserAuth;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        UserAuth userAuth = (UserAuth) authentication.getPrincipal();
        return Optional.of(userAuth.getUsername());
    }
}
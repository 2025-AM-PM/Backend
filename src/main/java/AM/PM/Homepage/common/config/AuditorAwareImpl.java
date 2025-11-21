package AM.PM.Homepage.common.config;

import AM.PM.Homepage.security.UserAuth;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditorAwareImpl implements AuditorAware<Long> {

    @SuppressWarnings("NullableProblems")
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserAuth userAuth)) {
            return Optional.empty();
        }

        return Optional.ofNullable(userAuth.getId());
    }
}

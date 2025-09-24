package AM.PM.Homepage.security.config;

import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.security.UserAuth;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authz")
public class Authz {

    // 임원 이상: MANAGER, PRESIDENT, SYSTEM_ADMIN
    public boolean isStaff(Authentication auth) {
        StudentRole role = extractRole(auth);
        return role != null && role.isStaff();
    }

    // 관리자 이상: PRESIDENT, SYSTEM_ADMIN
    public boolean isAdmin(Authentication auth) {
        StudentRole role = extractRole(auth);
        return role != null && role.isAdmin();
    }

    private StudentRole extractRole(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        Object p = auth.getPrincipal();
        if (p instanceof UserAuth ua) return ua.getRole();
        return null;
    }
}
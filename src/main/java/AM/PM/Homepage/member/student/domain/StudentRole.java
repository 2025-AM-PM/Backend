package AM.PM.Homepage.member.student.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StudentRole {

    USER,               // 일반
    MANAGER,            // 동아리 임원
    PRESIDENT,          // 회장, 부회장
    SYSTEM_ADMIN;       // 시스템 관리자

    public String getTitle() {
        return "ROLE_" + this.name();
    }

    public boolean isStaff() {
        return this == MANAGER || this == PRESIDENT || this == SYSTEM_ADMIN;
    }

    public boolean isAdmin() {
        return this == PRESIDENT || this == SYSTEM_ADMIN;
    }
}

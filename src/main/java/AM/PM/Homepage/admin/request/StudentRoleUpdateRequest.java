package AM.PM.Homepage.admin.request;

import AM.PM.Homepage.member.student.domain.StudentRole;
import lombok.Data;

@Data
public class StudentRoleUpdateRequest {

    private StudentRole newRole;
}

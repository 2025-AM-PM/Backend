package AM.PM.Homepage.member.signupapplication.domain;

import AM.PM.Homepage.common.entity.BaseTimeEntity;
import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "signup_application")
public class SignupApplication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", nullable = false)
    private String studentNumber;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "student_password", nullable = false)
    private String studentPassword;

    @Enumerated(EnumType.STRING)
    private SignupApplicationStatus status = SignupApplicationStatus.PENDING;

    @Builder
    private SignupApplication(String studentNumber, String studentName, String studentPassword) {
        this.studentNumber = studentNumber;
        this.studentName = studentName;
        this.studentPassword = studentPassword;
    }

    public void approve() {
        this.status = SignupApplicationStatus.APPROVED;
    }

    public void reject() {
        this.status = SignupApplicationStatus.REJECTED;
    }
}

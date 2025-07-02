package AM.PM.Homepage.member.student.domain;

import AM.PM.Homepage.member.student.response.StudentResponse;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", unique = true)
//    @Pattern(regexp = "^[0-9]{8}$")
    private String studentNumber;

    @Column(name = "student_role", nullable = false)
    private String studentRole;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_password")
    private String password;

    @Column(name = "solved_ac_token")
    private String verificationToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "algorithm_profile_id")
    private AlgorithmProfile baekjoonTier;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refresh_token_id")
    private RefreshToken refreshToken;

    public Student(StudentResponse response) {
        this.studentNumber = response.getStudentNumber();
        this.studentRole = "ROLE_USER";
        this.studentName = response.getStudentName();
        this.password = response.getPhoneNumber();
        this.verificationToken = issuedVerificationToken();
    }

    public void linkAlgorithmProfile(AlgorithmProfile profile) {
        this.baekjoonTier = profile;
        profile.linkStudent(this);
    }

    public static List<Student> from(List<StudentResponse> response) {

        return response.stream()
                .map(Student::new)
                .toList();
    }
    public String issuedVerificationToken() {
        return UUID.randomUUID().toString();
    }


}
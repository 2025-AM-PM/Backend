package AM.PM.Homepage.member.student.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

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
    @Pattern(regexp = "^[0-9]{6}$")
    private String studentNumber;

    @Column(name = "student_role", nullable = false)
    private String studentRole;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_password")
    private String password;

    @OneToOne
    @JoinColumn(name = "baekjoon_tier_id")
    private AlgorithmProfile baekjoonTier;

    @OneToOne
    @JoinColumn(name = "verification_code_id")
    private VerificationToken token;

}

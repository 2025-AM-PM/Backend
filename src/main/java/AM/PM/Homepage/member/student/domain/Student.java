package AM.PM.Homepage.member.student.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
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
    private AlgorithmGrade baekjoonTier;

}

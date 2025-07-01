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

    @OneToOne(mappedBy = "student", orphanRemoval = true, cascade = CascadeType.ALL)
    private AlgorithmProfile baekjoonTier;

    @OneToOne(mappedBy = "student", orphanRemoval = true, cascade = CascadeType.ALL)
    private VerificationToken token;

    public void linkAlgorithmProfile(AlgorithmProfile profile) {
        this.baekjoonTier = profile;
        profile.linkStudent(this); // 양방향 연관관계 설정을 위해 상대방에게도 자신을 알려준다.
    }
}

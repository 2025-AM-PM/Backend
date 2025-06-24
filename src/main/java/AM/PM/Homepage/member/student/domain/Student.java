package AM.PM.Homepage.member.student.domain;

import AM.PM.Homepage.util.constant.StudentRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", unique = true)
    @Min(9) @Max(9)
    private Integer studentNumber;

    @Column(name = "student_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private StudentRole studentRole;

    @Column(name = "student_name")
    private String studentName;

    @OneToOne
    @JoinColumn(name = "baekjoon_tier_id")
    private BaekjoonTier baekjoonTier;

}

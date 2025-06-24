package AM.PM.Homepage.member.student.domain;

import jakarta.persistence.*;

@Entity
public class BaekjoonTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "backjoon_tier")
    private String tier;

    @Column(name = "backjoon_tier_number")
    private Integer tierNumber;

    @OneToOne(mappedBy = "baekjoonTier")
    private Student student;

}

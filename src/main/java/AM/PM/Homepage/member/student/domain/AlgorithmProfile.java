package AM.PM.Homepage.member.student.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class AlgorithmProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "backjoon_tier")
    private Integer tier;

    @Column(name = "backjoon_solved_count")
    private Integer solvedCount;

    @Column(name = "backjoon_rating")
    private Integer rating;


    @Builder
    public AlgorithmProfile(Integer rating, Integer solvedCount, Integer tier) {
        this.rating = rating;
        this.solvedCount = solvedCount;
        this.tier = tier;
    }
}

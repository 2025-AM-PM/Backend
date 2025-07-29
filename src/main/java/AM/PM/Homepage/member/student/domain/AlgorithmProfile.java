package AM.PM.Homepage.member.student.domain;

import AM.PM.Homepage.member.student.response.SolvedAcInformationResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AlgorithmProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "backjoon_tier")
    private Integer tier;

    @Column(name = "backjoon_solved_count")
    private Integer solvedCount;

    @Column(name = "baekjoon_rating")
    private Integer rating;

    @OneToOne(mappedBy = "baekjoonTier", orphanRemoval = true, cascade = CascadeType.ALL)
    private Student student;


    public static AlgorithmProfile from(SolvedAcInformationResponse solvedAcInformationResponse) {
        return AlgorithmProfile.builder()
                .solvedCount(solvedAcInformationResponse.getSolvedCount())
                .rating(solvedAcInformationResponse.getRating())
                .tier(solvedAcInformationResponse.getTier())
                .build();
    }

    public void linkStudent(Student student) {
        this.student = student;
    }
}
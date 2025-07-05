package AM.PM.Homepage.member.student.domain;

import AM.PM.Homepage.member.student.response.StudentInformationResponse;
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


    public static AlgorithmProfile from(StudentInformationResponse solvedAcInformation) {
        return AlgorithmProfile.builder()
                .solvedCount(solvedAcInformation.getSolvedCount())
                .rating(solvedAcInformation.getRating())
                .tier(solvedAcInformation.getTier())
                .build();
    }

    public void linkStudent(Student student) {
        this.student = student;
    }
}
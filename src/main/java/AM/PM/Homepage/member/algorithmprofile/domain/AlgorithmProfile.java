package AM.PM.Homepage.member.algorithmprofile.domain;

import AM.PM.Homepage.member.algorithmprofile.response.SolvedAcInformationResponse;
import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
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
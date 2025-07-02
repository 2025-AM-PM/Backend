package AM.PM.Homepage.member.student.domain;

import AM.PM.Homepage.member.student.response.SolvedAcResponse;
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

    @MapsId
    @OneToOne(mappedBy = "baekjoonTier", cascade = CascadeType.ALL, orphanRemoval = true)
    private Student student;


    public static AlgorithmProfile from(SolvedAcResponse solvedAcInformation, Student student) {
        return AlgorithmProfile.builder()
                .solvedCount(solvedAcInformation.getSolvedCount())
                .rating(solvedAcInformation.getRating())
                .tier(solvedAcInformation.getTier())
                .student(student)
                .build();
    }

    public void linkStudent(Student student) {
        this.student = student;
    }
}
package AM.PM.Homepage.project.boast.domain;

import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ProjectBoast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "created_at")
    private LocalDateTime createBoard;

    @Lob
    @Column(name = "project_boast_explain")
    private String boastExplain;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

}


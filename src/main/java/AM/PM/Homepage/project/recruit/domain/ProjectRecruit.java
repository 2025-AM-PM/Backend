package AM.PM.Homepage.project.recruit.domain;

import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ProjectRecruit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "recruit_start_date")
    private LocalDateTime startDate;

    @Column(name = "recruit_end_date")
    private LocalDateTime endDate;

    @Lob
    @Column(name = "project_explain")
    private String projectExplain;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

}


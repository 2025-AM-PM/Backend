package AM.PM.Homepage.project.recruit.domain;

import AM.PM.Homepage.member.student.domain.Student;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ProjectApplicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "project_recruit_id")
    private ProjectRecruit projectRecruit;

    @Column(name = "applied_at")
    private LocalDateTime appliedProject;

}

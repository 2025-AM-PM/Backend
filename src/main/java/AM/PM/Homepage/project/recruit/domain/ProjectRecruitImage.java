package AM.PM.Homepage.project.recruit.domain;

import jakarta.persistence.*;

@Entity
public class ProjectRecruitImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_image_url")
    private String uploadImageUrl;

    @Column(name = "upload_image_name")
    private String uploadImageName;

    @ManyToOne
    @JoinColumn(name = "project_recrutit_id")
    public ProjectRecruit projectBoast;


}
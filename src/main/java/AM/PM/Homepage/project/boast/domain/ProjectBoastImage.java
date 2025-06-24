package AM.PM.Homepage.project.boast.domain;

import jakarta.persistence.*;

@Entity
public class ProjectBoastImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_image_url")
    private String uploadImageUrl;

    @Column(name = "upload_image_name")
    private String uploadImageName;

    @ManyToOne
    @JoinColumn(name = "project_boast_id")
    public ProjectBoast projectBoast;


}
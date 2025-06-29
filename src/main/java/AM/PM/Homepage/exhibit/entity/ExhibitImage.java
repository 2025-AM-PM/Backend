package AM.PM.Homepage.exhibit.entity;

import AM.PM.Homepage.config.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class ExhibitImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "upload_image_url", nullable = false)
    private String uploadImageUrl;

    @Column(name = "upload_image_name", nullable = false)
    private String uploadImageName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exhibits_id", nullable = false)
    public Exhibit exhibit;
}
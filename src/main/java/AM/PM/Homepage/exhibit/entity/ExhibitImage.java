package AM.PM.Homepage.exhibit.entity;

import AM.PM.Homepage.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Table(name = "exhibit_image")
public class ExhibitImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column(name = "original_image_name", nullable = false)
    private String originalImageName;

    @Column(name = "upload_image_name")
    private String uploadImageName;

    @Getter
    @Column(name = "upload_image_path", nullable = false)
    private String uploadImagePath;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exhibits_id", nullable = false)
    public Exhibit exhibit;

    @Builder
    public ExhibitImage(String originalImageName, String uploadImageName, String uploadImagePath, Exhibit exhibit) {
        this.originalImageName = originalImageName;
        this.uploadImageName = uploadImageName;
        this.uploadImagePath = uploadImagePath;
        this.exhibit = exhibit;
    }
}
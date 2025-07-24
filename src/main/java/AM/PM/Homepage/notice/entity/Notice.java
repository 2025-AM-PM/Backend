package AM.PM.Homepage.notice.entity;

import AM.PM.Homepage.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Table(name = "notices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Setter
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false)
    private NoticeType noticeType;

    @ColumnDefault("0")
    @Column(name = "views")
    private Integer views;

    @Column(name = "notice_url")
    private String url;

    @Builder
    protected Notice(String title, String content, NoticeType noticeType, String url) {
        this.title = title;
        this.content = content;
        this.noticeType = noticeType;
        this.url = url;
    }

    public void increaseViews() {
        this.views++;
    }

    public void update(String title, String content, NoticeType type, String url) {
        this.title = title;
        this.content = content;
        this.noticeType = type;
        this.url = url;
    }
}
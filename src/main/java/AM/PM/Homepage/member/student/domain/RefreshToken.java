package AM.PM.Homepage.member.student.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private String refreshToken;
    private String expiration;


    @Builder
    public RefreshToken(String expiration, UUID id, String refreshToken) {
        this.expiration = expiration;
        this.id = id;
        this.refreshToken = refreshToken;
    }
}

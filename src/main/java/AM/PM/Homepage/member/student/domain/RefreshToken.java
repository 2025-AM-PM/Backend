package AM.PM.Homepage.member.student.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {


    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne
    @JoinColumn(name = "student_id")
    @Builder.Default
    private Student student;

    private String refreshToken;
    private String expiration;

}

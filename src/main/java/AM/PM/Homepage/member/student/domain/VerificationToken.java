package AM.PM.Homepage.member.student.domain;

import jakarta.persistence.*;

@Entity
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_code")
    private String verificationCode;


}

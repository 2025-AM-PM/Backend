package AM.PM.Homepage.member.student.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VerificationTokenService {

    public String issuedVerificationToken() {
        return UUID.randomUUID().toString();
    }

}

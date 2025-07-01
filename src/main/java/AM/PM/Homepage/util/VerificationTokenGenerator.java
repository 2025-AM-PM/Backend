package AM.PM.Homepage.util;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public final class VerificationTokenGenerator {

    public static String issuedVerificationToken() {
        return UUID.randomUUID().toString();
    }

}

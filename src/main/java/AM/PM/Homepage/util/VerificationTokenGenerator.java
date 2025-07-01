package AM.PM.Homepage.util;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Component
public class VerificationTokenGenerator {

    public String issuedVerificationToken() {
        return UUID.randomUUID().toString();
    }

}

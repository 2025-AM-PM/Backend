package AM.PM.Homepage.util;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieProvider {

    @Value("${spring.jwt.refresh-cookie.max.age.second}")
    private static int COOKIE_MAX_AGE;


    private CookieProvider() {
    }

    public Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }


}



package AM.PM.Homepage.util;

import jakarta.servlet.http.Cookie;

public final class CookieProvider {

    private final static int COOKIE_MAX_AGE = 24*60*60;


    private CookieProvider() {
    }

    public static Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }


}

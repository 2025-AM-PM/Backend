package AM.PM.Homepage.util.constant;

import lombok.Getter;


@Getter
public enum JwtTokenType {

    ACCESS_TOKEN("access"),
    REFRESH_TOKEN("refresh");

    private final String value;

    JwtTokenType(String value) {
        this.value = value;
    }

}

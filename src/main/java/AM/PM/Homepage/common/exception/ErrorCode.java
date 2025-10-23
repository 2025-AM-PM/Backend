package AM.PM.Homepage.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    BAD_REQUEST("4000", "잘못된 요청", HttpStatus.BAD_REQUEST),
    INVALID_POLL_NO_SELECTION("4001", "선택 항목이 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_POLL_SINGLE_SELECTION("4002", "단일 선택 투표에서는 1개만 선택할 수 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_POLL_MAX_SELECTION("4003", "최대 선택 수를 초과했습니다.", HttpStatus.BAD_REQUEST),
    INVALID_POLL_OPTION("4004", "유효하지 않은 옵션이 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    NOT_BELONG_TO_GROUP("4005", "소속되지 않은 멤버입니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_NEW_MISMATCH("4006", "새 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 401 Unauthorized
    UNAUTHORIZED("4010", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    BAD_CREDENTIALS("4011", "학번 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REQUIRED("4012", "리프레시 토큰이 없습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("4013", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_CATEGORY("4014", "토큰의 종류가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("4015", "유효하지 않는 토큰", HttpStatus.UNAUTHORIZED),

    // 403 Forbidden
    FORBIDDEN("4030", "허용되지 않은 요청", HttpStatus.FORBIDDEN),
    FORBIDDEN_NOT_STAFF("4031", "관리자 외 접근할 수 없습니다.", HttpStatus.FORBIDDEN),
    FORBIDDEN_NOT_ADMIN("4032", "어드민(회장/부회장/시스템 관리자) 외 접근할 수 없습니다.", HttpStatus.FORBIDDEN),
    FORBIDDEN_NOT_SYSTEM_ADMIN("4033", "시스템 관리자 외 접근할 수 없습니다.", HttpStatus.FORBIDDEN),
    FORBIDDEN_POLL_CLOSE("4034", "투표 생성자만 마감할 수 있습니다.", HttpStatus.FORBIDDEN),
    FORBIDDEN_POLL_DELETE("4035", "투표 생성자만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN),
    FORBIDDEN_GROUP_LEADER_ONLY("4036", "스터디 그룹 리더만 수행할 수 있습니다.", HttpStatus.FORBIDDEN),
    FORBIDDEN_NOT_OWNER("4037", "본인만 수행할 수 있습니다.", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_EXHIBIT("4038", "exhibit 접근 권한 없음", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_("4039", "poll 접근 권한 없음", HttpStatus.FORBIDDEN),
    FORBIDDEN_CHANGE_ROLE("40331", "권한을 바꿀 수 없음", HttpStatus.FORBIDDEN),

    // 404 Not Found
    NOT_FOUND_STUDENT("4041", "student 엔티티를 찾을 수 없음", HttpStatus.NOT_FOUND),
    NOT_FOUND_EXHIBIT("4042", "exhibit 엔티티를 찾을 수 없음", HttpStatus.NOT_FOUND),
    NOT_FOUND_POLL("4043", "poll 엔티티를 찾을 수 없음", HttpStatus.NOT_FOUND),
    NOT_FOUND_NOTICE("4044", "notice 엔티티를 찾을 수 없음", HttpStatus.NOT_FOUND),
    NOT_FOUND_STUDY_GROUP("4045", "STUDY_GROUP 엔티티를 찾을 수 없음", HttpStatus.NOT_FOUND),
    NOT_FOUND_APPLICATION("4046", "APPLICATION 엔티티를 찾을 수 없음", HttpStatus.NOT_FOUND),
    NOT_FOUND_MEMBER("4047", "STUDY_GROUP 멤버를 찾을 수 없음", HttpStatus.NOT_FOUND),

    // 405 Method Not Allowed
    NOT_ALLOWED_METHOD("4050", "잘못된 요청 메서드", HttpStatus.METHOD_NOT_ALLOWED),

    // 409 Conflict
    DUPLICATE_STUDENT_NUMBER("4090", "이미 가입된 학번입니다.", HttpStatus.CONFLICT),
    CLOSED_POLL("4091", "이미 마감된 투표입니다.", HttpStatus.CONFLICT),
    RE_VOTE_NOT_ALLOWED("4092", "재투표가 허용되지 않는 투표입니다.", HttpStatus.CONFLICT),
    DUPLICATE_VOTE_REQUEST("4093", "이미 처리된 요청입니다.", HttpStatus.CONFLICT),
    CLOSED_STUDY_GROUP("4094", "모집이 마감된 스터디 그룹입니다.", HttpStatus.CONFLICT),
    ALREADY_JOINED_STUDY_GROUP("4095", "이미 가입된 스터디 그룹입니다.", HttpStatus.CONFLICT),
    FULL_STUDY_GROUP("4096", "스터디 그룹 인원이 초과되었습니다.", HttpStatus.CONFLICT),
    DUPLICATE_APPLICATION("4097", "이미 지원한 스터디 그룹입니다.", HttpStatus.CONFLICT),
    ALREADY_PROCESSED_APPLICATION("4098", "이미 처리된 신청입니다.", HttpStatus.CONFLICT),
    LEADER_CANNOT_LEAVE("4099", "스터디 리더는 탈퇴할 수 없습니다.", HttpStatus.CONFLICT),
    LEADER_CANNOT_BE_REMOVED("4100", "스터디 리더는 삭제할 수 없습니다.", HttpStatus.CONFLICT),

    // 422 Unprocessable Entity
    FAIL_PARSE_SECTION("4221", "'.section' 을 찾을 수 없음. 웹 페이지 구조가 변경되었을 수 있음", HttpStatus.UNPROCESSABLE_ENTITY),
    FAIL_PARSE_WEEK_DATES("4222", "유효한 주중 날짜를 파싱할 수 없음", HttpStatus.UNPROCESSABLE_ENTITY),

    // 500 Internal Server Error
    INTERNAL_ERROR("5000", "내부 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}

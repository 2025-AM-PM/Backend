package AM.PM.Homepage.member.auth.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.algorithmprofile.repository.AlgorithmGradeRepository;
import AM.PM.Homepage.member.auth.domain.SignupApplication;
import AM.PM.Homepage.member.auth.domain.SignupApplicationStatus;
import AM.PM.Homepage.member.auth.repository.SignupApplicationRepository;
import AM.PM.Homepage.member.auth.request.LoginRequest;
import AM.PM.Homepage.member.auth.request.SignupApplicationRequest;
import AM.PM.Homepage.member.auth.response.LoginSuccessResponse;
import AM.PM.Homepage.member.auth.response.SignupApplicationProcessResponse;
import AM.PM.Homepage.member.auth.response.SignupApplicationResponse;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.request.StudentSignupRequest;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final StudentRepository studentRepository;
    private final SignupApplicationRepository applicationRepository;
    private final AlgorithmGradeRepository algorithmGradeRepository;

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;

    // 로그인
    public ResponseEntity<LoginSuccessResponse> login(LoginRequest request, String deviceId) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getStudentNumber(), request.getStudentPassword()));
            UserAuth principal = (UserAuth) auth.getPrincipal();

            Long studentId = principal.getId();
            String studentNumber = principal.getStudentNumber();
            String studentName = principal.getStudentName();
            StudentRole role = principal.getRole();
            Integer algorithmTier = algorithmGradeRepository.findTierByStudentId(studentId).orElse(null);

            if (deviceId == null || deviceId.isBlank()) deviceId = UUID.randomUUID().toString();

            String accessToken = jwtUtil.generateAccessToken(studentId, studentNumber, role);
            String refreshToken = jwtUtil.generateRefreshToken(studentId, studentNumber, role, deviceId);

            // 리프레시 저장(회전 대비)
            refreshTokenService.registerRefreshToken(studentId, deviceId, refreshToken);

            LoginSuccessResponse body = new LoginSuccessResponse(
                    studentId, studentNumber, studentName, role.name(), algorithmTier
            );

            log.info("로그인 성공: studentId={}", studentId);
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + accessToken)
                    .header("X-Device-Id", deviceId)
                    .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken, false, "Lax").toString())
                    .body(body);
        } catch (BadCredentialsException e) {
            throw new CustomException(ErrorCode.BAD_CREDENTIALS, "studentNumber=" + request.getStudentNumber());
        }
    }

    private ResponseCookie buildRefreshCookie(String refreshToken, boolean secure, String sameSite) {
        return ResponseCookie.from("refresh", refreshToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(jwtUtil.getRefreshTtl())
                .build();
    }

    // 회원가입 신청
    public void signup(StudentSignupRequest request) {
        if (applicationRepository.existsByStudentNumber(request.getStudentNumber())
                || studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new CustomException(ErrorCode.DUPLICATE_STUDENT_NUMBER,
                    "이미 신청되거나 가입된 학번입니다. studentNumber=" + request.getStudentNumber());
        }

        SignupApplication application = SignupApplication.builder()
                .studentNumber(request.getStudentNumber())
                .studentName(request.getStudentName())
                .studentPassword(bCryptPasswordEncoder.encode(request.getStudentPassword()))
                .build();

        applicationRepository.save(application);
        log.info("[학생 가입 신청 완료] id={}, studentNumber={}", application.getId(), application.getStudentNumber());
    }

    // 어드민 목록/승인/거절
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('STAFF','PRESIDENT','SYSTEM_ADMIN')")
    public List<SignupApplicationResponse> getSignupApplications(SignupApplicationStatus status) {
        var list = (status == null) ? applicationRepository.findAll() : applicationRepository.findByStatus(status);
        return list.stream().map(SignupApplicationResponse::from).toList();
    }

    @PreAuthorize("hasAnyRole('PRESIDENT','SYSTEM_ADMIN')")
    public SignupApplicationProcessResponse approveSignup(SignupApplicationRequest request) {
        var students = new ArrayList<Student>();
        var applications = applicationRepository.findByStatus(SignupApplicationStatus.PENDING);

        for (SignupApplication app : applications) {
            // TODO: requestIds 필터링 로직 개선 필요
            if (!request.getApplicationIds().contains(app.getId())) {
                throw new CustomException(ErrorCode.NOT_FOUND_APPLICATION, "applicationId=" + app.getId());
            }
            app.approve();
            students.add(Student.signup(app.getStudentNumber(), app.getStudentName(), app.getStudentPassword()));
        }
        studentRepository.saveAll(students);
        return new SignupApplicationProcessResponse(students.size(), SignupApplicationStatus.APPROVED);
    }

    @PreAuthorize("hasAnyRole('PRESIDENT','SYSTEM_ADMIN')")
    public SignupApplicationProcessResponse rejectSignup(SignupApplicationRequest request) {
        var applications = applicationRepository.findByStatus(SignupApplicationStatus.PENDING);
        for (SignupApplication app : applications) {
            if (!request.getApplicationIds().contains(app.getId())) {
                throw new CustomException(ErrorCode.NOT_FOUND_APPLICATION, "applicationId=" + app.getId());
            }
            app.reject();
        }
        return new SignupApplicationProcessResponse(applications.size(), SignupApplicationStatus.REJECTED);
    }
}

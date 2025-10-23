package AM.PM.Homepage.member.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import AM.PM.Homepage.member.auth.domain.SignupApplication;
import AM.PM.Homepage.member.auth.domain.SignupApplicationStatus;
import AM.PM.Homepage.member.auth.repository.SignupApplicationRepository;
import AM.PM.Homepage.member.auth.request.SignupApplicationRequest;
import AM.PM.Homepage.member.auth.response.SignupApplicationProcessResponse;
import AM.PM.Homepage.member.student.request.StudentSignupRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    SignupApplicationRepository applicationRepository;
    @Autowired
    AuthService authService;

    @Test
    void 회원가입_요청_테스트() {
        StudentSignupRequest req = new StudentSignupRequest("123", "name", "pw");
        authService.signup(req);

        List<SignupApplication> applications = applicationRepository.findAll();

        assertThat(applications.size()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "PRESIDENT")
    void 회원가입_관리자_승인_성공() {
        authService.signup(new StudentSignupRequest("123", "name1", "pw"));
        SignupApplication application = applicationRepository.findAll().getFirst();
        SignupApplicationRequest req = new SignupApplicationRequest();
        req.setApplicationIds(List.of(application.getId()));

        SignupApplicationProcessResponse res = authService.approveSignup(req);

        assertThat(res.getTotal()).isEqualTo(1L);
        assertThat(res.getStatus()).isEqualTo(SignupApplicationStatus.APPROVED);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void 회원가입_관리자_승인_실패() {
        authService.signup(new StudentSignupRequest("123", "name1", "pw"));
        SignupApplication application = applicationRepository.findAll().getFirst();
        SignupApplicationRequest req = new SignupApplicationRequest();
        req.setApplicationIds(List.of(application.getId()));

        assertThatThrownBy(() -> authService.approveSignup(req)).isExactlyInstanceOf(
                AuthorizationDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void 회원가입_관리자_거절_성공() {
        authService.signup(new StudentSignupRequest("123", "name1", "pw"));
        SignupApplication application = applicationRepository.findAll().getFirst();
        SignupApplicationRequest req = new SignupApplicationRequest();
        req.setApplicationIds(List.of(application.getId()));

        SignupApplicationProcessResponse res = authService.rejectSignup(req);

        assertThat(res.getTotal()).isEqualTo(1L);
        assertThat(res.getStatus()).isEqualTo(SignupApplicationStatus.REJECTED);
    }
}
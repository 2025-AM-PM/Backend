package AM.PM.Homepage.member.signupapplication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import AM.PM.Homepage.member.signupapplication.domain.SignupApplication;
import AM.PM.Homepage.member.signupapplication.domain.SignupApplicationStatus;
import AM.PM.Homepage.member.signupapplication.repository.SignupApplicationRepository;
import AM.PM.Homepage.member.signupapplication.request.SignupApplicationRequest;
import AM.PM.Homepage.member.signupapplication.response.SignupApplicationResponse;
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
class SignupServiceTest {

    @Autowired
    SignupApplicationRepository applicationRepository;
    @Autowired
    SignupService signupService;

    @Test
    void 회원가입_요청_테스트() {
        StudentSignupRequest req = new StudentSignupRequest("123", "name", "pw");
        signupService.signup(req);

        List<SignupApplication> applications = applicationRepository.findAll();

        assertThat(applications.size()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "PRESIDENT")
    void 회원가입_관리자_승인_성공() {
        signupService.signup(new StudentSignupRequest("123", "name1", "pw"));
        SignupApplication application = applicationRepository.findAll().getFirst();
        SignupApplicationRequest req = new SignupApplicationRequest();
        req.setApplicationIds(List.of(application.getId()));

        SignupApplicationResponse res = signupService.approveSignup(req);

        assertThat(res.getTotal()).isEqualTo(1L);
        assertThat(res.getStatus()).isEqualTo(SignupApplicationStatus.APPROVED);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void 회원가입_관리자_승인_실패() {
        signupService.signup(new StudentSignupRequest("123", "name1", "pw"));
        SignupApplication application = applicationRepository.findAll().getFirst();
        SignupApplicationRequest req = new SignupApplicationRequest();
        req.setApplicationIds(List.of(application.getId()));

        assertThatThrownBy(() -> signupService.approveSignup(req)).isExactlyInstanceOf(
                AuthorizationDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void 회원가입_관리자_거절_성공() {
        signupService.signup(new StudentSignupRequest("123", "name1", "pw"));
        SignupApplication application = applicationRepository.findAll().getFirst();
        SignupApplicationRequest req = new SignupApplicationRequest();
        req.setApplicationIds(List.of(application.getId()));

        SignupApplicationResponse res = signupService.rejectSignup(req);

        assertThat(res.getTotal()).isEqualTo(1L);
        assertThat(res.getStatus()).isEqualTo(SignupApplicationStatus.REJECTED);
    }
}
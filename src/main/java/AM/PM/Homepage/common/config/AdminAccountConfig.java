package AM.PM.Homepage.common.config;

import static AM.PM.Homepage.member.student.domain.StudentRole.SYSTEM_ADMIN;

import AM.PM.Homepage.member.signupapplication.service.SignupService;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.service.StudentService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 서버 시작시, 관리자 계정 자동 생성. 동아리 회장, 부회장이 아닌 홈페이지 관리자로 세팅해야 함 application.properties 에서 설정
 */
@Component
@RequiredArgsConstructor
public class AdminAccountConfig {

    private final AdminAccountService adminAccountService;

    @PostConstruct
    public void init() {
        adminAccountService.doInit();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class AdminAccountService {
        private final StudentRepository studentRepository;
        private final BCryptPasswordEncoder bCryptPasswordEncoder;

        @Value("${account.admin.name}")
        private String ADMIN_PASSWORD;
        @Value("${account.admin.number}")
        private String ADMIN_NUMBER;
        @Value("${account.admin.password}")
        private String ADMIN_NAME;

        public void doInit() {
            String encoded = bCryptPasswordEncoder.encode(ADMIN_PASSWORD);
            Student admin = new Student(ADMIN_NUMBER, SYSTEM_ADMIN, ADMIN_NAME, encoded);
            studentRepository.save(admin);
        }
    }
}
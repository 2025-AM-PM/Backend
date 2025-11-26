package AM.PM.Homepage.common.config;

import static AM.PM.Homepage.member.student.domain.StudentRole.SYSTEM_ADMIN;

import AM.PM.Homepage.member.algorithmprofile.domain.AlgorithmProfile;
import AM.PM.Homepage.member.algorithmprofile.repository.AlgorithmGradeRepository;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

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
        private final BCryptPasswordEncoder passwordEncoder;
        private final AlgorithmGradeRepository algorithmGradeRepository;

        @Value("${account.admin.number}")
        private String adminNumber;
        @Value("${account.admin.name}")
        private String adminName;
        @Value("${account.admin.password}")
        private String adminPassword;

        public void doInit() {
            try {
                String encoded = passwordEncoder.encode(adminPassword);
                Student admin = new Student(adminNumber, SYSTEM_ADMIN, adminName, encoded);
                studentRepository.save(admin);

                AlgorithmProfile adminProfile = new AlgorithmProfile(admin.getId(), 22, 0, 0, admin);
                algorithmGradeRepository.save(adminProfile);
            } catch (Exception ignore) {
                // 무시
            }
        }
    }
}

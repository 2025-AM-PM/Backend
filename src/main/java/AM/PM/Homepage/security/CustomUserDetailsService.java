package AM.PM.Homepage.security;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository userRepository;

    public CustomUserDetailsService(StudentRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String studentNumber) throws UsernameNotFoundException {
        Student userData = userRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new UsernameNotFoundException("학생을 찾을 수 없음: " + studentNumber));
        return new UserAuth(userData);
    }
}

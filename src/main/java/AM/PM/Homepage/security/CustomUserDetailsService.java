package AM.PM.Homepage.security;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String studentNumber) throws UsernameNotFoundException {
        Student student = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new UsernameNotFoundException("찾을 수 없는 유저" + studentNumber));

        return new UserAuth(
                student.getId(),
                studentNumber,
                student.getPassword(),
                student.getStudentName(),
                student.getRole()
        );
    }
}

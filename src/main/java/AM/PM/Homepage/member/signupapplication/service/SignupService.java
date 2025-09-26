package AM.PM.Homepage.member.signupapplication.service;

import AM.PM.Homepage.common.exception.CustomException;
import AM.PM.Homepage.common.exception.ErrorCode;
import AM.PM.Homepage.member.signupapplication.domain.SignupApplication;
import AM.PM.Homepage.member.signupapplication.domain.SignupApplicationStatus;
import AM.PM.Homepage.member.signupapplication.repository.SignupApplicationRepository;
import AM.PM.Homepage.member.signupapplication.request.SignupApplicationRequest;
import AM.PM.Homepage.member.signupapplication.response.SignupApplicationProcessResponse;
import AM.PM.Homepage.member.signupapplication.response.SignupApplicationResponse;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.request.StudentSignupRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SignupService {

    private final StudentRepository studentRepository;
    private final SignupApplicationRepository applicationRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 회원가입 요청
    public void signup(StudentSignupRequest request) {
        log.info("[학생 가입 신청 요청] studentNumber={}, name={}", request.getStudentNumber(), request.getStudentName());

        if(applicationRepository.existsByStudentNumber(request.getStudentNumber()) || studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new CustomException(ErrorCode.DUPLICATE_STUDENT_NUMBER, "이미 신청되거나 가입된 학번입니다. studentNumber=" + request.getStudentNumber());
        }

        SignupApplication application = SignupApplication.builder()
                .studentNumber(request.getStudentNumber())
                .studentName(request.getStudentName())
                .studentPassword(bCryptPasswordEncoder.encode(request.getStudentPassword()))
                .build();

        applicationRepository.save(application);
        log.info("[학생 가입 신청 완료] studentId={}, studentNumber={}", application.getId(), application.getStudentNumber());
    }

    // 회원가입 요청 전체 조회
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('STAFF', 'PRESIDENT', 'SYSTEM_ADMIN')")
    public List<SignupApplicationResponse> getSignupApplications(SignupApplicationStatus status) {
        List<SignupApplication> responses;
        if(status == null) {
            responses = applicationRepository.findAll();
        } else {
            responses = applicationRepository.findByStatus(status);
        }
        return responses.stream()
                .map(SignupApplicationResponse::from)
                .toList();
    }

    // 회원가입 요청 일괄 승인
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SYSTEM_ADMIN')")
    public SignupApplicationProcessResponse approveSignup(SignupApplicationRequest request) {
        log.info("[회원가입 요청 - 일괄 승인 요청] studentId={}, size={}",
                request.getApplicationIds(), request.getApplicationIds().size());

        List<Student> students = new ArrayList<>();
        List<SignupApplication> applications = applicationRepository.findByStatus(SignupApplicationStatus.PENDING);
        for (SignupApplication application : applications) {
            // TODO: 버그 수정 필요. requestIds 안에 포함되어 있어야 하는게 아님
            if (!request.getApplicationIds().contains(application.getId())) {
                throw new CustomException(ErrorCode.NOT_FOUND_APPLICATION, "applicationId=" + application.getId());
            }
            application.approve();

            Student student = Student.signup(
                    application.getStudentNumber(),
                    application.getStudentName(),
                    application.getStudentPassword()
            );
            students.add(student);
        }
        studentRepository.saveAll(students);
        log.info("[회원가입 요청 - 일괄 승인 성공] students size={}", students.size());

        return new SignupApplicationProcessResponse(students.size(), SignupApplicationStatus.APPROVED);
    }

    // 회원가입 요청 일괄 거절
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SYSTEM_ADMIN')")
    public SignupApplicationProcessResponse rejectSignup(SignupApplicationRequest request) {
        log.info("[회원가입 요청 - 일괄 거절 요청] studentId={}, size={}",
                request.getApplicationIds(), request.getApplicationIds().size());

        List<SignupApplication> applications = applicationRepository.findByStatus(SignupApplicationStatus.PENDING);
        for (SignupApplication application : applications) {
            if (!request.getApplicationIds().contains(application.getId())) {
                throw new CustomException(ErrorCode.NOT_FOUND_APPLICATION, "applicationId=" + application.getId());
            }
            application.reject();
        }
        log.info("[회원가입 요청 - 일괄 거절 성공] students size={}", applications.size());

        return new SignupApplicationProcessResponse(applications.size(), SignupApplicationStatus.REJECTED);
    }
}

package AM.PM.Homepage.member.student.domain;

import AM.PM.Homepage.exhibit.entity.Exhibit;
import AM.PM.Homepage.member.student.response.StudentResponse;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplication;
import AM.PM.Homepage.studygroup.entity.StudyGroupMember;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", unique = true)
//    @Pattern(regexp = "^[0-9]{8}$")
    private String studentNumber;

    @Column(name = "student_role", nullable = false)
    private String studentRole;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_password")
    private String password;

    @Column(name = "solved_ac_token")
    private String verificationToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "algorithm_profile_id")
    private AlgorithmProfile baekjoonTier;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refresh_token_id")
    private RefreshToken refreshToken;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exhibit> exhibits;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyGroupMember> studyGroupMembers;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyGroupApplication> studyGroupApplications;

    public Student(StudentResponse response) {
        this.studentNumber = response.getStudentNumber();
        this.studentRole = "ROLE_USER";
        this.studentName = response.getStudentName();
        this.password = response.getPhoneNumber();
        this.verificationToken = issuedVerificationToken();
    }

    private Student(String studentNumber, String studentRole, String studentName, String password) {
        this.studentNumber = studentNumber;
        this.studentRole = studentRole;
        this.studentName = studentName;
        this.password = password;
        this.verificationToken = issuedVerificationToken();
    }

    public static Student signup(String studentNumber, String studentRole, String studentName, String encryptedPassword) {
        return new Student(studentNumber, studentRole, studentName, encryptedPassword);
    }

    public void linkAlgorithmProfile(AlgorithmProfile profile) {
        this.baekjoonTier = profile;
        profile.linkStudent(this);
    }

    public static List<Student> from(List<StudentResponse> response) {

        return response.stream()
                .map(Student::new)
                .toList();
    }

    public String issuedVerificationToken() {
        return UUID.randomUUID().toString();
    }


}
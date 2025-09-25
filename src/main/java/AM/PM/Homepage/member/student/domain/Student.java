package AM.PM.Homepage.member.student.domain;

import AM.PM.Homepage.exhibit.entity.Exhibit;
import AM.PM.Homepage.member.algorithmprofile.domain.AlgorithmProfile;
import AM.PM.Homepage.studygroup.entity.StudyGroupApplication;
import AM.PM.Homepage.studygroup.entity.StudyGroupMember;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "student_role", nullable = false)
    private StudentRole role;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_password")
    private String password;

    @Column(name = "solved_ac_token")
    private String verificationToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "algorithm_profile_id")
    private AlgorithmProfile baekjoonTier;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exhibit> exhibits;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyGroupMember> studyGroupMembers;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyGroupApplication> studyGroupApplications;

    private Student(String studentNumber, StudentRole role, String studentName, String password) {
        this.studentNumber = studentNumber;
        this.role = role;
        this.studentName = studentName;
        this.password = password;
        this.verificationToken = issuedVerificationToken();
    }

    public static Student signup(String studentNumber, String studentName, String encryptedPassword) {
        return new Student(studentNumber, StudentRole.USER, studentName, encryptedPassword);
    }

    public void linkAlgorithmProfile(AlgorithmProfile profile) {
        this.baekjoonTier = profile;
        profile.linkStudent(this);
    }

    public String issuedVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public void changeRole(StudentRole role) {
        this.role = role;
    }
}
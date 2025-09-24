package AM.PM.Homepage.member.student.repository;

import static AM.PM.Homepage.member.student.domain.QAlgorithmProfile.algorithmProfile;
import static AM.PM.Homepage.member.student.domain.QStudent.student;

import AM.PM.Homepage.admin.response.AllStudentDetailResponse;
import AM.PM.Homepage.member.student.domain.QStudent;
import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.response.QStudentDetailResponse;
import AM.PM.Homepage.admin.response.StudentDetailResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudentRepositoryImpl implements StudentRepositoryCustom {

    private final JPAQueryFactory qf;

    // 학생 정보 상세 조회
    @Override
    public AllStudentDetailResponse getAllStudentDetailResponse() {
        List<StudentDetailResponse> studentResponses = qf
                .select(new QStudentDetailResponse(
                        student.id,
                        student.studentNumber,
                        student.studentName,
                        student.role,
                        algorithmProfile.id,
                        algorithmProfile.tier,
                        algorithmProfile.solvedCount,
                        algorithmProfile.rating
                ))
                .from(student)
                .leftJoin(algorithmProfile).on(algorithmProfile.student.eq(student))
                .orderBy(student.role.asc(), student.studentName.asc())
                .fetch();

        long total = Optional.ofNullable(qf
                        .select(student.count())
                        .from(student)
                        .fetchOne())
                .orElse(0L);

        return new AllStudentDetailResponse(studentResponses, total);
    }

    // 알고리즘 프로필 조인해서 가져오기
    @Override
    public Optional<Student> findByIdWithAlgorithmProfile(Long studentId) {
        Student student = qf
                .selectFrom(QStudent.student)
                .leftJoin(QStudent.student.baekjoonTier, algorithmProfile)
                .fetchJoin()
                .where(QStudent.student.id.eq(studentId))
                .fetchOne();
        return Optional.ofNullable(student);
    }
}

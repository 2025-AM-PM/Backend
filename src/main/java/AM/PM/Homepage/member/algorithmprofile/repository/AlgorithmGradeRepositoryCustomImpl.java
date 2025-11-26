package AM.PM.Homepage.member.algorithmprofile.repository;

import AM.PM.Homepage.member.algorithmprofile.domain.QAlgorithmProfile;
import AM.PM.Homepage.member.algorithmprofile.response.AlgorithmProfileResponse;
import AM.PM.Homepage.member.algorithmprofile.response.QAlgorithmProfileResponse;
import AM.PM.Homepage.member.student.domain.QStudent;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AlgorithmGradeRepositoryCustomImpl implements AlgorithmGradeRepositoryCustom {

    private final JPAQueryFactory qf;

    @Override
    public List<AlgorithmProfileResponse> findTopTier(int count) {
        QStudent student = QStudent.student;
        QAlgorithmProfile profile = QAlgorithmProfile.algorithmProfile;
        return qf
                .select(new QAlgorithmProfileResponse(
                        student.id,
                        student.studentName,
                        student.studentNumber,
                        profile.tier,
                        profile.solvedCount,
                        profile.rating
                ))
                .from(student)
                .innerJoin(student.baekjoonTier, profile)
                .where(student.baekjoonTier.isNotNull()) // 미인증 제외
                .where(student.id.ne(1L)) // 초기 어드민 제외
                .orderBy(
                        profile.rating.desc(),
                        profile.tier.desc(),
                        profile.solvedCount.desc()
                )
                .limit(count)
                .fetch();
    }
}

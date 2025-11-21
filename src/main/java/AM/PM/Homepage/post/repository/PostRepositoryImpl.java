package AM.PM.Homepage.post.repository;

import static AM.PM.Homepage.post.domain.QPost.post;
import static org.springframework.util.StringUtils.hasText;

import AM.PM.Homepage.member.student.domain.QStudent;
import AM.PM.Homepage.member.student.response.QStudentResponse;
import AM.PM.Homepage.member.student.response.StudentResponse;
import AM.PM.Homepage.post.response.PostDetailResponse;
import AM.PM.Homepage.post.response.PostSummaryResponse;
import AM.PM.Homepage.post.response.QPostDetailResponse;
import AM.PM.Homepage.post.response.QPostSummaryResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory qf;

    @Override
    public Page<PostSummaryResponse> search(String title, String createdBy, Pageable pageable) {
        // 정렬 조건
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(pageable);

        // content 쿼리
        List<PostSummaryResponse> content = qf
                .select(new QPostSummaryResponse(
                        post.id,
                        post.title,
                        post.category,
                        post.likes,
                        post.views,
                        post.createdAt,
                        post.updatedAt
                )).from(post)
                .where(
                        titleContains(title)
                ).orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = qf
                .select(post.count())
                .from(post)
                .where(
                        titleContains(title)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchFirst);
    }

    @Override
    public Optional<PostDetailResponse> findByIdWithStudent(Long postId) {
        QStudent creator = new QStudent("creator");
        QStudent updater = new QStudent("updater");

        PostDetailResponse result = qf
                .select(new QPostDetailResponse(
                        post.id,
                        post.title,
                        post.content,
                        post.category,
                        post.likes,
                        post.views,
                        post.createdAt,
                        post.updatedAt,
                        new QStudentResponse(
                                creator.id,
                                creator.studentNumber,
                                creator.studentName,
                                creator.role
                        ),
                        new QStudentResponse(
                                updater.id,
                                updater.studentNumber,
                                updater.studentName,
                                updater.role
                        )
                ))
                .from(post)
                .leftJoin(creator).on(post.createdBy.eq(creator.id))
                .leftJoin(updater).on(post.updatedBy.eq(updater.id))
                .where(post.id.eq(postId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    private BooleanExpression titleContains(String title) {
        return hasText(title) ? post.title.containsIgnoreCase(title) : null;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        if (pageable != null) {
            pageable.getSort();
            for (Sort.Order o : pageable.getSort()) {
                Order direction = o.isAscending() ? Order.ASC : Order.DESC;
                switch (o.getProperty()) {
                    case "title" -> orders.add(new OrderSpecifier<>(direction, post.title));
                    case "createdAt" -> orders.add(new OrderSpecifier<>(direction, post.createdAt));
                    case "views" -> orders.add(new OrderSpecifier<>(direction, post.views));
                    case "likes" -> orders.add(new OrderSpecifier<>(direction, post.likes));
                    default -> {
                    }
                }
            }
        }
        // 안정적 결과를 위한 기본 정렬 (가장 마지막 우선순위)
        orders.add(new OrderSpecifier<>(Order.DESC, post.id));
        return orders.toArray(new OrderSpecifier<?>[0]);
    }
}

package AM.PM.Homepage.poll.repository;

import static AM.PM.Homepage.poll.entity.QPoll.poll;
import static AM.PM.Homepage.poll.entity.QPollOption.pollOption;
import static AM.PM.Homepage.poll.entity.QPollVote.pollVote;
import static org.springframework.util.StringUtils.hasText;

import AM.PM.Homepage.poll.entity.PollStatus;
import AM.PM.Homepage.poll.entity.QPollVote;
import AM.PM.Homepage.poll.request.PollSearchParam;
import AM.PM.Homepage.poll.response.PollDetailResponse;
import AM.PM.Homepage.poll.response.PollOptionResponse;
import AM.PM.Homepage.poll.response.PollSummaryResponse;
import AM.PM.Homepage.poll.response.QPollDetailResponse;
import AM.PM.Homepage.poll.response.QPollOptionResponse;
import AM.PM.Homepage.poll.response.QPollSummaryResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PollRepositoryImpl implements PollRepositoryCustom {

    private final JPAQueryFactory qf;

    @Override
    public Page<PollSummaryResponse> searchByParam(PollSearchParam param, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        BooleanExpression[] predicates = {
                containTitle(param.getQuery()),
                eqStatus(param.getStatus(), now),
                goeDeadlineFrom(param.getDeadlineFrom()),
                loeDeadlineTo(param.getDeadlineTo())
        };

        JPQLQuery<Integer> optionCount = JPAExpressions
                .select(pollOption.count().intValue())
                .from(pollOption)
                .where(pollOption.poll.eq(poll));

        List<PollSummaryResponse> content = qf
                .select(new QPollSummaryResponse(
                        poll.id,
                        poll.title,
                        poll.status,
                        optionCount,
                        poll.maxSelect,
                        poll.multiple,
                        poll.anonymous,
                        poll.allowAddOption,
                        poll.allowRevote,
                        poll.deadlineAt,
                        poll.createdBy,
                        poll.createdAt
                ))
                .from(poll)
                .where(predicates)
                .orderBy(orderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = qf
                .select(poll.count())
                .from(poll)
                .where(predicates);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<PollDetailResponse> findDetailWithOption(Long pollId, Long userId) {
        PollDetailResponse pollDetail = qf
                .select(new QPollDetailResponse(
                        poll.id,
                        poll.title,
                        poll.description,
                        poll.status,
                        poll.maxSelect,
                        poll.multiple,
                        poll.anonymous,
                        poll.allowAddOption,
                        poll.allowRevote,
                        poll.deadlineAt,
                        poll.createdBy,
                        poll.createdAt,
                        poll.updatedAt
                ))
                .from(poll)
                .where(poll.id.eq(pollId))
                .fetchOne();

        if (pollDetail == null) {
            return Optional.empty();
        }

        QPollVote myVote = new QPollVote("myVote");

        List<PollOptionResponse> options = qf
                .select(new QPollOptionResponse(
                        pollOption.id,
                        pollOption.label,
                        pollVote.id.countDistinct(),
                        myVote.id.goe(1L)
                ))
                .from(pollOption)
                .leftJoin(pollVote).on(pollVote.option.eq(pollOption))
                .leftJoin(myVote).on(myVote.option.eq(pollOption)
                        .and(myVote.voterId.eq(userId)))
                .where(pollOption.poll.id.eq(pollId))
                .groupBy(pollOption.id, pollOption.label)
                .orderBy(pollOption.id.asc())
                .fetch();

        long totalVotes = options.stream()
                .mapToLong(PollOptionResponse::getCount)
                .sum();

        Set<Long> mySelectedOptionIds = options.stream()
                .filter(PollOptionResponse::isSelected)
                .map(PollOptionResponse::getId)
                .collect(Collectors.toSet());

        return Optional.of(PollDetailResponse.of(pollDetail, totalVotes, options, mySelectedOptionIds));
    }

    private BooleanExpression containTitle(String query) {
        if (!hasText(query)) {
            return null;
        }
        return poll.title.containsIgnoreCase(query);
    }

    private BooleanExpression eqStatus(PollStatus status, LocalDateTime now) {
        if (status == null) {
            return null;
        }
        if (status == PollStatus.OPEN) {
            return poll.status.eq(PollStatus.OPEN)
                    .and(poll.deadlineAt.after(now));
        }
        return poll.status.eq(status);
    }

    private BooleanExpression goeDeadlineFrom(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return poll.deadlineAt.goe(from);
    }

    private BooleanExpression loeDeadlineTo(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return poll.deadlineAt.loe(to);
    }

    // 검색 결과 정렬 순서
    private OrderSpecifier<?>[] orderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> list = new ArrayList<>();
        for (Sort.Order o : sort) {
            Order direction = o.isAscending() ? Order.ASC : Order.DESC;
            switch (o.getProperty()) {
                case "id" -> list.add(new OrderSpecifier<>(direction, poll.id));
                case "title" -> list.add(new OrderSpecifier<>(direction, poll.title));
                case "status" -> list.add(new OrderSpecifier<>(direction, poll.status));
                case "createdAt" -> list.add(new OrderSpecifier<>(direction, poll.createdAt));
                default -> list.add(new OrderSpecifier<>(direction, poll.deadlineAt));
            }
        }
        if (list.isEmpty()) {
            list.add(poll.deadlineAt.asc());
        }
        return list.toArray(OrderSpecifier[]::new);
    }
}

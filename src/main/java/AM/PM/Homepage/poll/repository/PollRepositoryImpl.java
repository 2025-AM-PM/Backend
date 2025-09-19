package AM.PM.Homepage.poll.repository;

import static AM.PM.Homepage.member.student.domain.QStudent.student;
import static AM.PM.Homepage.poll.entity.QPoll.poll;
import static AM.PM.Homepage.poll.entity.QPollOption.pollOption;
import static AM.PM.Homepage.poll.entity.QPollVote.pollVote;
import static org.springframework.util.StringUtils.hasText;

import AM.PM.Homepage.poll.entity.PollStatus;
import AM.PM.Homepage.poll.request.PollSearchParam;
import AM.PM.Homepage.poll.response.PollDetailResponse;
import AM.PM.Homepage.poll.response.PollOptionResponse;
import AM.PM.Homepage.poll.response.PollSummaryResponse;
import AM.PM.Homepage.poll.response.PollVoteDto;
import AM.PM.Homepage.poll.response.QPollDetailResponse;
import AM.PM.Homepage.poll.response.QPollOptionResponse;
import AM.PM.Homepage.poll.response.QPollSummaryResponse;
import AM.PM.Homepage.poll.response.QPollVoteDto;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Slf4j
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

        List<PollSummaryResponse> content = qf
                .select(new QPollSummaryResponse(
                        poll.id,
                        poll.title,
                        poll.status,
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
    public Optional<PollDetailResponse> findPollDetailResponseById(Long pollId) {
        return Optional.ofNullable(qf
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
                        poll.resultVisibility,
                        poll.deadlineAt,
                        poll.createdBy,
                        poll.createdAt,
                        poll.updatedAt,
                        poll.closedAt
                ))
                .from(poll)
                .where(poll.id.eq(pollId))
                .fetchOne());
    }

    @Override
    public List<PollOptionResponse> findPollOptionResponsesByPollId(Long pollId) {
        return qf
                .select(new QPollOptionResponse(
                        pollOption.id,
                        pollOption.label
                ))
                .from(pollOption)
                .where(pollOption.poll.id.eq(pollId))
                .orderBy(pollOption.id.asc())
                .fetch();
    }

    @Override
    public Set<Long> findOptionIdsByPollIdAndUserId(Long pollId, Long userId) {
        return new HashSet<>(qf
                .select(pollVote.option.id)
                .from(pollVote)
                .where(pollVote.option.poll.id.eq(pollId)
                        .and(pollVote.voterId.eq(userId)))
                .fetch());
    }

    @Override
    public List<PollVoteDto> findAllVoteResponseByPollId(Long pollId) {
        return qf.select(new QPollVoteDto(
                        pollVote.id,
                        pollOption.id,
                        pollOption.label,
                        student.id,
                        student.studentName
                ))
                .from(pollVote)
                .join(pollVote.option, pollOption)
                .join(student).on(student.id.eq(pollVote.voterId)) // 필요 시 leftJoin
                .where(pollOption.poll.id.eq(pollId))
                .orderBy(pollOption.id.asc(), student.studentName.asc())
                .fetch();
    }

    @Override
    public List<PollVoteDto> findAllVoteAnonymousResponseByPollId(Long pollId) {
        return qf.select(new QPollVoteDto(
                        pollVote.id,
                        pollOption.id,
                        pollOption.label
                ))
                .from(pollVote)
                .join(pollVote.option, pollOption)
                .where(pollOption.poll.id.eq(pollId))
                .orderBy(pollOption.id.asc())
                .fetch();
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
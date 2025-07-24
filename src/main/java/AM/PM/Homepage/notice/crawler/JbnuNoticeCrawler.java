package AM.PM.Homepage.notice.crawler;

import AM.PM.Homepage.notice.entity.Notice;
import AM.PM.Homepage.notice.repository.NoticeRepository;
import AM.PM.Homepage.notice.entity.NoticeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JbnuNoticeCrawler implements NoticeCrawler {

    private final NoticeRepository noticeRepository;

    private static final String BASE_LIST_URL = "https://www.jbnu.ac.kr/web/news/notice/sub01.do";
    private static final String DETAIL_BASE_URL = "https://www.jbnu.ac.kr/web/Board/";

    @Override
    public void crawlNotice() {
        List<Notice> notices = crawlNoticeList();
        log.info("공지 크롤링 완료: {}개", notices.size());

        for (Notice notice : notices) {
            if (isAlreadySaved(notice))
                return;

            crawlDetailNotice(notice);
            noticeRepository.save(notice);
            log.info("공지 저장 완료: {}", notice.getTitle());
        }
    }

    public List<Notice> crawlNoticeList() {
        List<Notice> notices = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(BASE_LIST_URL).get();
            Elements rows = doc.select("table.tbl_Board_notice tbody tr");

            for (Element row : rows) {
                Notice notice = parseRowToNotice(row);
                if (notice != null) {
                    notices.add(notice);
                    log.debug("공지 목록 수집 완료: {}", notice.getTitle());
                }
            }

        } catch (IOException e) {
            log.error("전북대 공지 목록 크롤링 실패", e);
        }

        return notices;
    }

    private Notice parseRowToNotice(Element row) {
        Element titleEl = row.selectFirst("a.title");
        if (titleEl == null) return null;

        String title = titleEl.ownText().trim();
        String onclick = titleEl.attr("onclick");  // e.g. pf_DetailMove('190092')
        String id = onclick.replaceAll("\\D+", ""); // 숫자만 추출
        String detailUrl = DETAIL_BASE_URL + id + "/detailView.do?menu=2377";

        return Notice.builder()
                .title(title)
                .url(detailUrl)
                .noticeType(NoticeType.JBNU)
                .build();
    }

    private boolean isAlreadySaved(Notice notice) {
        boolean exists = noticeRepository.existsByTitleAndNoticeType(notice.getTitle(), notice.getNoticeType());
        if (exists) {
            log.debug("이미 저장된 공지입니다: {}", notice.getTitle());
        }
        return exists;
    }

    private void crawlDetailNotice(Notice notice) {
        String content = fetchNoticeContent(notice.getUrl());
        notice.setContent(content);
        log.debug("본문 수집 완료: {}", notice.getTitle());
    }

    private String fetchNoticeContent(String url) {
        try {
            Document detailDoc = Jsoup.connect(url).get();
            Element contentEl = detailDoc.selectFirst("div.com-post-content-01");

            if (contentEl == null) {
                log.warn("본문 태그 없음: {}", url);
                return "(본문 없음)";
            }

            Safelist safelist = Safelist.relaxed()
                    .addTags("br", "p", "ul", "li", "strong", "b", "em", "img")
                    .addAttributes("img", "src", "alt", "title", "width", "height")
                    .addProtocols("img", "src", "http", "https");

            return Jsoup.clean(contentEl.html(), url, safelist);

        } catch (IOException e) {
            log.error("상세 페이지 크롤링 실패: {}", url, e);
            return "(상세 페이지 오류)";
        }
    }
}

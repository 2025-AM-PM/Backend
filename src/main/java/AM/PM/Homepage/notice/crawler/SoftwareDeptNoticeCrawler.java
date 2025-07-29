package AM.PM.Homepage.notice.crawler;

import AM.PM.Homepage.notice.entity.Notice;
import AM.PM.Homepage.notice.repository.NoticeRepository;
import AM.PM.Homepage.notice.entity.NoticeType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SoftwareDeptNoticeCrawler implements NoticeCrawler {

    private final NoticeRepository noticeRepository;

    private static final String BASE_URL = "https://software.jbnu.ac.kr";
    private static final String LIST_URL = BASE_URL + "/bbs/software/527/artclList.do?page=1";

    @Override
    public void crawlNotice() {
        List<Notice> notices = crawlNoticeList();
        log.info("공지 크롤링 완료: {}개", notices.size());

        for (Notice notice : notices) {
            if (isAlreadySaved(notice)) continue;

            crawlDetailNotice(notice);
            noticeRepository.save(notice);
            log.info("공지 저장 완료: {}", notice.getTitle());
        }
    }

    public List<Notice> crawlNoticeList() {
        List<Notice> notices = new ArrayList<>();
        log.debug("크롤 시작: {}", LIST_URL);

        try {
            Document doc = Jsoup.connect(LIST_URL).get();
            Elements rows = doc.select("table.artclTable tbody tr");
            log.debug("목록 행 수: {}", rows.size());

            for (Element row : rows) {
                if (isFixedNotice(row)) continue;

                Notice notice = parseNoticeRow(row);
                if (notice != null) {
                    notices.add(notice);
                    log.debug("공지 목록 수집 완료: {}", notice.getTitle());
                }
            }

        } catch (IOException e) {
            log.error("공지 목록 크롤링 실패: {}", LIST_URL, e);
        }

        return notices;
    }

    private boolean isFixedNotice(Element row) {
        Element numTd = row.selectFirst("td._artclTdNum");
        return numTd != null && numTd.selectFirst("span._artclNotice") != null;
    }

    private Notice parseNoticeRow(Element row) {
        Element linkElement = row.selectFirst("td._artclTdTitle a.artclLinkView");
        if (linkElement == null) return null;

        String title = linkElement.text().replace("새글", "").trim();
        String detailUrl = BASE_URL + linkElement.attr("href");

        return Notice.builder()
                .title(title)
                .url(detailUrl)
                .noticeType(NoticeType.DEPT)
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

    private String fetchNoticeContent(String detailUrl) {
        try {
            Document detailDoc = Jsoup.connect(detailUrl).get();
            Element contentEl = detailDoc.selectFirst("div.artclView");

            if (contentEl == null) {
                log.warn("본문 없음: {}", detailUrl);
                return "(본문 없음)";
            }

            Safelist safelist = Safelist.relaxed()
                    .addTags("br", "p", "ul", "li", "strong", "b", "em", "img")
                    .addAttributes("img", "src", "alt", "title", "width", "height")
                    .addProtocols("img", "src", "http", "https")
                    .removeTags("script", "style");

            return Jsoup.clean(contentEl.html(), detailUrl, safelist);

        } catch (IOException e) {
            log.error("상세 페이지 요청 실패: {}", detailUrl, e);
            return "(상세 페이지 오류)";
        }
    }
}

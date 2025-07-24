package AM.PM.Homepage.notice.crawler;

import AM.PM.Homepage.notice.entity.Notice;
import AM.PM.Homepage.notice.repository.NoticeRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class NoticeCrawlerTest {

    @Autowired
    NoticeRepository repo;

    @Test
    void 학과_공지_크롤링_테스트() {
        SoftwareDeptNoticeCrawler crawler = new SoftwareDeptNoticeCrawler(repo);
        List<Notice> notices = crawler.crawlNoticeList();

        System.out.println("크롤링된 학과 공지 수: " + notices.size());

        notices.forEach(n -> {
            System.out.println("=================================================");
            System.out.println("제목 = " + n.getTitle());
            System.out.println("URL = " + n.getUrl());
        });
    }

    @Test
    void 교내_공지_크롤링_테스트() {
        JbnuNoticeCrawler crawler = new JbnuNoticeCrawler(repo);

        List<Notice> notices = crawler.crawlNoticeList();

        System.out.println("크롤링된 학과 공지 수: " + notices.size());

        notices.forEach(n -> {
            System.out.println("=================================================");
            System.out.println("제목 = " + n.getTitle());
            System.out.println("URL = " + n.getUrl());
        });
    }

    @Test
    void 학과_공지_크롤링_저장_테스트() {
        SoftwareDeptNoticeCrawler crawler = new SoftwareDeptNoticeCrawler(repo);
        crawler.crawlNotice();

        repo.findAll()
                .forEach(n -> {
                    System.out.println("=================================================");
                    System.out.println("제목 = " + n.getTitle());
                    System.out.println("내용 = " + n.getContent());
                    System.out.println("URL = " + n.getUrl());
                });
    }

    @Test
    void 교내_공지_크롤링_저장_테스트() {
        JbnuNoticeCrawler crawler = new JbnuNoticeCrawler(repo);
        crawler.crawlNotice();

        repo.findAll()
                .forEach(n -> {
                    System.out.println("=================================================");
                    System.out.println("제목 = " + n.getTitle());
                    System.out.println("내용 = " + n.getContent());
                    System.out.println("URL = " + n.getUrl());
                });
    }
}

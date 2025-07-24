package AM.PM.Homepage.notice.service;

import AM.PM.Homepage.notice.repository.NoticeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NoticeCrawlerServiceTest {

    @Autowired
    NoticeCrawlerService noticeCrawlerService;

    @Autowired
    NoticeRepository noticeRepository;

    @Test
    void 크롤러_스케줄러_테스트() {
        noticeCrawlerService.runAllCrawlers();

        noticeRepository.findAll()
                .forEach(notice -> {
                    System.out.println("=================================================");
                    System.out.println("제목 = " + notice.getTitle());
                    System.out.println("내용 = " + notice.getContent().substring(0,20));
                    System.out.println("URL = " + notice.getUrl());
                });
    }
}
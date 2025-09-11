package AM.PM.Homepage.notice.service;

import AM.PM.Homepage.notice.crawler.NoticeCrawler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeCrawlerService {

    private final List<NoticeCrawler> crawlers;

    // 매일 오전 4시 실행
    @Scheduled(cron = "0 0 4 * * *")
    public void runAllCrawlers() {
        for (NoticeCrawler crawler : crawlers) {
            log.info("크롤러 실행 시작: {}", crawler.getClass().getSimpleName());

            crawler.crawlNotice();

            log.info("크롤러 실행 종료: {}", crawler.getClass().getSimpleName());
        }
    }
}

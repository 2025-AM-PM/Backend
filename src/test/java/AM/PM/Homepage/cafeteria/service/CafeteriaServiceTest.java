package AM.PM.Homepage.cafeteria.service;

import AM.PM.Homepage.cafeteria.response.CafeteriaResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

//@DisplayName("CafeteriaService 단위 테스트")
class CafeteriaServiceTest {

    private CafeteriaService cafeteriaService;
    private Document sampleDocument;
    private List<LocalDate> expectedWeekDates;

    @BeforeEach
    void setUp() throws IOException {
        cafeteriaService = new CafeteriaService();

        // 테스트용 HTML 파일을 로드하여 Document 객체 생성
        File file = ResourceUtils.getFile("classpath:sample-cafeteria-menu.html");
        String content = new String(Files.readAllBytes(file.toPath()));
        sampleDocument = Jsoup.parse(content);

        // 예상되는 주중 날짜 리스트 생성
        expectedWeekDates = List.of(
                LocalDate.of(2025, 9, 15),
                LocalDate.of(2025, 9, 16),
                LocalDate.of(2025, 9, 17),
                LocalDate.of(2025, 9, 18),
                LocalDate.of(2025, 9, 19)
        );
    }

    @Test
    @DisplayName("날짜 범위 문자열을 LocalDate 리스트로 정확히 파싱한다")
    void parseDateRange_Success() throws Exception {
        // given
        String dateInfo = "2025년 09월 15일 ~ 09월 19일";
        Method parseDateRangeMethod = CafeteriaService.class.getDeclaredMethod("parseDateRange", String.class);
        parseDateRangeMethod.setAccessible(true); // private 메서드 접근 허용

        // when
        @SuppressWarnings("unchecked")
        List<LocalDate> dates = (List<LocalDate>) parseDateRangeMethod.invoke(cafeteriaService, dateInfo);

        // then
        assertThat(dates).isNotNull();
        assertThat(dates).hasSize(5);
        assertThat(dates).isEqualTo(expectedWeekDates);
    }

    @Test
    @DisplayName("진수원 메뉴를 정확히 파싱한다")
    void parseJinsu_Success() throws Exception {
        // given
        Element jinsuSection = sampleDocument.select(".section").get(0);
        Method parseJinsuMethod = CafeteriaService.class.getDeclaredMethod("parseJinsu", Element.class, List.class);
        parseJinsuMethod.setAccessible(true);

        // when
        @SuppressWarnings("unchecked")
        List<CafeteriaResponse> menus = (List<CafeteriaResponse>) parseJinsuMethod.invoke(cafeteriaService, jinsuSection, expectedWeekDates);

        // then
        assertThat(menus).hasSize(7);

        // 월요일 중식 백반 검증
        CafeteriaResponse mondayLunch = menus.get(0);
        assertThat(mondayLunch.getCafeteria()).isEqualTo("진수원");
        assertThat(mondayLunch.getMealType()).isEqualTo("중식");
        assertThat(mondayLunch.getCorner()).isEqualTo("백반");
        assertThat(mondayLunch.getMealDate()).isEqualTo(LocalDate.of(2025, 9, 15));
        assertThat(mondayLunch.getMealName()).isEqualTo("월요일 메뉴\n쌀밥");
        assertThat(mondayLunch.getPrice()).isEqualTo("6,000원");

        // 운영없음 메뉴는 리스트에 포함되지 않는지 검증 (화요일 석식)
        long tuesdayDinnerCount = menus.stream()
                .filter(menu -> menu.getMealDate().equals(LocalDate.of(2025, 9, 16)) && menu.getMealType().equals("석식"))
                .count();
        assertThat(tuesdayDinnerCount).isZero();
    }

    @Test
    @DisplayName("후생관 메뉴를 정확히 파싱한다 (백반, 오므라이스, 돈까스류 포함)")
    void parseHoosaeng_Success() throws Exception {
        // given
        Element hoosaengSection = sampleDocument.select(".section").get(1);
        Method parseHoosaengMethod = CafeteriaService.class.getDeclaredMethod("parseHoosaeng", Element.class, List.class);
        parseHoosaengMethod.setAccessible(true);

        // when
        @SuppressWarnings("unchecked")
        List<CafeteriaResponse> menus = (List<CafeteriaResponse>) parseHoosaengMethod.invoke(cafeteriaService, hoosaengSection, expectedWeekDates);

        // then
        // 백반(4), 오므라이스 기본(5), 오므라이스 특선(3), 돈까스(2*5=10) = 총 22개
        assertThat(menus).hasSize(22);

        // 백반 메뉴 검증 (월요일)
        CafeteriaResponse baekban = menus.stream()
                .filter(menu -> menu.getCorner().equals("백반") && menu.getMealDate().equals(LocalDate.of(2025, 9, 15)))
                .findFirst().orElse(null);
        assertThat(baekban).isNotNull();
        assertThat(baekban.getMealType()).isEqualTo("중식");
        assertThat(baekban.getMealName()).isEqualTo("후생관 월요일 백반");
        assertThat(baekban.getPrice()).isEqualTo("5,000원");

        // 오므라이스류 기본 메뉴 검증 (모든 날짜에 존재)
        List<CafeteriaResponse> omuriceBase = menus.stream()
                .filter(menu -> menu.getMealName().equals("오므라이스(기본)"))
                .collect(Collectors.toList());
        assertThat(omuriceBase).hasSize(5);
        assertThat(omuriceBase.get(0).getPrice()).isEqualTo("5,500원");

        // 오므라이스류 특선 메뉴 검증 (월, 수, 금)
        List<CafeteriaResponse> omuriceSpecial = menus.stream()
                .filter(menu -> menu.getMealName().equals("특선오므라이스"))
                .collect(Collectors.toList());
        assertThat(omuriceSpecial).hasSize(3);
        assertThat(omuriceSpecial.get(0).getPrice()).isEqualTo("6,000원");
        assertThat(omuriceSpecial.stream().map(CafeteriaResponse::getMealDate))
                .containsExactly(LocalDate.of(2025, 9, 15), LocalDate.of(2025, 9, 17), LocalDate.of(2025, 9, 19));

        // 돈까스류 메뉴 검증 (모든 날짜에 등심/치즈 돈까스가 존재)
        List<CafeteriaResponse> cutlets = menus.stream()
                .filter(menu -> menu.getCorner().equals("돈까스류"))
                .collect(Collectors.toList());
        assertThat(cutlets).hasSize(10); // 2종류 * 5일

        long cheeseCutletCount = cutlets.stream().filter(m -> m.getMealName().equals("치즈돈까스")).count();
        assertThat(cheeseCutletCount).isEqualTo(5);
        assertThat(cutlets.get(0).getPrice()).isEqualTo("6,000원"); // 등심돈까스
        assertThat(cutlets.get(1).getPrice()).isEqualTo("7,000원"); // 치즈돈까스
    }

    @Test
    @DisplayName("가격 정보를 포함하는 메뉴 텍스트를 개별 메뉴로 분리한다")
    void splitMenuItems_Success() throws Exception {
        // given
        String rawText = "등심돈까스 6000 / 치즈돈까스 7000 / 곱배기 +1000";
        Method splitMenuItemsMethod = CafeteriaService.class.getDeclaredMethod("splitMenuItems", String.class, String.class, String.class, LocalDate.class, String.class);
        splitMenuItemsMethod.setAccessible(true);
        LocalDate date = LocalDate.of(2025, 9, 15);

        // when
        @SuppressWarnings("unchecked")
        List<CafeteriaResponse> menus = (List<CafeteriaResponse>) splitMenuItemsMethod.invoke(cafeteriaService, "후생관", "중식", "돈까스류", date, rawText);

        // then
        assertThat(menus).hasSize(2);
        assertThat(menus.get(0).getMealName()).isEqualTo("등심돈까스");
        assertThat(menus.get(0).getPrice()).isEqualTo("6,000원");
        assertThat(menus.get(1).getMealName()).isEqualTo("치즈돈까스");
        assertThat(menus.get(1).getPrice()).isEqualTo("7,000원");
    }
}
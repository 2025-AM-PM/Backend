package AM.PM.Homepage.cafeteria.service;

import AM.PM.Homepage.cafeteria.response.CafeteriaResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CafeteriaService {

    private static final String JBNU_COOP_URL = "https://coopjbnu.kr/menu/week_menu.php";
    private static final int DELAY_TIME = 86400000;

    @Scheduled(fixedDelay = DELAY_TIME)
    public List<CafeteriaResponse> updateDailyCafeteriaMenus() throws IOException {
        return scrapeMenus();
    }

    public List<CafeteriaResponse> scrapeMenus() throws IOException {
        List<CafeteriaResponse> allMenus = new ArrayList<>();
        Document doc = Jsoup.connect(JBNU_COOP_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .get();
        Elements sections = doc.select(".contentsArea.WeekMenu .section");

        if (sections.isEmpty()) {
            throw new IOException("메인 콘텐츠('.section')를 찾을 수 없습니다. 웹 페이지 구조가 변경되었을 수 있습니다.");
        }

        String dateInfo = sections.first().select(".ttArea .info").text();
        List<LocalDate> weekDates = parseDateRange(dateInfo);

        if (weekDates.isEmpty()) {
            throw new IOException("유효한 주중 날짜를 파싱할 수 없습니다: " + dateInfo);
        }

        for (Element section : sections) {
            String cafeteria = section.select("h5.title").text();
            if (cafeteria.equals("진수원")) {
                allMenus.addAll(parseJinsu(section, weekDates));
            } else if (cafeteria.equals("후생관")) {
                allMenus.addAll(parseHoosaeng(section, weekDates));
            }
        }
        return allMenus;
    }

    private List<CafeteriaResponse> parseJinsu(Element section, List<LocalDate> weekDates) {
        List<CafeteriaResponse> menus = new ArrayList<>();
        String cafeteria = "진수원";
        Elements rows = section.select("table.tblType03 tbody tr");
        for (Element row : rows) {
            Elements ths = row.select("th");
            if (ths.size() < 2) continue;

            String mealType = ths.get(0).text().trim();
            String cornerAndPrice = ths.get(1).text().trim();
            String corner = cornerAndPrice.split("\\(")[0].trim();

            if (corner.isEmpty()) {
                continue;
            }

            String price = extractPrice(cornerAndPrice);

            Elements tds = row.select("td");
            for (int i = 0; i < tds.size() && i < weekDates.size(); i++) {
                String menuItems = cleanCellText(tds.get(i));
                if (!menuItems.isEmpty() && !menuItems.contains("운영없음")) {
                    menus.add(new CafeteriaResponse(cafeteria, mealType, corner, weekDates.get(i), menuItems, price));
                }
            }
        }
        return menus;
    }

    private List<CafeteriaResponse> parseHoosaeng(Element section, List<LocalDate> weekDates) {
        List<CafeteriaResponse> menus = new ArrayList<>();
        String cafeteria = "후생관";
        Elements rows = section.select("table.tblType03 tbody tr");

        String currentMealType = "";
        List<String> splittableCorners = Arrays.asList("비빔밥", "돈까스류", "김밥", "라면", "우동");

        boolean isNextRowOmuriceSpecial = false;

        for (Element row : rows) {
            Elements headers = row.select("th");

            if (isNextRowOmuriceSpecial && headers.isEmpty()) {
                Elements specialCells = row.select("td");

                int weekDayIndex = 0;
                for (int j = 1; j < specialCells.size() && weekDayIndex < weekDates.size(); j++) {
                    String specialMenu = cleanCellText(specialCells.get(j));
                    if (!specialMenu.isEmpty()) {
                        menus.add(new CafeteriaResponse(cafeteria, currentMealType, "오므라이스류", weekDates.get(weekDayIndex), specialMenu, "6,000원"));
                    }
                    weekDayIndex++;
                }

                isNextRowOmuriceSpecial = false;
                continue;
            }

            if (headers.isEmpty()) continue;

            if (headers.first().hasAttr("rowspan")) {
                currentMealType = headers.first().text().trim();
            }

            String cornerRaw = headers.last().text().trim();
            String generalPrice = extractPrice(cornerRaw);
            String corner = cornerRaw.replaceAll("\\(.*?\\)|\\d+,\\d+원|\\s*Cutlet|\\s*Seaweed Rice Rolls|\\s*Instant noodles|\\s*Udon|\\s", "");

            if (corner.isEmpty()) continue;

            Elements dataCells = row.select("td");

            if (corner.contains("오므라이스류")) {
                Element baseMenuCell = dataCells.first();
                if (baseMenuCell != null) {
                    String baseMenu = cleanCellText(baseMenuCell).split("\n")[0];
                    for (LocalDate date : weekDates) {
                        menus.add(new CafeteriaResponse(cafeteria, currentMealType, corner, date, baseMenu, "5,500원"));
                    }
                }
                isNextRowOmuriceSpecial = true;
                continue;
            }

            for (int j = 0; j < dataCells.size(); j++) {
                Element cell = dataCells.get(j);
                String menuItemsText = cleanCellText(cell).replaceAll("\n", " / ");
                if (menuItemsText.isEmpty() || menuItemsText.contains("운영없음")) continue;

                if (splittableCorners.contains(corner) && cell.hasAttr("colspan")) {
                    for (LocalDate date : weekDates) {
                        menus.addAll(splitMenuItems(cafeteria, currentMealType, corner, date, menuItemsText));
                    }
                    break;
                } else if (j < weekDates.size()) {
                    menus.add(new CafeteriaResponse(cafeteria, currentMealType, corner, weekDates.get(j), menuItemsText.replace(" / ", "\n"), generalPrice));
                }
            }
        }
        return menus;
    }

    private List<CafeteriaResponse> splitMenuItems(String cafeteria, String mealType, String corner, LocalDate date, String rawText) {
        List<CafeteriaResponse> splitMenus = new ArrayList<>();
        Pattern pricePattern = Pattern.compile("(\\d{1,3}(,\\d{3})*|\\d{4,})$");

        for (String item : rawText.split("/")) {
            String trimmedItem = item.trim();
            if (trimmedItem.isEmpty() || trimmedItem.contains("곱배기")) continue;

            Matcher matcher = pricePattern.matcher(trimmedItem);
            if (matcher.find()) {
                String priceStr = matcher.group(0);
                String name = trimmedItem.substring(0, matcher.start()).trim();
                String formattedPrice = String.format("%,d원", Integer.parseInt(priceStr.replace(",", "")));
                splitMenus.add(new CafeteriaResponse(cafeteria, mealType, corner, date, name, formattedPrice));
            }
        }
        return splitMenus;
    }

    private List<LocalDate> parseDateRange(String dateInfo) {
        List<LocalDate> datesInRange = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\d+)월\\s*(\\d+)일\\s*~\\s*\\d+월\\s*(\\d+)일");
        Matcher matcher = pattern.matcher(dateInfo);
        if (matcher.find()) {
            int year = LocalDate.now().getYear();
            int startMonth = Integer.parseInt(matcher.group(1));
            int startDay = Integer.parseInt(matcher.group(2));
            int endDay = Integer.parseInt(matcher.group(3));
            LocalDate startDate = LocalDate.of(year, startMonth, startDay);
            for (int i = 0; i <= (endDay - startDay); i++) datesInRange.add(startDate.plusDays(i));
        }
        return datesInRange.stream()
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .collect(Collectors.toList());
    }

    private String cleanCellText(Element cell) {
        String text = cell.html().replaceAll("(?i)<br\\s*/?>", "\n").replaceAll("<[^>]*>", " ");
        return text.trim().replaceAll("(\\s*\\n\\s*)+", "\n");
    }

    private String extractPrice(String text) {
        Pattern pattern = Pattern.compile("(\\d+,\\d{3}원|\\d{3}원)");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : "정보 없음";
    }
}
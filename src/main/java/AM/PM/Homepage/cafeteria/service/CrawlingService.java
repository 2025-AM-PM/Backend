package AM.PM.Homepage.cafeteria.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

/**
 * 전북대학교 생협 주간 식단표 웹 페이지를 크롤링하여 정제된 메뉴 데이터 리스트를 생성하는 클래스.
 * Spring Service 등에서 재사용 가능하도록 설계됨.
 */
@Service
public class CrawlingService {

    private static final String JBNU_COOP_URL = "https://coopjbnu.kr/menu/week_menu.php";

    public record MenuData(
            String cafeteria,
            String mealType,
            String corner,
            LocalDate mealDate,
            String menuItems,
            String price
    ) {}

    /**
     * 주간 식단표 페이지를 크롤링하여 모든 메뉴 정보를 List<MenuData> 형태로 반환한다.
     * @return 정제된 MenuData 객체의 리스트
     * @throws IOException 네트워크 문제나 웹 페이지 구조 변경 시 발생
     */
    public List<MenuData> scrapeMenus() throws IOException {
        List<MenuData> allMenus = new ArrayList<>();
        Document doc = Jsoup.connect(JBNU_COOP_URL).get();
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

    private List<MenuData> parseJinsu(Element section, List<LocalDate> weekDates) {
        List<MenuData> menus = new ArrayList<>();
        String cafeteria = "진수원";
        Elements rows = section.select("table.tblType03 tbody tr");
        for (Element row : rows) {
            Elements ths = row.select("th");
            if (ths.size() < 2) continue;

            String mealType = ths.get(0).text().trim();
            String cornerAndPrice = ths.get(1).text().trim();
            String corner = cornerAndPrice.split("\\(")[0].trim();
            String price = extractPrice(cornerAndPrice);

            Elements tds = row.select("td");
            for (int i = 0; i < tds.size() && i < weekDates.size(); i++) {
                String menuItems = cleanCellText(tds.get(i));
                if (!menuItems.isEmpty() && !menuItems.contains("운영없음")) {
                    menus.add(new MenuData(cafeteria, mealType, corner, weekDates.get(i), menuItems, price));
                }
            }
        }
        return menus;
    }

    private List<MenuData> parseHoosaeng(Element section, List<LocalDate> weekDates) {
        List<MenuData> menus = new ArrayList<>();
        String cafeteria = "후생관";
        Elements rows = section.select("table.tblType03 tbody tr");

        String currentMealType = "";
        List<String> splittableCorners = Arrays.asList("비빔밥", "돈까스류", "김밥", "라면", "우동");

        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements headers = row.select("th");
            if (headers.isEmpty()) continue;

            if (headers.first().hasAttr("rowspan")) {
                currentMealType = headers.first().text().trim();
            }

            String cornerRaw = headers.last().text().trim();
            String generalPrice = extractPrice(cornerRaw);
            String corner = cornerRaw.replaceAll("\\(.*?\\)|\\d+,\\d+원|\\s*Cutlet|\\s*Seaweed Rice Rolls|\\s*Instant noodles|\\s*Udon|\\s", "");
            
            Elements dataCells = row.select("td");

            if(corner.contains("오므라이스류")) {
                Element baseMenuCell = dataCells.first();
                if (baseMenuCell != null) {
                    String baseMenu = cleanCellText(baseMenuCell).split("\n")[0];
                    for (LocalDate date : weekDates) {
                        menus.add(new MenuData(cafeteria, currentMealType, corner, date, baseMenu, "5,500원"));
                    }
                }
                
                i++;
                if (i < rows.size()) {
                    Elements specialCells = rows.get(i).select("td");
                    for (int j = 0; j < specialCells.size() && j < weekDates.size(); j++) {
                        String specialMenu = cleanCellText(specialCells.get(j));
                        menus.add(new MenuData(cafeteria, currentMealType, corner, weekDates.get(j), specialMenu, "6,000원"));
                    }
                }
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
                    menus.add(new MenuData(cafeteria, currentMealType, corner, weekDates.get(j), menuItemsText.replace(" / ", "\n"), generalPrice));
                }
            }
        }
        return menus;
    }

    private List<MenuData> splitMenuItems(String cafeteria, String mealType, String corner, LocalDate date, String rawText) {
        List<MenuData> splitMenus = new ArrayList<>();
        Pattern pricePattern = Pattern.compile("((\\d,)?\\d{3})$");
        
        for (String item : rawText.split("/")) {
            String trimmedItem = item.trim();
            if (trimmedItem.isEmpty() || trimmedItem.contains("곱배기")) continue;

            Matcher matcher = pricePattern.matcher(trimmedItem);
            if (matcher.find()) {
                String priceStr = matcher.group(0);
                String name = trimmedItem.substring(0, matcher.start()).trim();
                String formattedPrice = String.format("%,d원", Integer.parseInt(priceStr.replace(",", "")));
                splitMenus.add(new MenuData(cafeteria, mealType, corner, date, name, formattedPrice));
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
        return cell.html().replaceAll("(?i)<br\\s*/?>", "\n").replaceAll("<[^>]*>", " ").trim();
    }
    
    private String extractPrice(String text) {
        Pattern pattern = Pattern.compile("(\\d+,\\d{3}원|\\d{3}원)");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : "정보 없음";
    }
}
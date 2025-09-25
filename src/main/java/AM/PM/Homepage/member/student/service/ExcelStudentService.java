package AM.PM.Homepage.member.student.service;

import AM.PM.Homepage.member.student.domain.StudentRole;
import AM.PM.Homepage.member.student.response.StudentResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcelStudentService {

    private final PasswordEncoder passwordEncoder;

    public List<StudentResponse> readExcel() {
        List<StudentResponse> studentResponses = new ArrayList<>();

        try {
            FileInputStream fileInputStream = new FileInputStream("asdf.xlsx");

            try (Workbook workbook = new XSSFWorkbook(fileInputStream)) {

                Sheet sheet = workbook.getSheetAt(0);

                if (sheet == null) {
                    System.out.println("sheet is null");
                }

                for (int i = 1; i < Objects.requireNonNull(sheet).getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }

                    String studentNumber = getCellValueAsString(row.getCell(2));
                    String studentName = getCellValueAsString(row.getCell(1));

//                    studentResponses.add(new StudentResponse(studentNumber, studentName, StudentRole.USER));
                }

            }

        } catch (IOException e) {
            System.out.println("file is null");
            throw new RuntimeException(e);
        }

        return studentResponses;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

}

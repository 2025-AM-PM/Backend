package AM.PM.Homepage;

import AM.PM.Homepage.member.student.response.StudentResponse;
import AM.PM.Homepage.member.student.service.ExcelStudentService;
import AM.PM.Homepage.member.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StudentData implements CommandLineRunner {

    private final StudentService studentService;
    private final ExcelStudentService excelStudentService;

    @Override
    public void run(String... args) throws Exception {

        List<StudentResponse> studentResponses = excelStudentService.readExcel();
        studentService.registerStudent(studentResponses);


    }

}

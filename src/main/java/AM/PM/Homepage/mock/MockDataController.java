package AM.PM.Homepage.mock;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mock")
public class MockDataController {

    private final MockDataService mockDataService;

    @GetMapping("/student")
    public ResponseEntity<String> mockSignup() {
        mockDataService.mockSignup();
        return ResponseEntity.status(HttpStatus.CREATED).body("Student, AlgorithmProfile 데이터 100개 생성 완료");
    }
}

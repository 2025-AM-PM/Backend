package AM.PM.Homepage.exhibit.controller;

import AM.PM.Homepage.exhibit.request.ExhibitCreateRequest;
import AM.PM.Homepage.exhibit.response.ExhibitResponse;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import AM.PM.Homepage.exhibit.service.ExhibitService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exhibits")
public class ExhibitController {

    private final ExhibitService exhibitService;

    @GetMapping
    public ResponseEntity<Page<ExhibitSummaryResponse>> findExhibits(Pageable pageable) {
        Page<ExhibitSummaryResponse> responses = exhibitService.findAllExhibit(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExhibitResponse> findExhibitById(@PathVariable Long id) {
        ExhibitResponse response = exhibitService.findExhibitById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ExhibitSummaryResponse> createExhibit(
            @Valid @RequestPart(name = "request") ExhibitCreateRequest request,
            @RequestPart(name = "files", required = false) List<MultipartFile> imageFiles,
            Authentication authentication
    ) throws FileUploadException {
        Long studentId = 1L; // 임시

        ExhibitSummaryResponse response = exhibitService.createExhibit(request, imageFiles, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

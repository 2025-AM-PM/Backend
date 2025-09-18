package AM.PM.Homepage.exhibit.controller;

import AM.PM.Homepage.exhibit.request.ExhibitCreateRequest;
import AM.PM.Homepage.exhibit.request.ExhibitUpdateRequest;
import AM.PM.Homepage.exhibit.response.ExhibitResponse;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import AM.PM.Homepage.exhibit.service.ExhibitService;
import AM.PM.Homepage.security.UserAuth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

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
            @Valid @RequestBody ExhibitCreateRequest request,
            //@RequestPart(value = "files", required = false) List<MultipartFile> imageFiles,
            @AuthenticationPrincipal UserAuth user
    ) throws FileUploadException {
        //ExhibitSummaryResponse response = exhibitService.createExhibit(request, imageFiles, user.getId());
        ExhibitSummaryResponse response = exhibitService.createExhibit(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{exhibitId}")
    public ResponseEntity<ExhibitSummaryResponse> updateExhibit(
            @PathVariable Long exhibitId,
            @Valid @RequestBody ExhibitUpdateRequest request,
            // @RequestPart(value = "files", required = false) List<MultipartFile> imageFiles,
            @AuthenticationPrincipal UserAuth user
    ) throws FileUploadException {
//        ExhibitSummaryResponse response = exhibitService.updateExhibit(exhibitId, request, imageFiles, user);
        ExhibitSummaryResponse response = exhibitService.updateExhibit(exhibitId, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{exhibitId}")
    public ResponseEntity<Void> deleteExhibit(
            @PathVariable Long exhibitId,
            @AuthenticationPrincipal UserAuth user
    ) {
        exhibitService.deleteExhibit(exhibitId, user);
        return ResponseEntity.noContent().build();
    }
}

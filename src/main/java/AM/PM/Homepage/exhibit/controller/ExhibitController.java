package AM.PM.Homepage.exhibit.controller;

import AM.PM.Homepage.exhibit.request.ExhibitCreateRequest;
import AM.PM.Homepage.exhibit.request.ExhibitUpdateRequest;
import AM.PM.Homepage.exhibit.response.ExhibitResponse;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import AM.PM.Homepage.exhibit.service.ExhibitService;
import AM.PM.Homepage.security.UserAuth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExhibitSummaryResponse> createExhibit(
            @Valid @RequestBody ExhibitCreateRequest request,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        ExhibitSummaryResponse response = exhibitService.createExhibit(request, userAuth.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{exhibitId}")
    @PreAuthorize("isAuthenticated() and @exhibitAuthz.isOwner(#exhibitId, userAuth)")
    public ResponseEntity<ExhibitSummaryResponse> updateExhibit(
            @PathVariable Long exhibitId,
            @Valid @RequestBody ExhibitUpdateRequest request
    ) {
        ExhibitSummaryResponse response = exhibitService.updateExhibit(exhibitId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{exhibitId}")
    @PreAuthorize("isAuthenticated() and @exhibitAuthz.isOwner(#exhibitId, userAuth)")
    public ResponseEntity<Void> deleteExhibit(@PathVariable Long exhibitId) {
        exhibitService.deleteExhibit(exhibitId);
        return ResponseEntity.noContent().build();
    }
}

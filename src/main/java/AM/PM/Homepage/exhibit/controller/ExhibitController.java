package AM.PM.Homepage.exhibit.controller;

import AM.PM.Homepage.exhibit.response.ExhibitResponse;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import AM.PM.Homepage.exhibit.service.ExhibitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}

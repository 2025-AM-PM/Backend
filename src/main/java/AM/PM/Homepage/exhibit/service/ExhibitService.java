package AM.PM.Homepage.exhibit.service;

import AM.PM.Homepage.exhibit.entity.Exhibit;
import AM.PM.Homepage.exhibit.repository.ExhibitImageRepository;
import AM.PM.Homepage.exhibit.repository.ExhibitRepository;
import AM.PM.Homepage.exhibit.response.ExhibitResponse;
import AM.PM.Homepage.exhibit.response.ExhibitSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExhibitService {

    private final ExhibitRepository exhibitRepository;
    private final ExhibitImageRepository exhibitImageRepository;

    public Page<ExhibitSummaryResponse> findAllExhibit(Pageable pageable) {
        Page<Exhibit> exhibits = exhibitRepository.findAll(pageable);
        return exhibits.map(ExhibitSummaryResponse::from);
    }

    public ExhibitResponse findExhibitById(Long id) {
        Exhibit exhibit = exhibitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전시"));

        return ExhibitResponse.from(exhibit);
    }
}

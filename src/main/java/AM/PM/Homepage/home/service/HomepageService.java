package AM.PM.Homepage.home.service;

import AM.PM.Homepage.exhibit.entity.Exhibit;
import AM.PM.Homepage.exhibit.repository.ExhibitRepository;
import AM.PM.Homepage.home.response.HomepageResponse;
import AM.PM.Homepage.notice.entity.Notice;
import AM.PM.Homepage.notice.repository.NoticeRepository;
import AM.PM.Homepage.studygroup.entity.StudyGroup;
import AM.PM.Homepage.studygroup.repository.StudyGroupRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HomepageService {

    private final ExhibitRepository exhibitRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public HomepageResponse getAllRecentPosts() {
        List<Exhibit> exhibits = exhibitRepository.findRecentWithLimit();
        List<StudyGroup> studyGroups = studyGroupRepository.findRecentWithLimit();
        List<Notice> notices = noticeRepository.findRecentWithLimit();
        // TODO: 취업 공고 가져오기

        return HomepageResponse.from(exhibits, studyGroups, notices);
    }
}

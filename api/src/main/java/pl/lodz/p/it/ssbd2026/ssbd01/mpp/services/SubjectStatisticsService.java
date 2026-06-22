package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectStatistics;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectStatisticsService {
    private final SubjectRepository subjectRepository;
    private final ReportsRepository reportsRepository;

    @Retryable(
            retryFor = {JpaSystemException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void recalculateAndUpdateStatistics(UUID subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        Double newSubjectAggregate = reportsRepository.calculateAggregatedAverageForSubject(subjectId);

        if (subject.getSubjectStatistics() == null) {
            SubjectStatistics subjectStatistics = new SubjectStatistics();
            subjectStatistics.setAverageSimilarity(newSubjectAggregate);
            subjectStatistics.setSubject(subject);
            subject.setSubjectStatistics(subjectStatistics);
        } else {
            subject.getSubjectStatistics().setAverageSimilarity(newSubjectAggregate);
        }

        subjectRepository.saveAndFlush(subject);
    }

}

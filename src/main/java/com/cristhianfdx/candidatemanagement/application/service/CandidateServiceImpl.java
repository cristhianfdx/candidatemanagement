package com.cristhianfdx.candidatemanagement.application.service;

import com.cristhianfdx.candidatemanagement.application.dto.CandidateMetricsResponse;
import com.cristhianfdx.candidatemanagement.application.dto.CandidateResponse;
import com.cristhianfdx.candidatemanagement.application.dto.CreateCandidateRequest;
import com.cristhianfdx.candidatemanagement.application.port.CandidatePort;
import com.cristhianfdx.candidatemanagement.domain.exception.CandidateAlreadyExistsException;
import com.cristhianfdx.candidatemanagement.domain.exception.CandidatesNotFoundException;
import com.cristhianfdx.candidatemanagement.domain.exception.DomainException;
import com.cristhianfdx.candidatemanagement.domain.model.Candidate;
import com.cristhianfdx.candidatemanagement.domain.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service implementation for managing candidates and their metrics.
 *
 * <p>This class handles CRUD operations for candidates,
 * calculates statistical metrics (average age, standard deviation),
 * and integrates with Redis for caching and SQS for event-driven updates.</p>
 *
 * <p>Decisions:
 * <ul>
 *   <li>Cache is used to avoid recalculating metrics repeatedly.</li>
 *   <li>Cache is evicted automatically when a new candidate is created.</li>
 *   <li>SQS is used to asynchronously trigger recalculation of metrics in distributed environments.</li>
 *   <li>Domain validation ensures candidate data consistency (age vs. birthdate).</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateServiceImpl implements CandidatePort {

    private final CandidateRepository candidateRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SqsManagementService sqsManagementService;

    private static final String METRICS_CACHE_KEY = "candidateMetrics";

    /**
     * Creates a new candidate in the system.
     * <p>
     * Validates that the provided age matches the calculated age from the birth date.
     * Evicts cached metrics to ensure recalculation on the next request.
     * Publishes a candidate-created event to SQS.
     * </p>
     *
     * @param request DTO containing candidate data
     * @throws DomainException if validation fails or candidate already exists
     */
    @Override
    @CacheEvict(value = METRICS_CACHE_KEY, allEntries = true)
    public void createCandidate(CreateCandidateRequest request) {
        log.info("Creating candidate: {} {}...", request.getFirstname(), request.getLastname());

        validateAge(request.getAge(), request.getBirthDate());

        Candidate candidate = Candidate.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .birthDate(request.getBirthDate())
                .age(request.getAge())
                .build();
        try {
            candidateRepository.save(candidate);
        } catch (DataIntegrityViolationException e) {
            throw new CandidateAlreadyExistsException("Candidate already exists.");
        }

        log.info("Candidate {} {} saved successfully.", request.getFirstname(), request.getLastname());

        sqsManagementService.publishCandidateCreated(String.format("candidate-%s-%s",
                candidate.getId().toString(), candidate.getEmail()));
    }

    /**
     * Retrieves all candidates as DTOs for external API usage.
     *
     * @return list of {@link CandidateResponse} objects
     */
    @Override
    public List<CandidateResponse> getCandidates() {
        return candidateRepository.findAll()
                .stream()
                .map(this::mapCandidateToResponse)
                .toList();
    }

    /**
     * Retrieves candidate metrics (average age, standard deviation).
     * <p>
     * Results are cached in Redis for 10 minutes.
     * If cache is present, it is returned; otherwise, metrics are recalculated.
     * </p>
     *
     * @return {@link CandidateMetricsResponse} containing metrics
     * @throws CandidatesNotFoundException if no candidates exist
     */
    @Override
    @Cacheable(value = METRICS_CACHE_KEY)
    public CandidateMetricsResponse getMetrics() {
        CandidateMetricsResponse cachedMetrics =
                (CandidateMetricsResponse) redisTemplate.opsForValue().get(METRICS_CACHE_KEY);

        if (cachedMetrics != null) {
            log.info("Returning candidate metrics from cache.");
            return cachedMetrics;
        }

        CandidateMetricsResponse metrics = calculateMetricsFromDb();
        redisTemplate.opsForValue().set(METRICS_CACHE_KEY, metrics, 10, TimeUnit.MINUTES);
        return metrics;
    }

    /**
     * Forces recalculation of candidate metrics and updates the Redis cache.
     * <p>Typically triggered by an SQS listener when a candidate is created.</p>
     */
    @Override
    public void recalculateMetrics() {
        CandidateMetricsResponse metrics = calculateMetricsFromDb();
        redisTemplate.opsForValue().set(METRICS_CACHE_KEY, metrics, 10, TimeUnit.MINUTES);
        log.info("Candidate metrics recalculated and cached via SQS trigger.");
    }

    /**
     * Calculates statistical metrics (average, standard deviation) from the DB.
     *
     * @return {@link CandidateMetricsResponse} with calculated values
     * @throws CandidatesNotFoundException if no candidates exist
     */
    private CandidateMetricsResponse calculateMetricsFromDb() {
        List<Candidate> candidates = candidateRepository.findAll();

        if (candidates.isEmpty()) {
            log.warn("No candidates found for metrics calculation.");
            throw new CandidatesNotFoundException("No candidates registered to calculate metrics.");
        }

        IntSummaryStatistics stats = candidates.stream()
                .mapToInt(Candidate::getAge)
                .summaryStatistics();

        double average = stats.getAverage();
        double stdDeviation = calculateStandardDeviation(
                candidates.stream().map(Candidate::getAge).toList(),
                average
        );

        return CandidateMetricsResponse.builder()
                .averageAge(average)
                .ageStandardDeviation(stdDeviation)
                .build();
    }

    /**
     * Calculates the standard deviation of ages.
     *
     * @param ages    list of ages
     * @param average pre-calculated average
     * @return standard deviation value
     */
    private double calculateStandardDeviation(List<Integer> ages, double average) {
        if (ages.size() <= 1) return 0.0;

        return Math.sqrt(
                ages.stream()
                        .mapToDouble(age -> Math.pow(age - average, 2))
                        .average()
                        .orElse(0.0)
        );
    }

    /**
     * Validates that the provided age is consistent with the birth date.
     *
     * @param providedAge age entered in request
     * @param birthDate   candidate's birth date
     * @throws DomainException if the difference between provided and calculated age is greater than 1
     */
    private void validateAge(int providedAge, LocalDate birthDate) {
        int calculatedAge = Period.between(birthDate, LocalDate.now()).getYears();
        if (Math.abs(providedAge - calculatedAge) > 1) {
            throw new DomainException(
                    String.format(
                            "The provided age (%d) does not match the birth date. Calculated age: %d",
                            providedAge, calculatedAge
                    )
            );
        }
    }

    private CandidateResponse mapCandidateToResponse(Candidate candidate) {
        return CandidateResponse.builder()
                .lifeExpectancyDate(candidate.calculateLifeExpectancyDate())
                .birthDate(candidate.getBirthDate())
                .firstname(candidate.getFirstname())
                .lastname(candidate.getLastname())
                .email(candidate.getEmail())
                .age(candidate.getAge())
                .build();
    }
}

package com.cristhianfdx.candidatemanagement.application.service;

import com.cristhianfdx.candidatemanagement.application.dto.CandidateMetricsResponse;
import com.cristhianfdx.candidatemanagement.application.dto.CandidateResponse;
import com.cristhianfdx.candidatemanagement.application.dto.CreateCandidateRequest;
import com.cristhianfdx.candidatemanagement.domain.exception.CandidateAlreadyExistsException;
import com.cristhianfdx.candidatemanagement.domain.exception.CandidatesNotFoundException;
import com.cristhianfdx.candidatemanagement.domain.exception.DomainException;
import com.cristhianfdx.candidatemanagement.domain.model.Candidate;
import com.cristhianfdx.candidatemanagement.domain.repository.CandidateRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CandidateServiceImplTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SqsManagementService sqsManagementService;

    @InjectMocks
    private CandidateServiceImpl subject;

    private CreateCandidateRequest validRequest;

    @Before
    public void setUp() {
        validRequest = new CreateCandidateRequest();
        validRequest.setFirstname("John");
        validRequest.setLastname("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setBirthDate(LocalDate.now().minusYears(30));
        validRequest.setAge(30);
    }

    @Test
    public void shouldSaveCandidateAndPublishSqsEvent() {
        Candidate savedCandidate = Candidate.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .birthDate(validRequest.getBirthDate())
                .age(30)
                .build();

        when(candidateRepository.save(any(Candidate.class))).thenReturn(savedCandidate);

        subject.createCandidate(validRequest);

        verify(candidateRepository, times(1)).save(any(Candidate.class));
        verify(sqsManagementService, times(1)).publishCandidateCreated(contains("candidate-"));
    }

    @Test(expected = DomainException.class)
    public void shouldThrowDomainExceptionForWrongAge() {
        validRequest.setAge(25); // incorrect age
        subject.createCandidate(validRequest);
    }

    @Test(expected = CandidateAlreadyExistsException.class)
    public void shouldThrowExceptionIfCandidateAlreadyExists() {
        when(candidateRepository.save(any(Candidate.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate"));

        subject.createCandidate(validRequest);
    }

    @Test
    public void shouldReturnMappedResponses() {
        Candidate candidate = Candidate.builder()
                .firstname("Jane")
                .lastname("Doe")
                .email("jane.doe@example.com")
                .birthDate(LocalDate.now().minusYears(25))
                .age(25)
                .build();

        when(candidateRepository.findAll()).thenReturn(Collections.singletonList(candidate));

        List<CandidateResponse> result = subject.getCandidates();

        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstname());
    }

    @Test
    public void shouldReturnMetricsFromDb() {
        Candidate candidate1 = Candidate.builder().age(20).build();
        Candidate candidate2 = Candidate.builder().age(30).build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(candidateRepository.findAll()).thenReturn(Arrays.asList(candidate1, candidate2));

        CandidateMetricsResponse metrics = subject.getMetrics();
        assertEquals(25.0, metrics.getAverageAge(), 0.001);
    }

    @Test(expected = CandidatesNotFoundException.class)
    public void shouldThrowExceptionIfCandidatesNotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(candidateRepository.findAll()).thenReturn(Collections.emptyList());

        subject.getMetrics();
    }

    @Test
    public void shouldUpdateCacheAndRecalculateMetrics_() {
        Candidate candidate = Candidate.builder().age(40).build();
        when(candidateRepository.findAll()).thenReturn(Collections.singletonList(candidate));
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));

        subject.recalculateMetrics();

        verify(redisTemplate.opsForValue(), times(1))
                .set(anyString(), any(), eq(10L), eq(java.util.concurrent.TimeUnit.MINUTES));
    }
}
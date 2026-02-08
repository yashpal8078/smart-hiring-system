package com.smarthiring.service;

import com.smarthiring.dto.request.JobRequest;
import com.smarthiring.dto.response.JobResponse;
import com.smarthiring.entity.Job;
import com.smarthiring.entity.User;
import com.smarthiring.enums.JobType;
import com.smarthiring.enums.RoleName;
import com.smarthiring.mapper.JobMapper;
import com.smarthiring.repository.JobRepository;
import com.smarthiring.repository.UserRepository;
import com.smarthiring.util.TestObjectFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobService jobService;

    private User hrUser;
    private Job job;
    private JobRequest jobRequest;

    @BeforeEach
    void setUp() {
        hrUser = TestObjectFactory.createTestUser(1L, "hr@test.com", RoleName.ROLE_HR);
        job = TestObjectFactory.createTestJob(1L, hrUser);

        jobRequest = JobRequest.builder()
                .title("Java Developer")
                .description("Description")
                .requiredSkills("Java")
                .jobType(JobType.FULL_TIME)
                .build();
    }

    @Test
    void createJob_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(hrUser));
        when(jobMapper.toEntity(any(JobRequest.class))).thenReturn(job);
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobMapper.toResponse(any(Job.class))).thenReturn(JobResponse.builder().id(1L).title("Java Developer").build());

        // Act
        JobResponse response = jobService.createJob(jobRequest, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Java Developer", response.getTitle());
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void getJobById_Success() {
        // Arrange
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobMapper.toResponse(any(Job.class))).thenReturn(JobResponse.builder().id(1L).build());

        // Act
        JobResponse response = jobService.getJobById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(jobRepository, times(1)).incrementViewCount(1L); // Verify view count incremented
    }
}
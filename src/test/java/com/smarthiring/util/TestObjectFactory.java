package com.smarthiring.util;

import com.smarthiring.entity.Job;
import com.smarthiring.entity.Role;
import com.smarthiring.entity.User;
import com.smarthiring.enums.JobType;
import com.smarthiring.enums.RoleName;
import com.smarthiring.enums.WorkMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class TestObjectFactory {

    public static User createTestUser(Long id, String email, RoleName roleName) {
        Role role = new Role();
        role.setId(1L);
        role.setName(roleName);

        return User.builder()
                .id(id)
                .email(email)
                .password("encodedPassword")
                .fullName("Test User")
                .phone("1234567890")
                .isActive(true)
                .emailVerified(true)
                .roles(Set.of(role))
                .build();
    }

    public static Job createTestJob(Long id, User postedBy) {
        return Job.builder()
                .id(id)
                .title("Java Developer")
                .description("We need a Java dev")
                .requiredSkills("Java, Spring")
                .experienceMin(2)
                .experienceMax(5)
                .salaryMin(BigDecimal.valueOf(100000))
                .salaryMax(BigDecimal.valueOf(200000))
                .location("Remote")
                .jobType(JobType.FULL_TIME)
                .workMode(WorkMode.REMOTE)
                .postedBy(postedBy)
                .isActive(true)
                .applicationDeadline(LocalDate.now().plusDays(30))
                .viewsCount(0)
                .applicationsCount(0)
                .build();
    }
}
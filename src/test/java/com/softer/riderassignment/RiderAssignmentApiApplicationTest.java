package com.softer.riderassignment;

import com.softer.riderassignment.model.RiderAssignment;
import com.softer.riderassignment.repository.TestH2Repository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class RiderAssignmentApiApplicationTest {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";
    private static RestTemplate restTemplate;

    @Autowired
    private TestH2Repository testH2Repository;

    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    public void setUp() {
        baseUrl = baseUrl.concat(":").concat(port + "").concat("/api/assignments");
    }

    // --------------------------------- Integration Tests ----------------------------------

    @Test
    @Order(1)
    @DisplayName("should get 0 assignments when table is empty")
    public void shouldGetAssignmentsWhenTableIsEmpty() {
        List<RiderAssignment> response = restTemplate.getForObject(baseUrl, List.class);
        assertEquals(0, response.size());
        assertEquals(0, testH2Repository.findAll().size());
    }

    @Test
    @Order(2)
    @DisplayName("should get all assignments when table is not empty")
    @Sql(statements = "INSERT INTO rider_assignments(id, delivery_id, rider_id, status, assigned_at) VALUES (1, 7001, 301, 'ASSIGNED', CURRENT_TIMESTAMP)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldGetAssignmentsWhenTableIsNotEmpty() {
        List<RiderAssignment> response = restTemplate.getForObject(baseUrl, List.class);
        assertEquals(1, response.size());
        assertEquals(1, testH2Repository.findAll().size());
    }









}

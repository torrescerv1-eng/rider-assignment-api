package com.softer.riderassignment;

import com.softer.riderassignment.model.AssignmentStatus;
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

    @Test
    @Order(3)
    @DisplayName("should register a new assignment")
    @Sql(statements = "DELETE FROM rider_assignments", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldAddAssignment() {
        RiderAssignment assignment = new RiderAssignment(null, 7001L, 301L, AssignmentStatus.ASSIGNED, null);
        RiderAssignment response = restTemplate.postForObject(baseUrl, assignment, RiderAssignment.class);
        assertNotNull(response);
        assertEquals(7001L, response.getDeliveryId());
        assertEquals(AssignmentStatus.ASSIGNED, response.getStatus());
        assertEquals(1, testH2Repository.findAll().size());
    }

    @Test
    @Order(4)
    @DisplayName("should get assignment with id=1")
    @Sql(statements = "INSERT INTO rider_assignments(id, delivery_id, rider_id, status, assigned_at) VALUES (1, 7001, 301, 'ASSIGNED', CURRENT_TIMESTAMP)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldFindAssignmentById() {
        RiderAssignment assignment = restTemplate.getForObject(baseUrl + "/{id}", RiderAssignment.class, 1L);
        assertAll(
                () -> assertNotNull(assignment),
                () -> assertEquals(1L, assignment.getId()),
                () -> assertEquals(7001L, assignment.getDeliveryId()),
                () -> assertEquals(301L, assignment.getRiderId())
        );
    }

    @Test
    @Order(5)
    @DisplayName("should update assignment with id=1")
    @Sql(statements = "INSERT INTO rider_assignments(id, delivery_id, rider_id, status, assigned_at) VALUES (1, 7001, 301, 'ASSIGNED', CURRENT_TIMESTAMP)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM rider_assignments", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldUpdateAssignment() {
        RiderAssignment updatedRequest = new RiderAssignment(1L, 7001L, 302L, AssignmentStatus.ACCEPTED, null);
        restTemplate.put(baseUrl + "/{id}", updatedRequest, 1L);

        RiderAssignment assignmentFromDB = testH2Repository.findById(1L).orElse(null);
        assertAll(
                () -> assertNotNull(assignmentFromDB),
                () -> assertEquals(302L, assignmentFromDB.getRiderId()),
                () -> assertEquals(AssignmentStatus.ACCEPTED, assignmentFromDB.getStatus())
        );
    }






}

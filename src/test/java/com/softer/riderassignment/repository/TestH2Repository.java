package com.softer.riderassignment.repository;

import com.softer.riderassignment.model.RiderAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestH2Repository extends JpaRepository<RiderAssignment, Long> {
}

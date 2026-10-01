package com.hospital.repository;

import com.hospital.entity.Doctor;
import com.hospital.enums.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUserEmail(String email);
    List<Doctor> findBySpecialty(Specialty specialty);
    List<Doctor> findByIsAvailableTrue();

    @Query("SELECT d FROM Doctor d JOIN FETCH d.user WHERE d.isAvailable = true")
    List<Doctor> findAllAvailableWithUser();

    @Query("SELECT d FROM Doctor d JOIN FETCH d.user WHERE " +
           "(:specialty IS NULL OR d.specialty = :specialty) AND " +
           "(:availableOnly = false OR d.isAvailable = true)")
    List<Doctor> search(@Param("specialty") Specialty specialty,
                        @Param("availableOnly") boolean availableOnly);

    @Query("SELECT d FROM Doctor d JOIN FETCH d.user WHERE d.id = :id")
    Optional<Doctor> findByIdWithUser(@Param("id") Long id);
}

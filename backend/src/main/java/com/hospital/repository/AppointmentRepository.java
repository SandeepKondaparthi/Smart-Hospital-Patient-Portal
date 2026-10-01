package com.hospital.repository;

import com.hospital.entity.Appointment;
import com.hospital.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user " +
           "JOIN FETCH a.doctor d JOIN FETCH d.user WHERE a.id = :id")
    Optional<Appointment> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user " +
           "JOIN FETCH a.doctor d JOIN FETCH d.user WHERE a.patient.id = :patientId " +
           "ORDER BY a.appointmentDate DESC, a.startTime DESC")
    List<Appointment> findByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user " +
           "JOIN FETCH a.doctor d JOIN FETCH d.user WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentDate = :date ORDER BY a.startTime")
    List<Appointment> findByDoctorIdAndDate(@Param("doctorId") Long doctorId,
                                            @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user " +
           "JOIN FETCH a.doctor d JOIN FETCH d.user WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentDate >= :from ORDER BY a.appointmentDate, a.startTime")
    List<Appointment> findUpcomingByDoctor(@Param("doctorId") Long doctorId,
                                           @Param("from") LocalDate from);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentDate = :date AND a.startTime = :startTime " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    Optional<Appointment> findConflictingSlot(@Param("doctorId") Long doctorId,
                                              @Param("date") LocalDate date,
                                              @Param("startTime") LocalTime startTime);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNotIn(
            Long doctorId, LocalDate date, List<AppointmentStatus> statuses);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentDate = :date AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    long countByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
}

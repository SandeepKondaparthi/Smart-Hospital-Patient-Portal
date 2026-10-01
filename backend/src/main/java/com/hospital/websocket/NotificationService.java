package com.hospital.websocket;

import com.hospital.dto.CommonDtos.AppointmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyAppointmentBooked(AppointmentDto appointment) {
        messagingTemplate.convertAndSend("/topic/appointments", appointment);
        messagingTemplate.convertAndSend(
                "/topic/doctor/" + appointment.getDoctorId() + "/appointments", appointment);
        messagingTemplate.convertAndSend(
                "/topic/patient/" + appointment.getPatientId() + "/appointments", appointment);
    }

    public void notifyAppointmentUpdated(AppointmentDto appointment) {
        messagingTemplate.convertAndSend("/topic/appointments", appointment);
        messagingTemplate.convertAndSend(
                "/topic/doctor/" + appointment.getDoctorId() + "/appointments", appointment);
        messagingTemplate.convertAndSend(
                "/topic/patient/" + appointment.getPatientId() + "/appointments", appointment);
    }
}

package com.example.easy_flow_backend.service;

import com.example.easy_flow_backend.entity.Passenger;
import com.example.easy_flow_backend.entity.StationaryTurnstile;
import com.example.easy_flow_backend.entity.Status;
import com.example.easy_flow_backend.entity.Ticket;
import com.example.easy_flow_backend.error.BadRequestException;
import com.example.easy_flow_backend.repos.PassengersRepo;
import com.example.easy_flow_backend.repos.StationaryTurnstileRepo;
import com.example.easy_flow_backend.repos.TicketRepo;
import com.example.easy_flow_backend.view.RideModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Date;
import java.util.Random;

@Service
public class StationeryTurnstileServiceImplementation implements StationeryTurnstileService {
    @Autowired
    private TicketRepo ticketRepo;
    @Autowired
    private PassengersRepo passengersRepo;
    @Autowired
    private StationaryTurnstileRepo stationaryTurnstileRepo;

    static public void validateRideRequest(@NotNull RideModel rideModel) throws BadRequestException {
        if (rideModel.getMachineId() == null) {
            throw new BadRequestException("The machine id must not null");
        } else if (rideModel.getUsername() == null) {
            throw new BadRequestException("Username not null");
        } else if (rideModel.getTime() == null) {
            throw new BadRequestException("Time must not null");
        } else if (rideModel.getMachineId().isEmpty()) {
            throw new BadRequestException("machine id must not Blank");
        } else if (rideModel.getUsername().isEmpty()) {
            throw new BadRequestException("Username must not Blank");
        } else if (rideModel.getTime().isAfter(LocalTime.now())) {
            throw new BadRequestException("The Date is invalid");
        }
    }


    @Override
    public String inRide(RideModel rideModel) throws BadRequestException {
        validateRideRequest(rideModel);
        if (ticketRepo.existsByPassengerUsernameAndStatus(rideModel.getUsername(), Status.Pending)) {
            throw new BadRequestException("You Can not make Ride as you have pending Request");
        } else if (!passengersRepo.existsByUsernameIgnoreCase(rideModel.getUsername())) {
            throw new BadRequestException("Passenger Not found!");
        } else if (!stationaryTurnstileRepo.existsById(rideModel.getMachineId())) {
            throw new BadRequestException("The machine Id is invalid!");
        }

        Passenger passenger = passengersRepo.findByUsernameIgnoreCase(rideModel.getUsername());

        StationaryTurnstile machine = stationaryTurnstileRepo.findById(rideModel.getMachineId()).get();
        //Todo check for minimum charge
        Ticket pendingTicket = new Ticket(passenger, machine.getStation(), new Date(),
                rideModel.getTime(), Status.Pending);
        ticketRepo.save(pendingTicket);
        return "Success";
    }

    @Override
    public String outRide(RideModel rideModel) throws BadRequestException {
        validateRideRequest(rideModel);

        if (!ticketRepo.existsByPassengerUsernameAndStatus(rideModel.getUsername(), Status.Pending)) {
            throw new BadRequestException("Failed No Binding Tickets");
        } else if (!stationaryTurnstileRepo.existsById(rideModel.getMachineId())) {
            throw new BadRequestException("The machine Id is invalid!");
        }

        StationaryTurnstile machine = stationaryTurnstileRepo.findById(rideModel.getMachineId()).get();
        Ticket ticket = ticketRepo.findByPassengerUsernameAndStatus(rideModel.getUsername(), Status.Pending);
        //Todo calc price

        Random rand = new Random();
        ticket.setPrice(rand.nextDouble());
        ticket.setEndStation(machine.getStation());
        ticket.setEndTime(LocalTime.now());
        ticket.setStatus(Status.Closed);
        ticketRepo.save(ticket);
        return "Success";
    }
}

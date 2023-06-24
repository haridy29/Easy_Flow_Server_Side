package com.example.easy_flow_backend.service.tunstile_services;

import com.example.easy_flow_backend.dto.Models.ForgetTicketModel;
import com.example.easy_flow_backend.dto.Models.RideModel;
import com.example.easy_flow_backend.dto.Views.StationeryMachineView;
import com.example.easy_flow_backend.entity.*;
import com.example.easy_flow_backend.error.BadRequestException;
import com.example.easy_flow_backend.error.NotFoundException;
import com.example.easy_flow_backend.error.ResponseMessage;
import com.example.easy_flow_backend.repos.PassengersRepo;
import com.example.easy_flow_backend.repos.StationaryTurnstileRepo;
import com.example.easy_flow_backend.repos.TripRepo;
import com.example.easy_flow_backend.service.TokenValidationService;
import com.example.easy_flow_backend.service.UserService;
import com.example.easy_flow_backend.service.payment_services.TicketService;
import com.example.easy_flow_backend.service.payment_services.TripService;
import com.example.easy_flow_backend.service.payment_services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class StationeryTurnstileServiceImplementation implements StationeryTurnstileService {
    @Autowired
    private TripRepo tripRepo;
    @Autowired
    private PassengersRepo passengersRepo;
    @Autowired
    private StationaryTurnstileRepo stationaryTurnstileRepo;
    @Autowired
    WalletService walletService;

    @Autowired
    TicketService ticketService;

    @Autowired
    TripService tripService;

    @Autowired
    TokenValidationService tokenValidationService;

    @Autowired
    UserService userService;

    private void rideValidation(String machineUsername) throws BadRequestException {

        //validate authintication
        if (machineUsername == null || machineUsername.equalsIgnoreCase("anonymous")) {
            throw new BadRequestException("Not Authenticated");
        }

        // validate that the machine is Stationary turnstile machine
        if (!stationaryTurnstileRepo.existsByUsernameIgnoreCase(machineUsername)) {
            throw new BadRequestException("Access Denied!");
        }


    }


    @Override
    public ResponseMessage inRide(RideModel rideModel) throws BadRequestException, NotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String machineUsername = auth.getPrincipal().toString();

        rideValidation(machineUsername);


        return tripService.makePendingTrip(rideModel, machineUsername);
    }


    @Override
    public ResponseMessage outRide(RideModel rideModel) throws BadRequestException, NotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String machineUsername = auth.getPrincipal().toString();

        //validate
        rideValidation(machineUsername);

        return tripService.makeFinalTrip(rideModel, machineUsername);
    }


    @Override
    public String getMyStationName(Principal principal) throws BadRequestException {
        if (principal == null)
            throw new BadRequestException("the principal must not be null, may be you are not authenticated");

        StationaryTurnstile stationaryTurnstile = stationaryTurnstileRepo.findUserByUsername(principal.getName());
        if (stationaryTurnstile == null)
            throw new BadRequestException("The user name not valid");
        Station station = stationaryTurnstile.getStation();
        if (station == null) return null;
        return station.getStationName();
    }

    @Override
    public StationeryMachineView findProjectedByUsername(String username) throws NotFoundException {
        if (!stationaryTurnstileRepo.existsByUsername(username))
            throw new NotFoundException("Machine Not found!");
        return stationaryTurnstileRepo.findProjectedByUsername(username);
    }

    @Override
    public List<StationeryMachineView> getMachines() {
        return stationaryTurnstileRepo.findAllProjectedBy();
    }

    @Override
    public ResponseMessage outRideForgetTicket(ForgetTicketModel forgetTicketModel) throws BadRequestException, NotFoundException {
        Passenger passenger=passengersRepo.findPassengerByPhoneNumberAndPin(forgetTicketModel.getPhoneNumber(),forgetTicketModel.getPin());
        if(passenger==null)
            return new ResponseMessage("Invalid phone number or pin", HttpStatus.BAD_REQUEST);
        Trip trip =tripRepo.outRideForgetTicket(passenger.getId());
        if(trip==null)
            return new ResponseMessage("No pending trips found", HttpStatus.BAD_REQUEST);

        RideModel rideModel=new RideModel
                (trip.getId(),trip.getStartStation(),null,forgetTicketModel.getTime());
        return outRide(rideModel);

    }

    @Override
    public List<Turnstile> getAllMachines() {
        return new ArrayList<>(stationaryTurnstileRepo.findAll());
    }

    @Override
    public ResponseMessage deletMachine(String username) throws NotFoundException {
        if (!stationaryTurnstileRepo.existsByUsername(username)) {
            throw new NotFoundException("Invalid username!");
        }
        return userService.deleteUser(username);
    }


}

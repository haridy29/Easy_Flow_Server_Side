package com.example.easy_flow_backend.service.payment_services;

import com.example.easy_flow_backend.dto.Models.RideModel;
import com.example.easy_flow_backend.dto.Views.TripId;
import com.example.easy_flow_backend.entity.*;
import com.example.easy_flow_backend.error.BadRequestException;
import com.example.easy_flow_backend.error.NotFoundException;
import com.example.easy_flow_backend.error.ResponseMessage;
import com.example.easy_flow_backend.repos.MovingTurnstileRepo;
import com.example.easy_flow_backend.repos.StationaryTurnstileRepo;
import com.example.easy_flow_backend.repos.TripRepo;
import com.example.easy_flow_backend.service.passenger_services.PassengerService;
import com.example.easy_flow_backend.service.graph_services.GraphWeightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TripServiceImpl implements TripService {
    @Autowired
    private TripRepo tripRepo;
    @Autowired
    private PassengerService passengerService;
    @Autowired
    private StationaryTurnstileRepo stationaryTurnstileRepo;
    @Autowired
    WalletService walletService;
    @Autowired
    private GraphWeightService graphWeightService;
    @Autowired
    private MovingTurnstileRepo movingTurnstileRepo;
    @Autowired
    TicketService ticketService;


    private void makeOpenTrips(int numOfTrips, String passengerUsername) throws NotFoundException {
        Passenger passenger = passengerService.getPassenger(passengerUsername);
        List<Trip> trips = new ArrayList<>();
        for (int i = 0; i < numOfTrips; i++) {
            Trip trip = new Trip(passenger, Status.Open);
            trips.add(trip);
        }
        System.out.println(numOfTrips);
        System.out.println(trips.toString());
        tripRepo.saveAll(trips);

    }

    @Override
    public List<TripId> getOpenTrips(int numOfTrips, String passengerUsername) throws NotFoundException {
        List<TripId> trips = tripRepo.findAllByPassengerUsernameAndStatus(passengerUsername, Status.Open, TripId.class);
        if (trips.size() == numOfTrips) return trips;
        else if (trips.size() > numOfTrips) {
            return trips.subList(0, numOfTrips);
        } else {
            makeOpenTrips(numOfTrips - trips.size(), passengerUsername);
            trips = tripRepo.findAllByPassengerUsernameAndStatus(passengerUsername, Status.Open, TripId.class);
            return trips;
        }
    }

    void validateTrip(Trip trip) throws NotFoundException {
        if (trip == null || trip.getStatus() != Status.Open) {
            throw new NotFoundException("The Ticket Not valid");
        }
    }

    @Override
    public ResponseMessage makePendingTrip(RideModel rideModel, String machineUsername) throws NotFoundException {

        Trip trip = tripRepo.findById(rideModel.getTripId(), Trip.class);
        validateTrip(trip);
        Passenger passenger = trip.getPassenger();

        StationaryTurnstile machine = stationaryTurnstileRepo.findUserByUsername(machineUsername);


        String ownerId = machine.getOwner().getId();

        double minPrice = ticketService.getMinPrice(ownerId);

        boolean can = walletService.canWithdraw(passenger.getWallet(), minPrice);

        if (can) {
            trip.setStartTurnstile(machine);
            trip.setStartStation(machine.getStation().getStationName());
            trip.setTransportationType(TransportationType.METRO);
            trip.setStartTime(rideModel.getTime());
            trip.setStatus(Status.Pending);

            tripRepo.save(trip);
        } else {
            return new ResponseMessage("No enough money", HttpStatus.OK);
        }
        return new ResponseMessage("Success", HttpStatus.OK);
    }

    //For Stationery TurnStile
    void validateMakeFinalTrip(Trip trip, String inOwnerId) throws NotFoundException, BadRequestException {
        if (trip == null || trip.getStatus() != Status.Pending) {
            throw new NotFoundException("The Ticket Not valid");
        }
        Owner outOwner = trip.getStartTurnstile().getOwner();
        if (!inOwnerId.equals(outOwner.getId())) {
            throw new BadRequestException("Can not ending Trip, Not for the Same Owner!");
        }
    }

    @Override
    public ResponseMessage makeFinalTrip(RideModel rideModel, String machineUsername) throws NotFoundException, BadRequestException {

        StationaryTurnstile machine = stationaryTurnstileRepo.findUserByUsername(machineUsername);

//        Trip trip = tripRepo.findByPassengerUsernameAndStatus(rideModel.getUsername(), Status.Pending);
        Trip trip = tripRepo.findById(rideModel.getTripId(), Trip.class);

        String ownerId = machine.getOwner().getId();

        validateMakeFinalTrip(trip, ownerId);


        double weight = graphWeightService.getOwnerWeight(ownerId, trip.getStartStation(), machine.getStation().getStationName());

        long totalTime = rideModel.getTime().getTime() - trip.getStartTime().getTime();

        double price = ticketService.getPrice(ownerId, weight, totalTime);

        boolean can = walletService.withdraw(trip.getPassenger().getWallet(), price);
        if (can) {
            trip.setPrice(price);
            trip.setEndTurnstile(machine);
            trip.setEndTime(rideModel.getTime());
            trip.setStatus(Status.Closed);
            trip.setEndStation(machine.getStation().getStationName());
            tripRepo.save(trip);
        } else {
            return new ResponseMessage("Can not End Trip", HttpStatus.OK);
        }

        return new ResponseMessage("Success", HttpStatus.OK);

    }

    public ResponseMessage makeTrip(RideModel rideModel, String machineUsername) throws NotFoundException {
        Trip trip = tripRepo.findById(rideModel.getTripId(), Trip.class);
        validateTrip(trip);
        Passenger passenger = trip.getPassenger();

        MovingTurnstile machine = movingTurnstileRepo.findUserByUsername(machineUsername);

        String lineId = machine.getLine().getId();
        String ownerId = machine.getLine().getOwner().getId();
        String startStation = rideModel.getStartStation();
        String endStation = rideModel.getEndStation();

        double weight = graphWeightService.getLineWeight(lineId, startStation, endStation);

        double price = ticketService.getPrice(ownerId, lineId, weight, 0L);

        boolean can = walletService.withdraw(passenger.getWallet(), price);

        if (can) {

            Trip closedTrip = new Trip(passenger, machine, machine, rideModel.getTime(), rideModel.getTime(), TransportationType.BUS, price, Status.Closed, startStation, endStation);

            tripRepo.save(closedTrip);

        } else {
            return new ResponseMessage("No enough money", HttpStatus.OK);
        }

        return new ResponseMessage("Success", HttpStatus.OK);

    }

}

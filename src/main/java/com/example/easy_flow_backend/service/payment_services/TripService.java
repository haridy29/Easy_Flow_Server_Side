package com.example.easy_flow_backend.service.payment_services;

import com.example.easy_flow_backend.dto.Models.RideModel;
import com.example.easy_flow_backend.dto.Views.TripId;
import com.example.easy_flow_backend.error.BadRequestException;
import com.example.easy_flow_backend.error.NotFoundException;
import com.example.easy_flow_backend.error.ResponseMessage;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TripService {



    List<TripId> getOpenTrips(int numOfTrips, String passengerUsername) throws NotFoundException;

    ResponseMessage makePendingTrip(RideModel rideModel, String machineUsername) throws NotFoundException;//stationeryTurnStile

    ResponseMessage makeFinalTrip(RideModel rideModel, String machineUsername) throws NotFoundException, BadRequestException;//stationeryTurnStile

    ResponseMessage makeTrip(RideModel rideModel, String machineUsername) throws NotFoundException;//MovingTurnstile

}

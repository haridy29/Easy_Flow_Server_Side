package com.example.easy_flow_backend.dto.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RideModel {

    public RideModel(String tripId, String startStation, String endStation, Date time) {
        TripId = tripId;
        this.startStation = startStation;
        this.endStation = endStation;
        this.time = time;
    }

    @NotBlank(message = "Trip Id should not be blank")
    @NotNull(message = "Trip Id should not be null")
    String TripId;
    @Min(1)
    int CompanionCount;

    @NotBlank(message = "Start Station should not be blank")
    @NotNull(message = "Start Station should not be null")
    private String startStation;

    @NotBlank(message = "End Station should not be blank")
    @NotNull(message = "End Station should not be null")
    private String endStation;

    @NotNull(message = "Time should not be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    Date time;

}

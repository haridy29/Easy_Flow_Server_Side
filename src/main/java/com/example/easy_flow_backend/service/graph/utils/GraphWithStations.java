package com.example.easy_flow_backend.service.graph.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphWithStations {
    private double[][] graph;
    private List<String> stations;

}

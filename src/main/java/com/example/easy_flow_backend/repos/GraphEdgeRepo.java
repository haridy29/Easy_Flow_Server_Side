package com.example.easy_flow_backend.repos;

import com.example.easy_flow_backend.entity.Graph;
import com.example.easy_flow_backend.entity.GraphEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GraphEdgeRepo extends JpaRepository<GraphEdge, String> {
    //    List<GraphEdge> findAllByGraph(Graph graph);
    List<GraphEdge> findAllByGraph(Graph graph);
}
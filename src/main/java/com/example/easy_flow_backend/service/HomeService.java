package com.example.easy_flow_backend.service;

import com.example.easy_flow_backend.error.NotFoundException;
import com.example.easy_flow_backend.dto.Models.RegisterModel;
import com.example.easy_flow_backend.error.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface HomeService {

    ResponseMessage Register(RegisterModel registerModel) throws NotFoundException;
    ResponseMessage resetPassword(@RequestBody String email) throws NotFoundException;
}

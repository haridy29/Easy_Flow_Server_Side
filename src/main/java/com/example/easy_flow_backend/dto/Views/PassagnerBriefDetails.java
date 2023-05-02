package com.example.easy_flow_backend.dto.Views;

import com.example.easy_flow_backend.entity.Gender;
import com.example.easy_flow_backend.entity.Wallet;

import java.util.Date;

public interface PassagnerBriefDetails extends UserDetails {


    String  getUsername();
    String getType();

    Gender getGender();

    String getPhoneNumber();

    String getEmail();

    Date getBirthDay();


}
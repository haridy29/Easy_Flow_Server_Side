package com.example.easy_flow_backend.dto.Views;

import com.example.easy_flow_backend.entity.Gender;
import com.example.easy_flow_backend.entity.Wallet;

import java.util.Date;

public interface PassagnerDetails extends UserDetails {

    String getFirstName();
    String getLastName();
    String getCity();
     String getType();
    Gender getGender();
     String getPhoneNumber();
     Date getBirthDay();
     boolean getActive();
     Wallet getWallet();
}
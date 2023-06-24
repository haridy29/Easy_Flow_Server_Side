package com.example.easy_flow_backend.service;

import com.example.easy_flow_backend.entity.User;
import com.example.easy_flow_backend.error.NotFoundException;
import com.example.easy_flow_backend.error.ResponseMessage;
import com.example.easy_flow_backend.repos.UserRepositry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepositry userRepositry;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseMessage flipUserActive(String username) throws NotFoundException {
        if (!userRepositry.existsByUsername(username)) {
            throw new NotFoundException("Invalid Username!");
        }
        User user = userRepositry.findUserByUsername(username);
        user.setActive(!user.isActive());
        userRepositry.save(user);
        return new ResponseMessage("Success", HttpStatus.OK);
    }

    public ResponseMessage deleteUser(String username) throws NotFoundException {
        if (!userRepositry.existsByUsername(username)) {
            throw new NotFoundException("Invalid Username!");
        }

        userRepositry.deleteByUsername(username);
        return new ResponseMessage("Success", HttpStatus.OK);
    }

    public void updatePassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepositry.save(user);

    }
}

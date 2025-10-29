package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")

public class UserController
{
    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
}

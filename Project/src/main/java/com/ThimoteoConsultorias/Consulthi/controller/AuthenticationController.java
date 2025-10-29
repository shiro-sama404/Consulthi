package com.ThimoteoConsultorias.Consulthi.controller;

import com.ThimoteoConsultorias.Consulthi.dto.UserDTO;
import com.ThimoteoConsultorias.Consulthi.service.AuthenticationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class AuthenticationController
{
    private AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO)
    {

        boolean isValid;

        if (userDTO.username() != null)
            isValid = authenticationService.checkPasswordByUsername(userDTO.username(), userDTO.rawPassword());
        else if (userDTO.email() != null)
            isValid = authenticationService.checkPasswordByEmail(userDTO.email(), userDTO.rawPassword());
        else
            isValid = false;

        if (isValid)
            return ResponseEntity.ok("Login bem-sucedido");
        else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody UserDTO userDTO)
    {
        return ResponseEntity.ok("Logout bem-sucedido");
    }
}
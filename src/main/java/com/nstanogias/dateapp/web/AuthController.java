package com.nstanogias.dateapp.web;

import com.nstanogias.dateapp.domain.Photo;
import com.nstanogias.dateapp.domain.User;
import com.nstanogias.dateapp.dtos.*;
import com.nstanogias.dateapp.repository.PhotoRepository;
import com.nstanogias.dateapp.repository.UserRepository;
import com.nstanogias.dateapp.security.JwtTokenProvider;
import com.nstanogias.dateapp.service.MapValidationErrorService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private MapValidationErrorService mapValidationErrorService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ModelMapper modelMapper;

    private UserRepository userRepository;
    private PhotoRepository photoRepository;

    public AuthController(UserRepository userRepository, PhotoRepository photoRepository) {
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserForLogin userForLogin, BindingResult result) {

        ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationService(result);
        if (errorMap != null) return errorMap;

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userForLogin.getUsername().toLowerCase(),
                        userForLogin.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        User user = userRepository.findByUsername(userForLogin.getUsername().toLowerCase()).get();
        UserForList userForList = modelMapper.map(user, UserForList.class);
        Optional<Photo> mainPhotoForUser = photoRepository.getMainPhotoForUser(user.getId());
        if (mainPhotoForUser.isPresent()) {
            userForList.setPhotoUrl(mainPhotoForUser.get().getUrl());
        }
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, userForList));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserForRegister userForRegister, BindingResult bindingResult) {

        ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationService(bindingResult);
        if (errorMap != null) return errorMap;

        userForRegister.setUsername(userForRegister.getUsername().toLowerCase());

        if (userRepository.existsByUsername(userForRegister.getUsername().toLowerCase())) {
            return new ResponseEntity(new ApiResponse(false, "Username already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User userToCreate = modelMapper.map(userForRegister, User.class);
        userToCreate.setPassword(passwordEncoder.encode(userForRegister.getPassword()));

        User createdUser = userRepository.save(userToCreate);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{id}")
                .buildAndExpand(createdUser.getId()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
}

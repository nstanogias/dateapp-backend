package com.nstanogias.dateapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nstanogias.dateapp.domain.User;
import com.nstanogias.dateapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class DateappApplication {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(DateappApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(UserRepository userRepository) {
        return args -> {
            // read JSON and load json
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<List<User>> typeReference = new TypeReference<List<User>>() {
            };
            InputStream inputStream = TypeReference.class.getResourceAsStream("/userSeedData.json");
            try {
                List<User> users = mapper.readValue(inputStream, typeReference);
                List<User> usersToSave = users.stream().map(user -> {
                            user.setUsername(user.getUsername().toLowerCase());
                            user.setPassword(passwordEncoder.encode(user.getPassword()));
                            user.getPhotos().get(0).setUser(user);
                            return user;
                        }
                ).collect(Collectors.toList());
                userRepository.saveAll(usersToSave);
                System.out.println("Users Saved!");
            } catch (IOException e) {
                System.out.println("Unable to save users: " + e.getMessage());
            }
        };
    }

}

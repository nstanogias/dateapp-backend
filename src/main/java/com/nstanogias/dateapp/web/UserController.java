package com.nstanogias.dateapp.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nstanogias.dateapp.domain.Like;
import com.nstanogias.dateapp.domain.Photo;
import com.nstanogias.dateapp.domain.User;
import com.nstanogias.dateapp.dtos.ApiResponse;
import com.nstanogias.dateapp.dtos.UserForDetailed;
import com.nstanogias.dateapp.dtos.UserForList;
import com.nstanogias.dateapp.dtos.UserForUpdate;
import com.nstanogias.dateapp.helper.PaginationHeader;
import com.nstanogias.dateapp.helper.UserParams;
import com.nstanogias.dateapp.repository.LikeRepository;
import com.nstanogias.dateapp.repository.PhotoRepository;
import com.nstanogias.dateapp.repository.UserRepository;
import com.nstanogias.dateapp.security.UserPrincipal;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRepository userRepository;
    private LikeRepository likeRepository;
    private PhotoRepository photoRepository;

    public UserController(UserRepository userRepository, LikeRepository likeRepository, PhotoRepository photoRepository) {
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.photoRepository = photoRepository;
    }

    @GetMapping()
    public ResponseEntity<List<UserForList>> userList(@Valid UserParams userParams) throws JsonProcessingException {
        long currentUserId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        User currentUser = userRepository.getOne(currentUserId);
        Sort sort;
        if (userParams.getGender() == null || userParams.getGender().isEmpty()) {
            userParams.setGender(currentUser.getGender().equals("male") ? "female" : "male");
        }
        if (userParams.getOrderBy() != null && !userParams.getOrderBy().isEmpty()) {
            sort = userParams.getOrderBy().equals("created") ? Sort.by("created") : Sort.by("lastActive");
        } else {
            sort = Sort.unsorted();
        }
        Pageable paging = PageRequest.of(userParams.getPageNumber() - 1, userParams.getPageSize(), sort);
        Page<User> users = userRepository.findAllByGender(userParams.getGender(), paging);
        Stream<User> userStream = users.stream();

        if (userParams.getMinAge() != 18 || userParams.getMaxAge() != 99) {
            LocalDate minDob = LocalDate.now().minusYears(userParams.getMaxAge() - 1);
            LocalDate maxDob = LocalDate.now().minusYears(userParams.getMinAge());
            userStream = users.stream().filter(user -> user.getDateOfBirth().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().isAfter(minDob) && user.getDateOfBirth().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().isBefore(maxDob));
        }
        if (userParams.getLikees() != null && userParams.getLikees()) {
            List<Like> likees = likeRepository.findLikeesByUser(currentUserId);
            List<Long> likeesIds = likees.stream().map(like -> like.getLikee()).collect(Collectors.toList());
            userStream = userStream.filter(user -> likeesIds.contains(user.getId()));
        }
        if (userParams.getLikers() != null && userParams.getLikers()) {
            List<Like> likers = likeRepository.findLikersForUser(currentUserId);
            List<Long> likersIds = likers.stream().map(like -> like.getLiker()).collect(Collectors.toList());
            userStream = userStream.filter(user -> likersIds.contains(user.getId()));
        }
        List<UserForList> result = userStream
                .map(user -> {
                    UserForList userForList = modelMapper.map(user, UserForList.class);
                    userForList.setAge(calculateAge(user.getDateOfBirth()));
                    Optional<Photo> mainPhotoForUser = photoRepository.getMainPhotoForUser(user.getId());
                    if (mainPhotoForUser.isPresent()) {
                        userForList.setPhotoUrl(mainPhotoForUser.get().getUrl());
                    }
                    return userForList;
                }).collect(Collectors.toList());

        //convert list to page
        long start = paging.getOffset();
        long end = (start + paging.getPageSize()) > result.size() ? result.size() : (start + paging.getPageSize());
        Page<UserForList> resultPages = new PageImpl<>(result.subList((int) start, (int) end), paging, result.size());

        HttpHeaders headers = new HttpHeaders();
        PaginationHeader paginationHeader = new PaginationHeader(
                userParams.getPageNumber(), userParams.getPageSize(), resultPages.getTotalElements(), resultPages.getTotalPages());
        headers.add("Pagination", objectMapper.writeValueAsString(paginationHeader));
        headers.add("Access-Control-Expose-Headers", "Pagination");
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public UserForDetailed getUser(@PathVariable long id) {
        User user = userRepository.getOne(id);
        UserForDetailed userForDetailed = modelMapper.map(userRepository.findById(id).get(), UserForDetailed.class);
        userForDetailed.setAge(calculateAge(user.getDateOfBirth()));
        Optional<Photo> mainPhotoForUser = photoRepository.getMainPhotoForUser(user.getId());
        if (mainPhotoForUser.isPresent()) {
            userForDetailed.setPhotoUrl(mainPhotoForUser.get().getUrl());
        }
        return userForDetailed;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable long id, UserForUpdate userForUpdate) {
        long currentUserId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        if (currentUserId != id) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User userToUpdate = userRepository.getOne(id);
        userToUpdate.setIntroduction(userForUpdate.getIntroduction());
        userToUpdate.setLookingFor(userForUpdate.getLookingFor());
        userToUpdate.setInterests(userForUpdate.getInterests());
        userToUpdate.setCity(userForUpdate.getCountry());
        userToUpdate.setCountry(userForUpdate.getCountry());
        userRepository.save(userToUpdate);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private int calculateAge(Date date) {
        int age = new Date().getYear() - date.getYear();
        return age;
    }

    @PostMapping("/{id}/like/{recipientId}")
    public ResponseEntity<ApiResponse> likeUser(@PathVariable long id, @PathVariable long recipientId) {
        long currentUserId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        if (currentUserId != id) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Like like = likeRepository.findOneByLikerAndLikee(id, recipientId);

        if (like != null) {
            return new ResponseEntity(new ApiResponse(false, "You already like this user!"), HttpStatus.BAD_REQUEST);
        }

        Like likeToCreate = new Like();
        likeToCreate.setLiker(id);
        likeToCreate.setLikee(recipientId);

        likeRepository.save(likeToCreate);

        return new ResponseEntity(new ApiResponse(true, "Like persisted successfully"), HttpStatus.OK);
    }
}

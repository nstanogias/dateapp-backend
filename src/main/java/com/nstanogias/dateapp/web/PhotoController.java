package com.nstanogias.dateapp.web;

import com.nstanogias.dateapp.domain.Photo;
import com.nstanogias.dateapp.domain.User;
import com.nstanogias.dateapp.dtos.ApiResponse;
import com.nstanogias.dateapp.dtos.PhotoForCreate;
import com.nstanogias.dateapp.dtos.PhotoForReturn;
import com.nstanogias.dateapp.repository.PhotoRepository;
import com.nstanogias.dateapp.repository.UserRepository;
import com.nstanogias.dateapp.security.UserPrincipal;
import com.nstanogias.dateapp.service.CloudinaryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/users/{userId}/photos")
public class PhotoController {

    @Autowired
    private ModelMapper modelMapper;

    private UserRepository userRepository;
    private PhotoRepository photoRepository;

    private CloudinaryService cloudinaryService;

    public PhotoController(UserRepository userRepository, PhotoRepository photoRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping()
    public List<Photo> getPhotos(@PathVariable long userId) {
        return photoRepository.findAllByUser(userId);
    }

    @GetMapping("/{id}")
    public Photo getPhoto(@PathVariable long id) {
        return photoRepository.getOne(id);
    }

    @PostMapping()
    public ResponseEntity<PhotoForReturn> addPhotoForUser(@PathVariable long userId, @RequestParam("file") MultipartFile file) {
        long currentUserId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        if (currentUserId != userId) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User currentUser = userRepository.getOne(userId);

        Map uploadResult = cloudinaryService.uploadFile(file);

        PhotoForCreate photoForCreate = new PhotoForCreate();
        photoForCreate.setUrl(uploadResult.get("url").toString());
        photoForCreate.setPublicId(uploadResult.get("public_id").toString());
        Optional<Photo> mainPhoto = photoRepository.getMainPhotoForUser(userId);
        if (!mainPhoto.isPresent()) {
            photoForCreate.setIsMain(true);
        }
        currentUser.addPhoto(modelMapper.map(photoForCreate, Photo.class));
        currentUser = userRepository.save(currentUser);
        // URI (URL) parameters
        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("userId", userId);
        urlParams.put("id", currentUser.getPhotos().get(currentUser.getPhotos().size() - 1).getId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{userId}/photos/{id}")
                .buildAndExpand(urlParams).toUri();

        PhotoForReturn photoForReturn = modelMapper.map(currentUser.getPhotos().get(currentUser.getPhotos().size() - 1), PhotoForReturn.class);
        return ResponseEntity.created(location).body(photoForReturn);
    }

    @PostMapping("/{id}/setMain")
    public ResponseEntity<ApiResponse> setMain(@PathVariable long userId, @PathVariable long id) {
        long currentUserId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        if (currentUserId != userId) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User currentUser = userRepository.getOne(userId);
        if (!currentUser.getPhotos().stream().anyMatch(photo -> photo.getId() == id)) {
            return new ResponseEntity(new ApiResponse(false, "This is not your photo to delete!"),
                    HttpStatus.UNAUTHORIZED);
        }

        Photo photo = photoRepository.getOne(id);

        if (photo.getIsMain() != null && photo.getIsMain()) {
            return new ResponseEntity(new ApiResponse(false, "This is already the main photo!"),
                    HttpStatus.BAD_REQUEST);
        }

        currentUser.getPhotos().forEach(ph -> ph.setIsMain(false));
        photo.setIsMain(true);
        photoRepository.save(photo);
        return new ResponseEntity(new ApiResponse(true, "Photo is set to main"), HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePhoto(@PathVariable long userId, @PathVariable long id) throws IOException {
        long currentUserId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        if (currentUserId != userId) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User currentUser = userRepository.getOne(userId);
        if (!currentUser.getPhotos().stream().anyMatch(photo -> photo.getId() == id)) {
            return new ResponseEntity(new ApiResponse(false, "This is not your photo to delete!"),
                    HttpStatus.UNAUTHORIZED);
        }

        Photo photo = photoRepository.getOne(id);

        if (photo.getIsMain()) {
            return new ResponseEntity(new ApiResponse(false, "You cannot delete your main photo!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (photo.getPublicId() != null) {
            Map deleteResult = cloudinaryService.destroy(photo.getPublicId());
            if (deleteResult.get("result").equals("ok")) {
                photoRepository.deleteById(id);
            }
        }

        if (photo.getPublicId() == null) {
            photoRepository.deleteById(id);
        }

        return new ResponseEntity(new ApiResponse(true, "Photo deleted successfully"), HttpStatus.NO_CONTENT);
    }

}

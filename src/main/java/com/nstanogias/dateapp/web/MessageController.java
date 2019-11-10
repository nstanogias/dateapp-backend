package com.nstanogias.dateapp.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nstanogias.dateapp.domain.Message;
import com.nstanogias.dateapp.domain.User;
import com.nstanogias.dateapp.dtos.ApiResponse;
import com.nstanogias.dateapp.dtos.MessageForCreate;
import com.nstanogias.dateapp.dtos.MessageToReturn;
import com.nstanogias.dateapp.helper.MessageParams;
import com.nstanogias.dateapp.helper.PaginationHeader;
import com.nstanogias.dateapp.repository.MessageRepository;
import com.nstanogias.dateapp.repository.UserRepository;
import com.nstanogias.dateapp.security.UserPrincipal;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/messages")
public class MessageController {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MessageRepository messageRepository;
    private UserRepository userRepository;

    public MessageController(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public Message getMessage(@PathVariable long userId, @PathVariable long id) {
        return messageRepository.getOne(id);
    }

    @GetMapping()
    public ResponseEntity<?> getMessagesForUser(@PathVariable long userId, MessageParams messageParams) throws JsonProcessingException {
        if (!isAuthorized(userId)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        messageParams.setUserId(userId);
        Sort sort = Sort.by("message_sent").descending();
        Pageable paging = PageRequest.of(messageParams.getPageNumber() - 1, messageParams.getPageSize(), sort);
        Page<Message> messages;
        switch (messageParams.getMessageContainer()) {
            case "Inbox": {
                messages = messageRepository.findInboxMessages(userId, paging);
                break;
            }
            case "Outbox": {
                messages = messageRepository.findOutboxMessages(userId, paging);
                break;
            }
            default: {
                messages = messageRepository.findDefaultMessages(userId, paging);
                break;
            }
        }
        List<Message> content = messages.getContent();
        List<MessageToReturn> result = Collections.emptyList();
        if (content.size() > 0) {
            result = content.stream().map(message -> {
                MessageToReturn messageToReturn = modelMapper.map(message, MessageToReturn.class);
                messageToReturn.setSenderId(message.getSender().getId());
                messageToReturn.setSenderKnownAs(message.getSender().getUsername());
                messageToReturn.setSenderPhotoUrl(message.getSender().getPhotos().stream().filter(photo -> photo.getIsMain()).findFirst().get().getUrl());
                messageToReturn.setRecipientId(message.getRecipient().getId());
                messageToReturn.setRecipientKnownAs(message.getRecipient().getUsername());
                messageToReturn.setRecipientPhotoUrl(message.getRecipient().getPhotos().stream().filter(photo -> photo.getIsMain()).findFirst().get().getUrl());
                return messageToReturn;
            }).collect(Collectors.toList());
        }
        HttpHeaders headers = new HttpHeaders();
        PaginationHeader paginationHeader = new PaginationHeader(
                messageParams.getPageNumber(), messageParams.getPageSize(), messages.getTotalElements(), messages.getTotalPages());
        headers.add("Pagination", objectMapper.writeValueAsString(paginationHeader));
        headers.add("Access-Control-Expose-Headers", "Pagination");
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    @GetMapping("/thread/{recipientId}")
    public ResponseEntity<List<MessageToReturn>> getMessageThread(@PathVariable long userId, @PathVariable long recipientId) {
        if (!isAuthorized(userId)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Message> messageThread = messageRepository.getMessageThread(userId, recipientId);
        List<MessageToReturn> result = messageThread.stream().map(message -> {
                MessageToReturn messageToReturn = modelMapper.map(message, MessageToReturn.class);
                messageToReturn.setSenderId(message.getSender().getId());
                messageToReturn.setSenderKnownAs(message.getSender().getUsername());
                messageToReturn.setSenderPhotoUrl(message.getSender().getPhotos().stream().filter(photo -> photo.getIsMain()).findFirst().get().getUrl());
                messageToReturn.setRecipientId(message.getRecipient().getId());
                messageToReturn.setRecipientKnownAs(message.getRecipient().getUsername());
                messageToReturn.setRecipientPhotoUrl(message.getRecipient().getPhotos().stream().filter(photo -> photo.getIsMain()).findFirst().get().getUrl());
                return messageToReturn;
        }).collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<MessageToReturn> createMessage(@PathVariable long userId, @RequestBody MessageForCreate messageForCreate) {
        if (!isAuthorized(userId)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User sender = userRepository.getOne(userId);

        messageForCreate.setSenderId(userId);
        Optional<User> recipient = this.userRepository.findById(messageForCreate.getRecipientId());

        if (!recipient.isPresent()) {
            return new ResponseEntity(new ApiResponse(false, "Could not find user"), HttpStatus.BAD_REQUEST);
        }

        User receiver = recipient.get();

        Message messageToCreate = modelMapper.map(messageForCreate, Message.class);
        messageToCreate.setIsRead(false);
//        Message createdMessage = messageRepository.save(messageToCreate);
        sender.addSentMessage(messageToCreate);
        receiver.addReceivedMessage(messageToCreate);
        userRepository.save(sender);
//        userRepository.save(receiver);
        // URI (URL) parameters
        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("userId", userId);
        urlParams.put("id", sender.getSentMessages().get(sender.getSentMessages().size() -1).getId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{userId}/messages/{id}")
                .buildAndExpand(urlParams).toUri();

        MessageToReturn messageToReturn = modelMapper.map(sender.getSentMessages().get(sender.getSentMessages().size() -1), MessageToReturn.class);
        messageToReturn.setSenderId(sender.getId());
        messageToReturn.setSenderKnownAs(sender.getUsername());
        messageToReturn.setSenderPhotoUrl(sender.getPhotos().stream().filter(photo -> photo.getIsMain()).findFirst().get().getUrl());
        messageToReturn.setRecipientId(receiver.getId());
        messageToReturn.setRecipientKnownAs(receiver.getUsername());
        messageToReturn.setRecipientPhotoUrl(receiver.getPhotos().stream().filter(photo -> photo.getIsMain()).findFirst().get().getUrl());
        return ResponseEntity.created(location).body(messageToReturn);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable long userId, @PathVariable long id) {
        if (!isAuthorized(userId)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Message message = messageRepository.getOne(id);

        if (message.getSender().getId() == userId) {
            message.setSenderDeleted(true);
        }

        if (message.getRecipient().getId() == userId) {
            message.setRecipientDeleted(true);
        }

        if (message.getRecipientDeleted() && message.getSenderDeleted()) {
            messageRepository.delete(message);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable long userId, @PathVariable long id) {
        if (!isAuthorized(userId)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Message message = messageRepository.getOne(id);

        if (message.getRecipient().getId() != userId) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        message.setIsRead(true);
        message.setDateRead(new Date());

        messageRepository.save(message);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private boolean isAuthorized(long userId) {
        long currentUserId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return userId == currentUserId;
    }
}

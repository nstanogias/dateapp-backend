package com.nstanogias.dateapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String gender;
    private Date dateOfBirth;
    private String password;
    private String knownAs;
    //    @UpdateTimestamp
//    @CreationTimestamp
//    private LocalDateTime createdDate;
    private Date created;
    private Date lastActive;
    @Column(length = 1000)
    private String introduction;
    @Column(length = 1000)
    private String lookingFor;
    private String interests;
    private String city;
    private String country;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Photo> photos;

    @OneToMany(
            mappedBy = "sender",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Message> sentMessages;

    @OneToMany(
            mappedBy = "recipient",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Message> receivedMessages;

    public void addPhoto(Photo photo) {
        photos.add(photo);
        photo.setUser(this);
    }

    public void removePhoto(Photo photo) {
        photos.remove(photo);
        photo.setUser(null);
    }

    public void addSentMessage(Message message) {
        sentMessages.add(message);
        message.setSender(this);
    }

    public void removeSentMessage(Message message) {
        sentMessages.remove(message);
        message.setSender(null);
    }

    public void addReceivedMessage(Message message) {
//        receivedMessages.add(message);
        message.setRecipient(this);
    }

    public void removeReceivedMessage(Message message) {
        receivedMessages.remove(message);
        message.setRecipient(null);
    }
}

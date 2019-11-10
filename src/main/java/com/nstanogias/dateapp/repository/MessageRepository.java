package com.nstanogias.dateapp.repository;

import com.nstanogias.dateapp.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    //Native
    @Query(value = "select * from Message m where m.recipient_id = ?1 and m.recipient_deleted is not true", nativeQuery = true)
    Page<Message> findInboxMessages(@Param("userId") Long userId, Pageable pageable);

    //Native
    @Query(value = "select * from Message m where m.sender_id = ?1 and m.sender_deleted is not true", nativeQuery = true)
    Page<Message> findOutboxMessages(@Param("userId") Long userId, Pageable pageable);

    //Native
    @Query(value = "select * from Message m where m.recipient_id = ?1 and m.recipient_deleted is not true and m.is_read is not true", nativeQuery = true)
    Page<Message> findDefaultMessages(@Param("userId") Long userId, Pageable pageable);

    //Native
    @Query(value = "select * from Message m where m.recipient_id = ?1 and m.recipient_deleted is not true and m.sender_id = ?2 " +
            "or m.recipient_id = ?2 and m.sender_id = ?1 and m.sender_deleted is not true order by m.message_sent desc", nativeQuery = true)
    List<Message> getMessageThread(@Param("userId") Long userId, @Param("recipientId") Long recipientId);
}

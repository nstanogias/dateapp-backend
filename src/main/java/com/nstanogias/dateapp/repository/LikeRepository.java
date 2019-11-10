package com.nstanogias.dateapp.repository;

import com.nstanogias.dateapp.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    //JPQL
    @Query("SELECT l from Like l where l.liker = :userId")
    List<Like> findLikeesByUser(@Param("userId") Long userId);

    //JPQL
    @Query("SELECT l from Like l where l.likee = :userId")
    List<Like> findLikersForUser(@Param("userId") Long userId);

    Like findOneByLikerAndLikee(long liker, long likkee);
}

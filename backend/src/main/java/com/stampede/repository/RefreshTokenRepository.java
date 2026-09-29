package com.stampede.repository;

import com.stampede.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    // remove every refresh token for a person (e.g. "log out of all devices")
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.personId = :personId")
    void deleteByPersonId(@Param("personId") String personId);
}

package com.stampede.repository;

import com.stampede.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, String> {

    Optional<Person> findByEmail(String email);

    boolean existsByEmail(String email);
}

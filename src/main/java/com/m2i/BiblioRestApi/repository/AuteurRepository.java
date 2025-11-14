package com.m2i.BiblioRestApi.repository;


import com.m2i.BiblioRestApi.model.Auteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuteurRepository extends JpaRepository<Auteur, Long> {

    Optional<Auteur> findByEmail(String email);

    boolean existsByEmail(String email);
}

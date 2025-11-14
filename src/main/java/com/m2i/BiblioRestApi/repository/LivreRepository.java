package com.m2i.BiblioRestApi.repository;

import com.m2i.BiblioRestApi.model.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Long> {

    Optional<Livre> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    List<Livre> findByAuteurId(Long auteurId);

    List<Livre> findByTitreContainingIgnoreCase(String titre);

    List<Livre> findByAnneePublicationBetween(Integer anneeMin, Integer anneeMax);
}

package com.m2i.BiblioRestApi.init;

import com.m2i.BiblioRestApi.model.Auteur;
import com.m2i.BiblioRestApi.model.Livre;
import com.m2i.BiblioRestApi.repository.AuteurRepository;
import com.m2i.BiblioRestApi.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Classe d'initialisation des données de test
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AuteurRepository auteurRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Override
    public void run(String... args) throws Exception {

        // Vérifier si les données existent déjà
        if (auteurRepository.count() > 0) {
            System.out.println("Les données existent déjà, initialisation ignorée.");
            return;
        }

        System.out.println("Initialisation des données de test...");

        // Créer des auteurs
        Auteur hugo = Auteur.builder()
                .nom("Hugo")
                .prenom("Victor")
                .email("victor.hugo@example.com")
                .build();
        auteurRepository.save(hugo);

        Auteur camus = Auteur.builder()
                .nom("Camus")
                .prenom("Albert")
                .email("albert.camus@example.com")
                .build();
        auteurRepository.save(camus);

        Auteur austen = Auteur.builder()
                .nom("Austen")
                .prenom("Jane")
                .email("jane.austen@example.com")
                .build();
        auteurRepository.save(austen);

        Auteur orwell = Auteur.builder()
                .nom("Orwell")
                .prenom("George")
                .email("george.orwell@example.com")
                .build();
        auteurRepository.save(orwell);

        Auteur tolkien = Auteur.builder()
                .nom("Tolkien")
                .prenom("J.R.R.")
                .email("jrr.tolkien@example.com")
                .build();
        auteurRepository.save(tolkien);

        // Créer des livres pour Victor Hugo
        Livre miserables = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(hugo)
                .build();
        livreRepository.save(miserables);

        Livre notredame = Livre.builder()
                .titre("Notre-Dame de Paris")
                .isbn("9782070413089")
                .anneePublication(1831)
                .nombreExemplaires(3)
                .auteur(hugo)
                .build();
        livreRepository.save(notredame);

        // Créer des livres pour Albert Camus
        Livre etranger = Livre.builder()
                .titre("L'Étranger")
                .isbn("9782070360024")
                .anneePublication(1942)
                .nombreExemplaires(8)
                .auteur(camus)
                .build();
        livreRepository.save(etranger);

        Livre peste = Livre.builder()
                .titre("La Peste")
                .isbn("9782070360420")
                .anneePublication(1947)
                .nombreExemplaires(4)
                .auteur(camus)
                .build();
        livreRepository.save(peste);

        // Créer des livres pour Jane Austen
        Livre prejudice = Livre.builder()
                .titre("Orgueil et Préjugés")
                .isbn("9782070413805")
                .anneePublication(1813)
                .nombreExemplaires(6)
                .auteur(austen)
                .build();
        livreRepository.save(prejudice);

        // Créer des livres pour George Orwell
        Livre orwell1984 = Livre.builder()
                .titre("1984")
                .isbn("9782070368228")
                .anneePublication(1949)
                .nombreExemplaires(10)
                .auteur(orwell)
                .build();
        livreRepository.save(orwell1984);

        Livre animalFarm = Livre.builder()
                .titre("La Ferme des Animaux")
                .isbn("9782070375165")
                .anneePublication(1945)
                .nombreExemplaires(7)
                .auteur(orwell)
                .build();
        livreRepository.save(animalFarm);

        // Créer des livres pour J.R.R. Tolkien
        Livre hobbit = Livre.builder()
                .titre("Le Hobbit")
                .isbn("9782070612376")
                .anneePublication(1937)
                .nombreExemplaires(5)
                .auteur(tolkien)
                .build();
        livreRepository.save(hobbit);

        Livre seigneur = Livre.builder()
                .titre("Le Seigneur des Anneaux")
                .isbn("9782070612369")
                .anneePublication(1954)
                .nombreExemplaires(3)
                .auteur(tolkien)
                .build();
        livreRepository.save(seigneur);

        System.out.println("Données de test initialisées avec succès !");
        System.out.println("- " + auteurRepository.count() + " auteurs créés");
        System.out.println("- " + livreRepository.count() + " livres créés");
    }
}
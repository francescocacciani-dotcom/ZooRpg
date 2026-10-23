package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;

import java.util.Optional;

/** Salvataggio e ripristino dello zoo, indipendente dal formato. */
public interface ZooSaveRepository {

    void salva(Zoo zoo) throws PersistenzaException;

    /** @return lo zoo salvato, oppure vuoto se non esiste ancora un salvataggio */
    Optional<Zoo> carica() throws PersistenzaException;
}
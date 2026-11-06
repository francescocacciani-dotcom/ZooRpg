package it.unicam.cs.mpgc.rpg130131.persistence;

import java.util.Optional;

/** Salvataggio e ripristino dello stato della partita, indipendente dal formato. */
public interface SaveRepository {

    void salva(StatoPartita stato) throws PersistenzaException;

    /** @return lo stato salvato, oppure vuoto se non esiste ancora un salvataggio */
    Optional<StatoPartita> carica() throws PersistenzaException;
}
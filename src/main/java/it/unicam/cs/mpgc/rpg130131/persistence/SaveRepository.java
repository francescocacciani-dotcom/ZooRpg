package it.unicam.cs.mpgc.rpg130131.persistence;

import java.util.Optional;

/**
 * Interfaccia che definisce il contratto per il salvataggio e il ripristino dello stato di una partita.
 *
 * <p>Il repository può essere implementato in vari modi (file, database, cloud storage) ed è indipendente
 * dal formato di persistenza scelto.</p>
 *
 * <p>L'interfaccia fornisce due metodi principali:</p>
 * <ul>
 *   <li>{@link #salva} salva lo stato corrente della partita nel repository</li>
 *   <li>{@link #carica} ripristina lo stato della partita dal repository, restituendo un Optional vuoto
 *       se nessun salvataggio esiste ancora</li>
 * </ul>
 *
 * @see XMLSaveRepository
 * @see StatoPartita
 */
public interface SaveRepository {

    /**
     * Salva lo stato corrente della partita nel repository.
     *
     * <p>Lo stato da salvare include lo zoo (con tutti i recinti e gli animali) e il giocatore
     * (con posizione e inventario completo).</p>
     *
     * @param stato l'oggetto che contiene tutto ciò che deve essere salvato della partita
     * @throws PersistenzaException se la scrittura del file o dell'archivio fallisce
     */
    void salva(StatoPartita stato) throws PersistenzaException;

    /**
     * Carica e restituisce lo stato salvato precedentemente dalla partita.
     *
     * <p>Se il file di salvataggio non esiste ancora o la ricarica fallisce, restituisce un Optional vuoto.</p>
     *
     * @return un Optional contenente lo StatoPartita caricato, oppure un Optional.empty() se nessun salvataggio esiste
     * @throws PersistenzaException se la lettura del file fallisce o i dati sono corrotti
     */
    Optional<StatoPartita> carica() throws PersistenzaException;
}
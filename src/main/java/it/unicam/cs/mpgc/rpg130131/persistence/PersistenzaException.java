package it.unicam.cs.mpgc.rpg130131.persistence;

/**
 * Eccezione lanciata quando si verifica un errore durante le operazioni di persistenza.
 * Questa eccezione indica che è fallita una lettura, scrittura o validazione dei dati
 * persistenti (XML) associati al gioco RPG Zoo.
 *
 * <p>Le cause tipiche includono:</p>
 * <ul>
 *   <li>File corrotto o non trovabile durante il caricamento dello stato di partita</li>
 *   <li>Dati XML che non corrispondono allo schema XSD previsto</li>
 *   <li>Operazioni di scrittura bloccate da errori I/O</li>
 * </ul>
 */
public class PersistenzaException extends Exception {

    /**
     * Costruttore per creare un'eccezione con un messaggio di errore descrittivo.
     *
     * @param messaggio descrizione dettagliata dell'errore di persistenza verificatosi
     */
    public PersistenzaException(String messaggio) {
        super(messaggio);
    }

    /**
     * Costruttore per creare un'eccezione con il causale throwable associato.
     * Utile quando l'errore è la conseguenza di un'altra eccezione già verificata.
     *
     * @param messaggio descrizione dettagliata dell'errore di persistenza verificatosi
     * @param causa il Throwable che ha causato questa eccezione (se pertinente)
     */
    public PersistenzaException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
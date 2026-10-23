package it.unicam.cs.mpgc.rpg130131.persistence;

/** Errore di lettura, scrittura o validazione dei dati persistenti. */
public class PersistenzaException extends Exception {

    public PersistenzaException(String messaggio) {
        super(messaggio);
    }

    public PersistenzaException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
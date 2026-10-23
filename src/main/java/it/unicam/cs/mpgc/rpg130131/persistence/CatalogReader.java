package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;

/** Sorgente del catalogo di gioco. */
public interface CatalogReader {

    Catalogo leggi() throws PersistenzaException;
}
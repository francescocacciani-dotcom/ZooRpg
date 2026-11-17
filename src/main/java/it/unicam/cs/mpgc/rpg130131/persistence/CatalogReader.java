package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;

/**
 * Interfaccia che definisce il contratto per la lettura del catalogo di gioco.
 *
 * Il catalogo contiene le informazioni sugli animali disponibili nel gioco, inclusi:
 * - specie e caratteristiche degli animali (rarità, peso)
 * - nomi alternativi per ogni animale
 * - strumenti di cattura con relativi bonus
 * - cibi usati come esca con i loro effetti
 *
 * <p>Le implementazioni tipiche includono {@link XMLCatalogReader} che legge il catalogo da un file XML.</p>
 *
 * @see XMLCatalogReader
 */
public interface CatalogReader {

    /**
     * Legge e restituisce l'oggetto {@link Catalogo} completo contenente tutte le informazioni
     * sugli animali, gli strumenti di cattura e i cibi.
     *
     * @return il catalogo popolato con tutti gli animali, strumenti e cibo disponibili nel gioco
     * @throws PersistenzaException se il file del catalogo non può essere letto o contiene dati invalidi
     */
    Catalogo leggi() throws PersistenzaException;
}
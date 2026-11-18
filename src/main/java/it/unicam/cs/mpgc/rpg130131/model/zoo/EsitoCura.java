package it.unicam.cs.mpgc.rpg130131.model.zoo;

/** Esito di un tentativo di cura. */
public enum EsitoCura {
    CURATO,
    GIA_IN_SALUTE,
    /** Il cibo cura solo animali con la vita sopra {@link Zoo#VITA_MINIMA_PER_CIBO}. */
    CIBO_NON_ADATTO,
    NON_CURATIVO,
    /** Non è prodotto dallo zoo: lo usa chi coordina il gioco quando l'oggetto non è nell'inventario. */
    NON_POSSEDUTO
}
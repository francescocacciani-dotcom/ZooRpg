package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;

import java.util.Objects;

/**
 * Record che rappresenta lo stato completo di una partita del gioco RPG Zoo da salvare e ricaricare.
 *
 * <p>Lo stato di partita include:</p>
 * <ul>
 *   <li><strong>Zoo</strong>: l'oggetto Zoo contenente tutti i recinti e gli animali</li>
 *   <li><strong>Giocatore</strong>: il giocatore corrente con posizione e inventario completo</li>
 * </ul>
 *
 * <p>Nell'atto del salvataggio, la mappa 2D è considerata fissa (non viene salvata) e gli animali
 * posizionati sulla mappa non sono inclusi nello stato persistente - solo quelli contenuti nei recinti del zoo.</p>
 *
 * @see Zoo
 * @see Giocatore
 */
public record StatoPartita(Zoo zoo, Giocatore giocatore) {

    /**
     * Costruttore del record con validazione dei parametri.
     *
     * <p>Assicura che sia lo Zoo che il Giocatore siano istanziati correttamente, evitando stati inconsistenti.</p>
     *
     * @param zoo l'oggetto Zoo contenente tutti i recinti e gli animali
     * @param giocatore il giocatore corrente con posizione e inventario
     * @throws NullPointerException se zoo o giocatore sono null
     */
    public StatoPartita {
        Objects.requireNonNull(zoo, "zoo");
        Objects.requireNonNull(giocatore, "giocatore");
    }
}
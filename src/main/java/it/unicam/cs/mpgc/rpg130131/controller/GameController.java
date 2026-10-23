package it.unicam.cs.mpgc.rpg130131.controller;

import it.unicam.cs.mpgc.rpg130131.controller.EsitoTentativo.Tipo;
import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.capture.EngineCattura;
import it.unicam.cs.mpgc.rpg130131.model.capture.RisultatoCattura;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.Direzione;
import it.unicam.cs.mpgc.rpg130131.model.map.Mappa2D;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;
import it.unicam.cs.mpgc.rpg130131.persistence.PersistenzaException;
import it.unicam.cs.mpgc.rpg130131.persistence.ZooSaveRepository;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Coordina una partita: muove il giocatore, gestisce i tentativi di cattura e il salvataggio.
 * Non contiene regole di calcolo né formattazione di messaggi: delega al modello e alla vista.
 */
public class GameController {

    private final Mappa2D mappa;
    private final Giocatore giocatore;
    private final SpawnManager spawnManager;
    private final EngineCattura engine;
    private final ZooSaveRepository repository;
    private final int popolazioneObiettivo;
    private Zoo zoo;

    /**
     * @param popolazioneObiettivo numero di animali che il gioco cerca di mantenere sulla mappa
     * @throws IllegalArgumentException se l'obiettivo è negativo o il giocatore parte fuori dalla mappa
     */
    public GameController(Mappa2D mappa,
                          Giocatore giocatore,
                          Zoo zoo,
                          SpawnManager spawnManager,
                          EngineCattura engine,
                          ZooSaveRepository repository,
                          int popolazioneObiettivo) {
        this.mappa = Objects.requireNonNull(mappa, "mappa");
        this.giocatore = Objects.requireNonNull(giocatore, "giocatore");
        this.zoo = Objects.requireNonNull(zoo, "zoo");
        this.spawnManager = Objects.requireNonNull(spawnManager, "spawnManager");
        this.engine = Objects.requireNonNull(engine, "engine");
        this.repository = Objects.requireNonNull(repository, "repository");
        if (popolazioneObiettivo < 0) {
            throw new IllegalArgumentException("L'obiettivo non può essere negativo: " + popolazioneObiettivo);
        }
        if (!mappa.contiene(giocatore.getPosizione())) {
            throw new IllegalArgumentException("Il giocatore parte fuori dalla mappa");
        }
        this.popolazioneObiettivo = popolazioneObiettivo;
    }

    /** Fa comparire gli animali iniziali. Da chiamare una volta all'avvio della partita. */
    public void inizia() {
        spawnManager.mantieniPopolazione(mappa, popolazioneObiettivo);
    }

    /** @return true se il giocatore si è mosso, false se la destinazione è fuori dalla mappa */
    public boolean muovi(Direzione direzione) {
        Optional<Posizione> arrivo = mappa.destinazione(giocatore.getPosizione(), direzione);
        arrivo.ifPresent(giocatore::spostaA);
        return arrivo.isPresent();
    }

    /** @return l'animale presente nella cella del giocatore, se c'è */
    public Optional<Animale> animaleQui() {
        return spawnManager.animaleIn(giocatore.getPosizione());
    }

    public EsitoTentativo tentaCattura(StrumentoCattura strumento) {
        return tentaCattura(strumento, Cibo.NESSUNA_ESCA);
    }

    /**
     * Tenta di catturare l'animale nella cella del giocatore. Strumento ed esca vengono
     * consumati solo se il tentativo viene effettuato; se fallisce l'animale resta dov'è.
     */
    public EsitoTentativo tentaCattura(StrumentoCattura strumento, Cibo esca) {
        Objects.requireNonNull(strumento, "strumento");
        Objects.requireNonNull(esca, "esca");

        Optional<Animale> presente = animaleQui();
        if (presente.isEmpty()) {
            return new EsitoTentativo(Tipo.NESSUN_ANIMALE, null, 0.0);
        }
        Animale animale = presente.get();

        Inventario inventario = giocatore.getInventario();
        boolean usaEsca = !esca.equals(Cibo.NESSUNA_ESCA);
        if (!inventario.contiene(strumento)) {
            return new EsitoTentativo(Tipo.STRUMENTO_NON_DISPONIBILE, animale, 0.0);
        }
        if (usaEsca && !inventario.contiene(esca)) {
            return new EsitoTentativo(Tipo.ESCA_NON_DISPONIBILE, animale, 0.0);
        }
        if (!zoo.puoAccogliere(animale)) {
            return new EsitoTentativo(Tipo.ZOO_SENZA_POSTO, animale, 0.0);
        }

        inventario.usa(strumento);
        if (usaEsca) {
            inventario.usa(esca);
        }
        RisultatoCattura risultato = engine.tenta(animale, strumento, esca);
        if (!risultato.riuscita()) {
            return new EsitoTentativo(Tipo.FALLITO, animale, risultato.probabilita());
        }

        spawnManager.rimuovi(giocatore.getPosizione());
        zoo.accogli(animale);
        spawnManager.mantieniPopolazione(mappa, popolazioneObiettivo);
        return new EsitoTentativo(Tipo.CATTURATO, animale, risultato.probabilita());
    }

    public void salva() throws PersistenzaException {
        repository.salva(zoo);
    }

    /**
     * Sostituisce lo zoo corrente con quello salvato.
     *
     * @return true se esisteva un salvataggio, false altrimenti (lo zoo resta invariato)
     */
    public boolean carica() throws PersistenzaException {
        Optional<Zoo> salvato = repository.carica();
        salvato.ifPresent(caricato -> this.zoo = caricato);
        return salvato.isPresent();
    }

    public Mappa2D getMappa() {
        return mappa;
    }

    public Giocatore getGiocatore() {
        return giocatore;
    }

    public Zoo getZoo() {
        return zoo;
    }

    /** @return vista non modificabile degli animali presenti sulla mappa */
    public Map<Posizione, Animale> getAvvistamenti() {
        return spawnManager.getAvvistamenti();
    }
}
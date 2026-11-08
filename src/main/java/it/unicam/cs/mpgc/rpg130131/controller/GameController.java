package it.unicam.cs.mpgc.rpg130131.controller;

import it.unicam.cs.mpgc.rpg130131.controller.EsitoTentativo.Tipo;
import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.capture.EngineCattura;
import it.unicam.cs.mpgc.rpg130131.model.capture.PoliticaRicompensa;
import it.unicam.cs.mpgc.rpg130131.model.capture.RisultatoCattura;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.Direzione;
import it.unicam.cs.mpgc.rpg130131.model.map.Mappa2D;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;
import it.unicam.cs.mpgc.rpg130131.persistence.PersistenzaException;
import it.unicam.cs.mpgc.rpg130131.persistence.SaveRepository;
import it.unicam.cs.mpgc.rpg130131.persistence.StatoPartita;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Coordina una partita: muove il giocatore, gestisce i tentativi di cattura e il salvataggio.
 * Non contiene regole di calcolo né formattazione di messaggi: delega al modello e alla vista.
 */
public class GameController {

    private final Mappa2D mappa;
    private final SpawnManager spawnManager;
    private final EngineCattura engine;
    private final SaveRepository repository;
    private final int popolazioneObiettivo;
    private Giocatore giocatore;
    private Zoo zoo;
    private final PoliticaRicompensa ricompensa;

    /**
     * @param popolazioneObiettivo numero di animali che il gioco cerca di mantenere sulla mappa
     * @throws IllegalArgumentException se l'obiettivo è negativo o il giocatore parte fuori dalla mappa
     */
    public GameController(Mappa2D mappa,
                          Giocatore giocatore,
                          Zoo zoo,
                          SpawnManager spawnManager,
                          EngineCattura engine,
                          PoliticaRicompensa ricompensa,
                          SaveRepository repository,
                          int popolazioneObiettivo) {
        this.mappa = Objects.requireNonNull(mappa, "mappa");
        this.giocatore = Objects.requireNonNull(giocatore, "giocatore");
        this.zoo = Objects.requireNonNull(zoo, "zoo");
        this.spawnManager = Objects.requireNonNull(spawnManager, "spawnManager");
        this.engine = Objects.requireNonNull(engine, "engine");
        this.ricompensa = Objects.requireNonNull(ricompensa, "ricompensa");
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
        spawnManager.rimuovi(giocatore.getPosizione());
        zoo.accogli(animale);
        Map<Oggetto, Integer> premio = ricompensa.perCattura(animale);
        premio.forEach(inventario::aggiungi);
        spawnManager.mantieniPopolazione(mappa, popolazioneObiettivo);
        return new EsitoTentativo(Tipo.CATTURATO, animale, risultato.probabilita(), premio);
    }

    /**
     * Probabilità di cattura dell'animale presente nella cella, con strumento ed esca indicati.
     * Non consuma nulla.
     *
     * @return la probabilità, o vuoto se nella cella non c'è un animale
     */
    public OptionalDouble anteprimaProbabilita(StrumentoCattura strumento, Cibo esca) {
        Objects.requireNonNull(strumento, "strumento");
        Objects.requireNonNull(esca, "esca");
        Optional<Animale> presente = animaleQui();
        if (presente.isEmpty()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(engine.probabilita(presente.get(), strumento, esca));
    }

    public void salva() throws PersistenzaException {
        repository.salva(new StatoPartita(zoo, giocatore));
    }

    /**
     * Sostituisce zoo e giocatore correnti con quelli salvati. Chi mostra lo stato (la vista)
     * deve quindi rileggerlo da {@link #getZoo()} e {@link #getGiocatore()} dopo il caricamento.
     *
     * @return true se esisteva un salvataggio, false altrimenti (lo stato resta invariato)
     * @throws PersistenzaException se il salvataggio è illeggibile o la posizione salvata è fuori mappa
     */
    public boolean carica() throws PersistenzaException {
        Optional<StatoPartita> salvato = repository.carica();
        if (salvato.isEmpty()) {
            return false;
        }
        StatoPartita stato = salvato.get();
        Posizione posizione = stato.giocatore().getPosizione();
        if (!mappa.contiene(posizione)) {
            throw new PersistenzaException("La posizione salvata è fuori dalla mappa: " + posizione);
        }
        this.zoo = stato.zoo();
        this.giocatore = stato.giocatore();
        return true;
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
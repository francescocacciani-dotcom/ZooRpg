package it.unicam.cs.mpgc.rpg130131.controller;

import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.capture.CalcolatoreProbabilitaStandard;
import it.unicam.cs.mpgc.rpg130131.model.capture.EngineCattura;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.map.GeneratoreMappa;
import it.unicam.cs.mpgc.rpg130131.model.map.Mappa2D;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;
import it.unicam.cs.mpgc.rpg130131.persistence.CatalogReader;
import it.unicam.cs.mpgc.rpg130131.persistence.PersistenzaException;
import it.unicam.cs.mpgc.rpg130131.persistence.SaveRepository;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Assembla una nuova partita: mappa, giocatore con il kit iniziale, zoo con un recinto per bioma,
 * gestore delle comparse e motore di cattura. Tutte le dipendenze arrivano dal costruttore.
 */
public class GameFactory {

    private static final String NOME_ZOO = "Il mio zoo";

    private final CatalogReader catalogReader;
    private final RegistroSpecie registro;
    private final GeneratoreMappa generatoreMappa;
    private final SaveRepository repository;
    private final ConfigurazioneGioco configurazione;
    private final RandomGenerator random;

    public GameFactory(CatalogReader catalogReader,
                       RegistroSpecie registro,
                       GeneratoreMappa generatoreMappa,
                       SaveRepository repository,
                       ConfigurazioneGioco configurazione,
                       RandomGenerator random) {
        this.catalogReader = Objects.requireNonNull(catalogReader, "catalogReader");
        this.registro = Objects.requireNonNull(registro, "registro");
        this.generatoreMappa = Objects.requireNonNull(generatoreMappa, "generatoreMappa");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.configurazione = Objects.requireNonNull(configurazione, "configurazione");
        this.random = Objects.requireNonNull(random, "random");
    }

    /**
     * Crea una partita pronta da giocare: il giocatore parte al centro della mappa e gli animali
     * iniziali sono già comparsi.
     *
     * @throws PersistenzaException     se il catalogo non è leggibile o non è coerente con le specie registrate
     * @throws IllegalArgumentException se il nome del giocatore è vuoto
     */
    public GameController nuovaPartita(String nomeGiocatore) throws PersistenzaException {
        Catalogo catalogo = catalogReader.leggi();
        Mappa2D mappa = generatoreMappa.genera(configurazione.larghezza(), configurazione.altezza());
        Posizione centro = new Posizione(configurazione.larghezza() / 2, configurazione.altezza() / 2);
        Giocatore giocatore = new Giocatore(nomeGiocatore, centro, kitIniziale(catalogo));

        SpawnManager spawnManager;
        try {
            spawnManager = new SpawnManager(catalogo, registro, random);
        } catch (IllegalArgumentException e) {
            throw new PersistenzaException("Catalogo incoerente con le specie registrate: " + e.getMessage(), e);
        }
        EngineCattura engine = new EngineCattura(new CalcolatoreProbabilitaStandard(), random);

        GameController controller = new GameController(
                mappa, giocatore, zooIniziale(), spawnManager, engine, repository,
                configurazione.popolazione());
        controller.inizia();
        return controller;
    }

    private Inventario kitIniziale(Catalogo catalogo) {
        Inventario inventario = new Inventario();
        catalogo.strumenti().forEach(s -> inventario.aggiungi(s, configurazione.quantitaStrumenti()));
        catalogo.cibi().forEach(c -> inventario.aggiungi(c, configurazione.quantitaCibi()));
        return inventario;
    }

    private Zoo zooIniziale() {
        Zoo zoo = new Zoo(NOME_ZOO);
        for (BiomaEnum bioma : BiomaEnum.values()) {
            zoo.aggiungiRecinto(new Recinto(
                    "Recinto " + bioma.getNomeVisualizzato(), bioma, configurazione.capienzaRecinti()));
        }
        return zoo;
    }
}
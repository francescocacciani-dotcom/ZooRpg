package it.unicam.cs.mpgc.rpg130131.controller;

import it.unicam.cs.mpgc.rpg130131.controller.EsitoTentativo.Tipo;
import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;
import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.capture.EngineCattura;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.catalog.VoceAnimale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.map.Direzione;
import it.unicam.cs.mpgc.rpg130131.model.map.Mappa2D;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;
import it.unicam.cs.mpgc.rpg130131.persistence.XMLZooSaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    private static final StrumentoCattura RETE = new StrumentoCattura("Rete", 0.1);
    private static final Cibo CARNE = new Cibo("Carne", 0.1);
    private static final RandomGenerator DADO_BASSO = () -> 0L;   // il tentativo riesce sempre
    private static final RandomGenerator DADO_ALTO = () -> -1L;   // il tentativo fallisce sempre

    @TempDir
    Path cartella;

    private Giocatore giocatore;
    private SpawnManager spawn;
    private Zoo zoo;
    private GameController controller;

    @BeforeEach
    void setUp() {
        creaController(DADO_BASSO, zooConSavana());
    }

    private Zoo zooConSavana() {
        Zoo nuovo = new Zoo("Zoo di prova");
        nuovo.aggiungiRecinto(new Recinto("Savana", BiomaEnum.SAVANA, 2));
        return nuovo;
    }

    /** Mappa di 3 celle di savana con un solo animale (un leone) e probabilità di cattura fissa al 50%. */
    private void creaController(RandomGenerator dado, Zoo zooIniziale) {
        Mappa2D mappa = new Mappa2D(3, 1, p -> BiomaEnum.SAVANA);
        Catalogo catalogo = new Catalogo(
                List.of(new VoceAnimale("Leone", RaritaEnum.COMUNE, 1, List.of("Leo"))),
                List.of(), List.of());
        RegistroSpecie registro = RegistroSpecie.predefinito();

        spawn = new SpawnManager(catalogo, registro, new Random(1));
        giocatore = new Giocatore("Tester", new Posizione(0, 0), new Inventario());
        zoo = zooIniziale;
        controller = new GameController(
                mappa, giocatore, zoo, spawn,
                new EngineCattura((animale, strumento, esca) -> 0.5, dado),
                new XMLZooSaveRepository(cartella.resolve("zoo.xml"), registro),
                1);
        controller.inizia();
        giocatore.getInventario().aggiungi(RETE, 1);
        portaGiocatoreSuAnimale();
    }

    private void portaGiocatoreSuAnimale() {
        giocatore.spostaA(spawn.getAvvistamenti().keySet().iterator().next());
    }

    @Test
    void iniziaPopolaLaMappa() {
        assertEquals(1, controller.getAvvistamenti().size());
    }

    @Test
    void ilGiocatoreSiMuoveEntroLaMappa() {
        giocatore.spostaA(new Posizione(0, 0));

        assertTrue(controller.muovi(Direzione.EST));
        assertEquals(new Posizione(1, 0), giocatore.getPosizione());
        assertFalse(controller.muovi(Direzione.NORD));
        assertEquals(new Posizione(1, 0), giocatore.getPosizione());
    }

    @Test
    void catturaRiuscita() {
        EsitoTentativo esito = controller.tentaCattura(RETE);

        assertEquals(Tipo.CATTURATO, esito.tipo());
        assertTrue(esito.catturato());
        assertEquals(0.5, esito.probabilita(), 1e-9);
        assertTrue(zoo.getAnimali().contains(esito.animale()));
        assertEquals(0, giocatore.getInventario().quantita(RETE));
        assertEquals(1, controller.getAvvistamenti().size(), "la mappa viene ripopolata");
    }

    @Test
    void catturaFallitaConsumaGliOggettiMaLAnimaleResta() {
        creaController(DADO_ALTO, zooConSavana());

        EsitoTentativo esito = controller.tentaCattura(RETE);

        assertEquals(Tipo.FALLITO, esito.tipo());
        assertTrue(zoo.getAnimali().isEmpty());
        assertEquals(0, giocatore.getInventario().quantita(RETE));
        assertTrue(controller.animaleQui().isPresent());
    }

    @Test
    void ancheLEscaVieneConsumata() {
        giocatore.getInventario().aggiungi(CARNE, 2);

        controller.tentaCattura(RETE, CARNE);

        assertEquals(1, giocatore.getInventario().quantita(CARNE));
    }

    @Test
    void senzaAnimaleNonSiCattura() {
        spawn.rimuovi(giocatore.getPosizione());

        EsitoTentativo esito = controller.tentaCattura(RETE);

        assertEquals(Tipo.NESSUN_ANIMALE, esito.tipo());
        assertNull(esito.animale());
        assertEquals(1, giocatore.getInventario().quantita(RETE));
    }

    @Test
    void senzaStrumentoNonSiCattura() {
        StrumentoCattura trappola = new StrumentoCattura("Trappola", 0.2);

        EsitoTentativo esito = controller.tentaCattura(trappola);

        assertEquals(Tipo.STRUMENTO_NON_DISPONIBILE, esito.tipo());
        assertTrue(zoo.getAnimali().isEmpty());
    }

    @Test
    void senzaEscaNonSiCatturaConEsca() {
        EsitoTentativo esito = controller.tentaCattura(RETE, CARNE);

        assertEquals(Tipo.ESCA_NON_DISPONIBILE, esito.tipo());
        assertEquals(1, giocatore.getInventario().quantita(RETE), "nulla viene consumato");
    }

    @Test
    void zooSenzaPostoRifiutaPrimaDiConsumare() {
        creaController(DADO_BASSO, new Zoo("Zoo senza recinti"));

        EsitoTentativo esito = controller.tentaCattura(RETE);

        assertEquals(Tipo.ZOO_SENZA_POSTO, esito.tipo());
        assertEquals(1, giocatore.getInventario().quantita(RETE));
        assertTrue(controller.animaleQui().isPresent());
    }

    @Test
    void senzaSalvataggioNonSiCaricaNulla() throws Exception {
        assertFalse(controller.carica());
        assertSame(zoo, controller.getZoo());
    }

    @Test
    void salvaECaricaRipristinaLoZoo() throws Exception {
        controller.tentaCattura(RETE);
        Zoo prima = controller.getZoo();
        controller.salva();

        assertTrue(controller.carica());

        assertNotSame(prima, controller.getZoo());
        assertEquals(prima.getStatistiche(), controller.getZoo().getStatistiche());
        assertEquals(1, controller.getZoo().getAnimali().size());
    }

    @Test
    void parametriNonValidiRifiutati() {
        Mappa2D mappa = new Mappa2D(2, 2, p -> BiomaEnum.SAVANA);
        Giocatore fuori = new Giocatore("Tester", new Posizione(5, 5), new Inventario());
        XMLZooSaveRepository repository =
                new XMLZooSaveRepository(cartella.resolve("altro.xml"), RegistroSpecie.predefinito());
        EngineCattura engine = new EngineCattura((a, s, e) -> 0.5);

        assertThrows(IllegalArgumentException.class,
                () -> new GameController(mappa, fuori, zoo, spawn, engine, repository, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new GameController(mappa, giocatore, zoo, spawn, engine, repository, -1));
    }
}
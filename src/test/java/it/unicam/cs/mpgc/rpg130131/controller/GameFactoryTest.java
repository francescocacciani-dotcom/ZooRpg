package it.unicam.cs.mpgc.rpg130131.controller;

import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.map.Direzione;
import it.unicam.cs.mpgc.rpg130131.model.map.GeneratoreMappaARegioni;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import it.unicam.cs.mpgc.rpg130131.persistence.XMLCatalogReader;
import it.unicam.cs.mpgc.rpg130131.persistence.XMLSaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameFactoryTest {

    private final ConfigurazioneGioco configurazione = ConfigurazioneGioco.predefinita();

    @TempDir
    Path cartella;

    private GameFactory factory;

    @BeforeEach
    void setUp() {
        RegistroSpecie registro = RegistroSpecie.predefinito();
        factory = new GameFactory(
                XMLCatalogReader.predefinito(),
                registro,
                new GeneratoreMappaARegioni(),
                new XMLSaveRepository(cartella.resolve("partita.xml"), registro),
                configurazione,
                new Random(42));
    }

    @Test
    void laNuovaPartitaHaMappaEGiocatoreIniziali() throws Exception {
        GameController controller = factory.nuovaPartita("Francesco");

        assertEquals(configurazione.larghezza(), controller.getMappa().getLarghezza());
        assertEquals(configurazione.altezza(), controller.getMappa().getAltezza());
        assertEquals("Francesco", controller.getGiocatore().getNome());
        assertEquals(new Posizione(6, 4), controller.getGiocatore().getPosizione());
    }

    @Test
    void ilGiocatoreParteConIlKit() throws Exception {
        GameController controller = factory.nuovaPartita("Francesco");

        verificaKit(controller);
    }

    @Test
    void zooHaUnRecintoPerBioma() throws Exception {
        GameController controller = factory.nuovaPartita("Francesco");

        assertEquals(BiomaEnum.values().length, controller.getZoo().getRecinti().size());
        for (BiomaEnum bioma : BiomaEnum.values()) {
            assertTrue(controller.getZoo().getRecinti().stream().anyMatch(r -> r.getBioma() == bioma));
        }
        assertTrue(controller.getZoo().getAnimali().isEmpty());
    }

    @Test
    void gliAnimaliInizialiSonoNelLoroBioma() throws Exception {
        GameController controller = factory.nuovaPartita("Francesco");

        assertEquals(configurazione.popolazione(), controller.getAvvistamenti().size());
        controller.getAvvistamenti().forEach((posizione, animale) ->
                assertEquals(animale.getBiomaNativo(), controller.getMappa().getCella(posizione).bioma()));
    }

    @Test
    void laPartitaSiSalvaESiRicarica() throws Exception {
        GameController controller = factory.nuovaPartita("Francesco");
        controller.muovi(Direzione.EST);
        Posizione posizione = controller.getGiocatore().getPosizione();

        controller.salva();
        assertTrue(controller.carica());

        assertEquals("Francesco", controller.getGiocatore().getNome());
        assertEquals(posizione, controller.getGiocatore().getPosizione());
        verificaKit(controller);
    }

    @Test
    void nomeGiocatoreVuotoRifiutato() {
        assertThrows(IllegalArgumentException.class, () -> factory.nuovaPartita("  "));
    }

    @Test
    void configurazioneNonValidaRifiutata() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigurazioneGioco(0, 8, 8, 3, 2, 4));
        assertThrows(IllegalArgumentException.class, () -> new ConfigurazioneGioco(12, 8, 8, 3, 2, -1));
    }

    private void verificaKit(GameController controller) throws Exception {
        Catalogo catalogo = XMLCatalogReader.predefinito().leggi();
        catalogo.strumenti().forEach(s -> assertEquals(configurazione.quantitaStrumenti(),
                controller.getGiocatore().getInventario().quantita(s)));
        catalogo.cibi().forEach(c -> assertEquals(configurazione.quantitaCibi(),
                controller.getGiocatore().getInventario().quantita(c)));
    }
}
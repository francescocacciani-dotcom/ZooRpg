package it.unicam.cs.mpgc.rpg130131.model.shop;

import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NegozioTest {

    private final StrumentoCattura trappola = new StrumentoCattura("Trappola", 0.2);
    private final VoceNegozio inVendita = new VoceNegozio(trappola, 35);
    private final Negozio negozio = new Negozio(List.of(inVendita));
    private Giocatore giocatore;

    @BeforeEach
    void setUp() {
        giocatore = new Giocatore("Tester", new Posizione(0, 0), new Inventario(), 50);
    }

    @Test
    void compraSpendeMonetePerUnaUnita() {
        assertEquals(EsitoAcquisto.ACQUISTATO, negozio.compra(giocatore, inVendita));

        assertEquals(15, giocatore.getMonete());
        assertEquals(1, giocatore.getInventario().quantita(trappola));
    }

    @Test
    void moneteInsufficientiNonCambianoNulla() {
        Giocatore povero = new Giocatore("Povero", new Posizione(0, 0), new Inventario(), 10);

        assertEquals(EsitoAcquisto.MONETE_INSUFFICIENTI, negozio.compra(povero, inVendita));

        assertEquals(10, povero.getMonete());
        assertEquals(0, povero.getInventario().quantita(trappola));
    }

    @Test
    void articoloNonInVendita() {
        VoceNegozio altro = new VoceNegozio(trappola, 1);

        assertEquals(EsitoAcquisto.NON_IN_VENDITA, negozio.compra(giocatore, altro));
        assertEquals(50, giocatore.getMonete());
    }

    @Test
    void ilPrezzoDellAmpliamentoCresceConLaCapienza() {
        Recinto piccolo = new Recinto("Piccolo", BiomaEnum.SAVANA, 4);
        Recinto grande = new Recinto("Grande", BiomaEnum.SAVANA, 8);

        assertEquals(4 * Negozio.PREZZO_PER_POSTO, negozio.prezzoAmpliamento(piccolo));
        assertTrue(negozio.prezzoAmpliamento(grande) > negozio.prezzoAmpliamento(piccolo));
    }

    @Test
    void ampliaIlRecintoESpendeMonete() {
        Recinto recinto = new Recinto("R", BiomaEnum.SAVANA, 4);
        int prezzo = negozio.prezzoAmpliamento(recinto);

        assertEquals(EsitoAcquisto.ACQUISTATO, negozio.ampliaRecinto(giocatore, recinto));

        assertEquals(4 + Negozio.POSTI_PER_AMPLIAMENTO, recinto.getCapienza());
        assertEquals(50 - prezzo, giocatore.getMonete());
        assertTrue(negozio.prezzoAmpliamento(recinto) > prezzo, "il prezzo sale a ogni ampliamento");
    }

    @Test
    void ampliaSenzaMoneteNonCambiaNulla() {
        Recinto recinto = new Recinto("R", BiomaEnum.SAVANA, 10); // costa 100, il giocatore ha 50

        assertEquals(EsitoAcquisto.MONETE_INSUFFICIENTI, negozio.ampliaRecinto(giocatore, recinto));

        assertEquals(10, recinto.getCapienza());
        assertEquals(50, giocatore.getMonete());
    }

    @Test
    void articoliNonModificabili() {
        assertThrows(UnsupportedOperationException.class, () -> negozio.getArticoli().clear());
    }
}
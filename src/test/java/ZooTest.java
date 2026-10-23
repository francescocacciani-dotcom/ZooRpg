package it.unicam.cs.mpgc.rpg130131.model.zoo;

import it.unicam.cs.mpgc.rpg130131.model.animals.*;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ZooTest {

    private Zoo zoo;
    private Recinto savana;
    private Recinto palude;

    @BeforeEach
    void setUp() {
        zoo = new Zoo("Zoo di prova");
        savana = new Recinto("Savana", BiomaEnum.SAVANA, 3);
        palude = new Recinto("Palude", BiomaEnum.PALUDE, 2);
        zoo.aggiungiRecinto(savana);
        zoo.aggiungiRecinto(palude);
    }

    @Test
    void recintoNonValido() {
        assertThrows(IllegalArgumentException.class, () -> new Recinto(" ", BiomaEnum.SAVANA, 2));
        assertThrows(IllegalArgumentException.class, () -> new Recinto("R", BiomaEnum.SAVANA, 0));
    }

    @Test
    void accogliePlaceNelRecintoDelBiomaGiusto() {
        Animale leone = new Leone("Simba", RaritaEnum.RARO, StatoAnimale.SELVATICO);
        assertEquals(Optional.of(savana), zoo.accogli(leone));
        assertTrue(savana.getAnimali().contains(leone));
        assertEquals(2, savana.getPostiLiberi());
    }

    @Test
    void rifiutaSeNonCeRecintoAdatto() {
        Zoo soloPalude = new Zoo("Solo palude");
        soloPalude.aggiungiRecinto(palude);
        Animale leone = new Leone("Simba", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        assertFalse(soloPalude.puoAccogliere(leone));
        assertEquals(Optional.empty(), soloPalude.accogli(leone));
    }

    @Test
    void rifiutaSeRecintoPieno() {
        for (int i = 0; i < 3; i++) {
            zoo.accogli(new Giraffa("G" + i, RaritaEnum.COMUNE, StatoAnimale.SELVATICO));
        }
        assertTrue(savana.isPieno());
        assertEquals(Optional.empty(),
                zoo.accogli(new Leone("Extra", RaritaEnum.COMUNE, StatoAnimale.SELVATICO)));
    }

    @Test
    void nonAccoglieDueVolteLoStessoAnimale() {
        Animale capibara = new Capibara("Cappy", RaritaEnum.COMUNE, StatoAnimale.IN_PERICOLO);
        assertTrue(zoo.accogli(capibara).isPresent());
        assertFalse(zoo.puoAccogliere(capibara));
        assertThrows(IllegalStateException.class, () -> palude.aggiungi(capibara));
    }

    @Test
    void rimuoviLiberaIlPosto() {
        Animale capibara = new Capibara("Cappy", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        zoo.accogli(capibara);
        assertTrue(palude.rimuovi(capibara));
        assertEquals(2, palude.getPostiLiberi());
        assertFalse(palude.rimuovi(capibara));
    }

    @Test
    void statisticheCorrette() {
        zoo.accogli(new Leone("Simba", RaritaEnum.RARO, StatoAnimale.SELVATICO));
        zoo.accogli(new Giraffa("Gigi", RaritaEnum.COMUNE, StatoAnimale.SELVATICO));
        zoo.accogli(new Capibara("Cappy", RaritaEnum.EPICO, StatoAnimale.IN_PERICOLO));

        StatisticheZoo s = zoo.getStatistiche();
        assertEquals(3, s.totaleAnimali());
        assertEquals(5, s.capienzaTotale());
        assertEquals(1L, s.animaliPerSpecie().get("Leone"));
        assertEquals(1L, s.animaliPerRarita().get(RaritaEnum.EPICO));
        assertEquals(3 + 1 + 7, s.punteggio());
    }

    @Test
    void listeEsposteNonModificabili() {
        assertThrows(UnsupportedOperationException.class, () -> zoo.getRecinti().clear());
        assertThrows(UnsupportedOperationException.class, () -> savana.getAnimali().clear());
    }
}
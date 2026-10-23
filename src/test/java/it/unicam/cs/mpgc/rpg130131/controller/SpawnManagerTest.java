package it.unicam.cs.mpgc.rpg130131.controller;

import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;
import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.catalog.VoceAnimale;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.map.Mappa2D;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SpawnManagerTest {

    private final RegistroSpecie registro = RegistroSpecie.predefinito();
    private final VoceAnimale capibara = new VoceAnimale("Capibara", RaritaEnum.COMUNE, 1, List.of("Cappy"));
    private final VoceAnimale leone = new VoceAnimale("Leone", RaritaEnum.RARO, 1, List.of("Leo"));

    // 4 celle in fila: x = 0 e 1 savana, x = 2 e 3 palude
    private final Mappa2D mappa = new Mappa2D(4, 1, p -> p.x() < 2 ? BiomaEnum.SAVANA : BiomaEnum.PALUDE);

    private SpawnManager manager(VoceAnimale... voci) {
        return new SpawnManager(new Catalogo(List.of(voci), List.of(), List.of()), registro, new Random(7));
    }

    @Test
    void glianimaliCompaionoNelBiomaNativo() {
        SpawnManager manager = manager(capibara);

        assertEquals(2, manager.popola(mappa, 2));
        manager.getAvvistamenti().forEach((posizione, animale) -> {
            assertTrue(posizione.x() >= 2, "Un capibara deve stare in palude");
            assertEquals("Capibara", animale.getSpecie());
        });
    }

    @Test
    void senzaCelleLibereNonCompareNulla() {
        SpawnManager manager = manager(capibara);
        manager.popola(mappa, 2);

        assertEquals(0, manager.popola(mappa, 1));
        assertEquals(2, manager.getAvvistamenti().size());
    }

    @Test
    void mantieniPopolazioneRiempieSoloLaDifferenza() {
        SpawnManager manager = manager(leone);

        assertEquals(2, manager.mantieniPopolazione(mappa, 2));
        Posizione prima = manager.getAvvistamenti().keySet().iterator().next();
        manager.rimuovi(prima);

        assertEquals(1, manager.mantieniPopolazione(mappa, 2));
        assertEquals(0, manager.mantieniPopolazione(mappa, 2));
    }

    @Test
    void animaleInERimuovi() {
        SpawnManager manager = manager(leone);
        manager.popola(mappa, 1);
        Posizione posizione = manager.getAvvistamenti().keySet().iterator().next();

        assertTrue(manager.animaleIn(posizione).isPresent());
        assertTrue(manager.rimuovi(posizione).isPresent());
        assertTrue(manager.animaleIn(posizione).isEmpty());
        assertTrue(manager.rimuovi(posizione).isEmpty());
    }

    @Test
    void quantitaNegativaRifiutata() {
        assertThrows(IllegalArgumentException.class, () -> manager(leone).popola(mappa, -1));
    }

    @Test
    void catalogoVuotoRifiutato() {
        assertThrows(IllegalArgumentException.class, () -> manager());
    }

    @Test
    void specieNonRegistrataRifiutata() {
        VoceAnimale drago = new VoceAnimale("Drago", RaritaEnum.LEGGENDARIO, 1, List.of("Smaug"));
        assertThrows(IllegalArgumentException.class, () -> manager(drago));
    }

    @Test
    void avvistamentiNonModificabiliDallEsterno() {
        SpawnManager manager = manager(leone);
        assertThrows(UnsupportedOperationException.class, () -> manager.getAvvistamenti().clear());
    }
}
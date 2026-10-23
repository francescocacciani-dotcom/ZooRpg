package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.animals.*;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XMLZooSaveRepositoryTest {

    @TempDir
    Path cartella;

    private final RegistroSpecie registro = RegistroSpecie.predefinito();

    private Zoo zooDiProva() {
        Zoo zoo = new Zoo("Zoo di prova");
        zoo.aggiungiRecinto(new Recinto("Savana", BiomaEnum.SAVANA, 3));
        zoo.aggiungiRecinto(new Recinto("Palude", BiomaEnum.PALUDE, 2));
        zoo.accogli(new Leone("Leo", RaritaEnum.RARO, StatoAnimale.SELVATICO));
        zoo.accogli(new Giraffa("Gigi", RaritaEnum.COMUNE, StatoAnimale.IN_PERICOLO));
        // caratteri speciali per verificare l'escape XML
        zoo.accogli(new Capibara("Dott. O'Brien & <figli>", RaritaEnum.EPICO, StatoAnimale.SELVATICO));
        return zoo;
    }

    @Test
    void salvaECaricaMantieneTutto() throws Exception {
        Zoo originale = zooDiProva();
        XMLZooSaveRepository repository =
                new XMLZooSaveRepository(cartella.resolve("salvataggi/zoo.xml"), registro);

        repository.salva(originale);
        Zoo caricato = repository.carica().orElseThrow();

        assertEquals("Zoo di prova", caricato.getNome());
        assertEquals(originale.getRecinti().size(), caricato.getRecinti().size());
        assertEquals(originale.getStatistiche(), caricato.getStatistiche());

        List<Animale> prima = originale.getAnimali();
        List<Animale> dopo = caricato.getAnimali();
        assertEquals(prima.size(), dopo.size());
        for (int i = 0; i < prima.size(); i++) {
            assertEquals(prima.get(i), dopo.get(i));
            assertEquals(prima.get(i).getSpecie(), dopo.get(i).getSpecie());
            assertEquals(prima.get(i).getNome(), dopo.get(i).getNome());
            assertEquals(prima.get(i).getRarita(), dopo.get(i).getRarita());
            assertEquals(prima.get(i).getStato(), dopo.get(i).getStato());
        }
        assertInstanceOf(Leone.class, dopo.get(0));
    }

    @Test
    void ilSalvataggioSostituisceQuelloPrecedente() throws Exception {
        XMLZooSaveRepository repository = new XMLZooSaveRepository(cartella.resolve("zoo.xml"), registro);
        repository.salva(zooDiProva());
        repository.salva(new Zoo("Zoo vuoto"));

        Zoo caricato = repository.carica().orElseThrow();
        assertEquals("Zoo vuoto", caricato.getNome());
        assertTrue(caricato.getAnimali().isEmpty());
        assertFalse(Files.exists(cartella.resolve("zoo.xml.tmp")));
    }

    @Test
    void senzaFileNonCeNullaDaCaricare() throws Exception {
        XMLZooSaveRepository repository = new XMLZooSaveRepository(cartella.resolve("assente.xml"), registro);
        assertTrue(repository.carica().isEmpty());
    }

    @Test
    void fileNonXmlRifiutato() throws Exception {
        Path file = cartella.resolve("rotto.xml");
        Files.writeString(file, "questo non è xml");
        XMLZooSaveRepository repository = new XMLZooSaveRepository(file, registro);
        assertThrows(PersistenzaException.class, repository::carica);
    }

    @Test
    void salvataggioIncoerenteRifiutato() throws Exception {
        Path file = cartella.resolve("incoerente.xml");
        // un leone in un recinto di palude: valido per l'XSD ma non per le regole dello zoo
        Files.writeString(file, """
                <zoo nome="Z">
                  <recinto nome="Palude" bioma="PALUDE" capienza="2">
                    <animale id="123e4567-e89b-12d3-a456-426614174000" specie="Leone"
                             nome="Leo" rarita="RARO" stato="SELVATICO"/>
                  </recinto>
                </zoo>
                """);
        XMLZooSaveRepository repository = new XMLZooSaveRepository(file, registro);
        assertThrows(PersistenzaException.class, repository::carica);
    }

    @Test
    void specieSconosciutaRifiutata() throws Exception {
        Path file = cartella.resolve("sconosciuta.xml");
        Files.writeString(file, """
                <zoo nome="Z">
                  <recinto nome="Savana" bioma="SAVANA" capienza="2">
                    <animale id="123e4567-e89b-12d3-a456-426614174000" specie="Drago"
                             nome="Smaug" rarita="LEGGENDARIO" stato="SELVATICO"/>
                  </recinto>
                </zoo>
                """);
        XMLZooSaveRepository repository = new XMLZooSaveRepository(file, registro);
        assertThrows(PersistenzaException.class, repository::carica);
    }
}
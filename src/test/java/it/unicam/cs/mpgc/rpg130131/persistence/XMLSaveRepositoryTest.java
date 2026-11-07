package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.animals.*;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XMLSaveRepositoryTest {

    private static final StrumentoCattura RETE = new StrumentoCattura("Rete", 0.1);
    private static final Cibo CARNE = new Cibo("Carne", 0.15);

    @TempDir
    Path cartella;

    private final RegistroSpecie registro = RegistroSpecie.predefinito();

    private StatoPartita statoDiProva() {
        Zoo zoo = new Zoo("Zoo di prova");
        zoo.aggiungiRecinto(new Recinto("Savana", BiomaEnum.SAVANA, 3));
        zoo.aggiungiRecinto(new Recinto("Palude", BiomaEnum.PALUDE, 2));
        zoo.accogli(new Leone("Leo", RaritaEnum.RARO, StatoAnimale.SELVATICO));
        zoo.accogli(new Giraffa("Gigi", RaritaEnum.COMUNE, StatoAnimale.IN_PERICOLO));
        // caratteri speciali per verificare l'escape XML
        zoo.accogli(new Capibara("Dott. O'Brien & <figli>", RaritaEnum.EPICO, StatoAnimale.SELVATICO));

        Inventario inventario = new Inventario();
        inventario.aggiungi(RETE, 3);
        inventario.aggiungi(CARNE, 2);
        return new StatoPartita(zoo, new Giocatore("Francesco", new Posizione(4, 2), inventario));
    }

    private XMLSaveRepository repository(String nomeFile) {
        return new XMLSaveRepository(cartella.resolve(nomeFile), registro);
    }

    @Test
    void salvaECaricaMantieneTutto() throws Exception {
        StatoPartita originale = statoDiProva();
        XMLSaveRepository repository = repository("salvataggi/partita.xml");

        repository.salva(originale);
        StatoPartita caricato = repository.carica().orElseThrow();

        Zoo zooOriginale = originale.zoo();
        Zoo zooCaricato = caricato.zoo();
        assertEquals("Zoo di prova", zooCaricato.getNome());
        assertEquals(zooOriginale.getRecinti().size(), zooCaricato.getRecinti().size());
        assertEquals(zooOriginale.getStatistiche(), zooCaricato.getStatistiche());

        List<Animale> prima = zooOriginale.getAnimali();
        List<Animale> dopo = zooCaricato.getAnimali();
        assertEquals(prima.size(), dopo.size());
        for (int i = 0; i < prima.size(); i++) {
            assertEquals(prima.get(i), dopo.get(i));
            assertEquals(prima.get(i).getSpecie(), dopo.get(i).getSpecie());
            assertEquals(prima.get(i).getNome(), dopo.get(i).getNome());
            assertEquals(prima.get(i).getRarita(), dopo.get(i).getRarita());
            assertEquals(prima.get(i).getStato(), dopo.get(i).getStato());
        }
        assertInstanceOf(Leone.class, dopo.get(0));

        Giocatore giocatore = caricato.giocatore();
        assertEquals("Francesco", giocatore.getNome());
        assertEquals(new Posizione(4, 2), giocatore.getPosizione());
        assertEquals(3, giocatore.getInventario().quantita(RETE));
        assertEquals(2, giocatore.getInventario().quantita(CARNE));
        assertEquals(2, giocatore.getInventario().getContenuto().size());
    }

    @Test
    void ilSalvataggioSostituisceQuelloPrecedente() throws Exception {
        XMLSaveRepository repository = repository("partita.xml");
        repository.salva(statoDiProva());
        repository.salva(new StatoPartita(
                new Zoo("Zoo vuoto"),
                new Giocatore("Altro", new Posizione(0, 0), new Inventario())));

        StatoPartita caricato = repository.carica().orElseThrow();
        assertEquals("Zoo vuoto", caricato.zoo().getNome());
        assertTrue(caricato.zoo().getAnimali().isEmpty());
        assertEquals("Altro", caricato.giocatore().getNome());
        assertTrue(caricato.giocatore().getInventario().getContenuto().isEmpty());
        assertFalse(Files.exists(cartella.resolve("partita.xml.tmp")));
    }

    @Test
    void senzaFileNonCeNullaDaCaricare() throws Exception {
        assertTrue(repository("assente.xml").carica().isEmpty());
    }

    @Test
    void fileNonXmlRifiutato() throws Exception {
        Files.writeString(cartella.resolve("rotto.xml"), "questo non è xml");
        assertThrows(PersistenzaException.class, repository("rotto.xml")::carica);
    }

    @Test
    void bonusFuoriRangeRifiutatoDalloSchema() throws Exception {
        Files.writeString(cartella.resolve("bonus.xml"), """
                <partita>
                  <giocatore nome="T" x="0" y="0">
                    <inventario><strumento nome="Rete" bonus="7" quantita="1"/></inventario>
                  </giocatore>
                  <zoo nome="Z"/>
                </partita>
                """);
        assertThrows(PersistenzaException.class, repository("bonus.xml")::carica);
    }

    @Test
    void salvataggioIncoerenteRifiutato() throws Exception {
        // un leone in un recinto di palude: valido per l'XSD ma non per le regole dello zoo
        Files.writeString(cartella.resolve("incoerente.xml"), partitaConRecinto("""
                <recinto nome="Palude" bioma="PALUDE" capienza="2">
                  <animale id="123e4567-e89b-12d3-a456-426614174000" specie="Leone"
                           nome="Leo" rarita="RARO" stato="SELVATICO"/>
                </recinto>
                """));
        assertThrows(PersistenzaException.class, repository("incoerente.xml")::carica);
    }

    @Test
    void specieSconosciutaRifiutata() throws Exception {
        Files.writeString(cartella.resolve("sconosciuta.xml"), partitaConRecinto("""
                <recinto nome="Savana" bioma="SAVANA" capienza="2">
                  <animale id="123e4567-e89b-12d3-a456-426614174000" specie="Drago"
                           nome="Smaug" rarita="LEGGENDARIO" stato="SELVATICO"/>
                </recinto>
                """));
        assertThrows(PersistenzaException.class, repository("sconosciuta.xml")::carica);
    }

    private static String partitaConRecinto(String recinto) {
        return """
                <partita>
                  <giocatore nome="T" x="0" y="0"><inventario/></giocatore>
                  <zoo nome="Z">
                    %s
                  </zoo>
                </partita>
                """.formatted(recinto);
    }
}
package it.unicam.cs.mpgc.rpg130131.controller;

import it.unicam.cs.mpgc.rpg130131.controller.EsitoTentativo.Tipo;
import it.unicam.cs.mpgc.rpg130131.model.animals.*;
import it.unicam.cs.mpgc.rpg130131.model.capture.EngineCattura;
import it.unicam.cs.mpgc.rpg130131.model.capture.PoliticaRicompensa;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.catalog.VoceAnimale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Medicina;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.map.Direzione;
import it.unicam.cs.mpgc.rpg130131.model.map.Mappa2D;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.shop.EsitoAcquisto;
import it.unicam.cs.mpgc.rpg130131.model.shop.Negozio;
import it.unicam.cs.mpgc.rpg130131.model.shop.VoceNegozio;
import it.unicam.cs.mpgc.rpg130131.model.zoo.EsitoCura;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.RisultatoCura;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;
import it.unicam.cs.mpgc.rpg130131.persistence.PersistenzaException;
import it.unicam.cs.mpgc.rpg130131.persistence.XMLSaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
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

    private void creaController(RandomGenerator dado, Zoo zooIniziale) {
        creaController(dado, zooIniziale, animale -> Map.of());
    }

    private void creaController(RandomGenerator dado, Zoo zooIniziale, PoliticaRicompensa premio) {
        creaController(dado, zooIniziale, premio, new Negozio(List.of()));
    }

    /** Mappa di 3 celle di savana con un solo animale (un leone) e probabilità di cattura fissa al 50%. */
    private void creaController(RandomGenerator dado, Zoo zooIniziale, PoliticaRicompensa premio,
                                Negozio negozio) {
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
                premio,
                negozio,
                new XMLSaveRepository(cartella.resolve("partita.xml"), registro),
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
    void salvaECaricaRipristinaZooGiocatoreEInventario() throws Exception {
        giocatore.getInventario().aggiungi(CARNE, 2);
        controller.tentaCattura(RETE);
        Zoo zooSalvato = controller.getZoo();
        Posizione posizioneSalvata = giocatore.getPosizione();
        controller.salva();

        // modifico lo stato dopo il salvataggio: il caricamento deve annullare queste modifiche
        giocatore.getInventario().aggiungi(RETE, 5);
        giocatore.spostaA(new Posizione((posizioneSalvata.x() + 1) % 3, 0));

        assertTrue(controller.carica());

        Giocatore caricato = controller.getGiocatore();
        assertNotSame(giocatore, caricato);
        assertEquals(posizioneSalvata, caricato.getPosizione());
        assertEquals(0, caricato.getInventario().quantita(RETE));
        assertEquals(2, caricato.getInventario().quantita(CARNE));
        assertNotSame(zooSalvato, controller.getZoo());
        assertEquals(zooSalvato.getStatistiche(), controller.getZoo().getStatistiche());
    }

    @Test
    void caricaRifiutaUnaPosizioneFuoriDallaMappa() throws Exception {
        Files.writeString(cartella.resolve("partita.xml"), """
                <giocatore nome="Tester" x="99" y="0" monete="0"><inventario/></giocatore>
                """);
        Zoo prima = controller.getZoo();

        assertThrows(PersistenzaException.class, controller::carica);

        assertSame(prima, controller.getZoo());
        assertSame(giocatore, controller.getGiocatore());
    }

    @Test
    void parametriNonValidiRifiutati() {
        Mappa2D mappa = new Mappa2D(2, 2, p -> BiomaEnum.SAVANA);
        Giocatore fuori = new Giocatore("Tester", new Posizione(5, 5), new Inventario());
        XMLSaveRepository repository =
                new XMLSaveRepository(cartella.resolve("altro.xml"), RegistroSpecie.predefinito());
        EngineCattura engine = new EngineCattura((a, s, e) -> 0.5);
        PoliticaRicompensa nessunPremio = animale -> Map.of();
        Negozio negozioVuoto = new Negozio(List.of());

        assertThrows(IllegalArgumentException.class, () -> new GameController(
                mappa, fuori, zoo, spawn, engine, nessunPremio, negozioVuoto, repository, 1));
        assertThrows(IllegalArgumentException.class, () -> new GameController(
                mappa, new Giocatore("Ok", new Posizione(0, 0), new Inventario()),
                zoo, spawn, engine, nessunPremio, negozioVuoto, repository, -1));
    }

    @Test
    void laCatturaDaLaRicompensa() {
        creaController(DADO_BASSO, zooConSavana(), animale -> Map.of(CARNE, 2));

        EsitoTentativo esito = controller.tentaCattura(RETE);

        assertEquals(Map.of(CARNE, 2), esito.ricompensa());
        assertEquals(2, giocatore.getInventario().quantita(CARNE));
    }

    @Test
    void senzaCatturaNienteRicompensa() {
        creaController(DADO_ALTO, zooConSavana(), animale -> Map.of(CARNE, 2));

        EsitoTentativo esito = controller.tentaCattura(RETE);

        assertTrue(esito.ricompensa().isEmpty());
        assertEquals(0, giocatore.getInventario().quantita(CARNE));
    }

    @Test
    void anteprimaDellaProbabilita() {
        OptionalDouble anteprima = controller.anteprimaProbabilita(RETE, Cibo.NESSUNA_ESCA);

        assertEquals(0.5, anteprima.orElseThrow(), 1e-9);
        assertEquals(1, giocatore.getInventario().quantita(RETE), "l'anteprima non consuma nulla");
        assertTrue(zoo.getAnimali().isEmpty());
    }

    @Test
    void anteprimaVuotaSenzaAnimale() {
        spawn.rimuovi(giocatore.getPosizione());

        assertTrue(controller.anteprimaProbabilita(RETE, Cibo.NESSUNA_ESCA).isEmpty());
    }

    private Animale catturaUnAnimale() {
        return controller.tentaCattura(RETE).animale();
    }

    @Test
    void rilasciaDaLeMoneteDellaRarita() {
        Animale animale = catturaUnAnimale();

        int monete = controller.rilascia(animale);

        assertEquals(RaritaEnum.COMUNE.getMoneteRilascio(), monete);
        assertEquals(monete, giocatore.getMonete());
        assertTrue(zoo.getAnimali().isEmpty());
    }

    @Test
    void rilasciareUnAnimaleNonNelloZooLanciaEccezione() {
        Animale estraneo = new Leone("X", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        assertThrows(IllegalArgumentException.class, () -> controller.rilascia(estraneo));
    }

    @Test
    void dopoIlRilascioSiPuoCatturareDiNuovo() {
        creaController(DADO_BASSO, zooConSavana());
        Animale primo = catturaUnAnimale();
        controller.rilascia(primo);
        giocatore.getInventario().aggiungi(RETE, 1);
        portaGiocatoreSuAnimale();

        assertTrue(controller.tentaCattura(RETE).catturato());
    }

    @Test
    void laMedicinaCuraEVieneConsumata() {
        Animale animale = catturaUnAnimale();
        animale.impostaVita(30);
        Medicina antibiotico = new Medicina("Antibiotico", 15);
        giocatore.getInventario().aggiungi(antibiotico, 1);

        RisultatoCura risultato = controller.cura(animale, antibiotico);

        assertEquals(EsitoCura.CURATO, risultato.esito());
        assertEquals(15, risultato.puntiRecuperati());
        assertEquals(45, animale.getVita());
        assertEquals(0, giocatore.getInventario().quantita(antibiotico));
    }

    @Test
    void curaSenzaLOggettoNonFaNulla() {
        Animale animale = catturaUnAnimale();
        animale.impostaVita(30);

        RisultatoCura risultato = controller.cura(animale, new Medicina("Siero", 60));

        assertEquals(EsitoCura.NON_POSSEDUTO, risultato.esito());
        assertEquals(30, animale.getVita());
    }

    @Test
    void ilCiboNonCuraSottoLaSogliaENonVieneConsumato() {
        Animale animale = catturaUnAnimale();
        animale.impostaVita(70);
        Cibo frutta = new Cibo("Frutta", 0.05, 3);
        giocatore.getInventario().aggiungi(frutta, 1);

        RisultatoCura risultato = controller.cura(animale, frutta);

        assertEquals(EsitoCura.CIBO_NON_ADATTO, risultato.esito());
        assertEquals(70, animale.getVita());
        assertEquals(1, giocatore.getInventario().quantita(frutta));
    }

    @Test
    void ilCiboCuraSopraLaSoglia() {
        Animale animale = catturaUnAnimale();
        animale.impostaVita(90);
        Cibo frutta = new Cibo("Frutta", 0.05, 3);
        giocatore.getInventario().aggiungi(frutta, 1);

        RisultatoCura risultato = controller.cura(animale, frutta);

        assertEquals(EsitoCura.CURATO, risultato.esito());
        assertEquals(93, animale.getVita());
        assertEquals(0, giocatore.getInventario().quantita(frutta));
    }

    @Test
    void compraSpendeMonetePerUnaUnita() {
        VoceNegozio articolo = new VoceNegozio(RETE, 10);
        creaController(DADO_BASSO, zooConSavana(), animale -> Map.of(), new Negozio(List.of(articolo)));
        giocatore.aggiungiMonete(25);

        assertEquals(EsitoAcquisto.ACQUISTATO, controller.compra(articolo));

        assertEquals(15, giocatore.getMonete());
        assertEquals(2, giocatore.getInventario().quantita(RETE));
    }

    @Test
    void compraSenzaMoneteNonFaNulla() {
        VoceNegozio articolo = new VoceNegozio(RETE, 10);
        creaController(DADO_BASSO, zooConSavana(), animale -> Map.of(), new Negozio(List.of(articolo)));

        assertEquals(EsitoAcquisto.MONETE_INSUFFICIENTI, controller.compra(articolo));
        assertEquals(1, giocatore.getInventario().quantita(RETE));
    }

    @Test
    void ampliaUnRecintoESpendeMonete() {
        Recinto savana = zoo.getRecinti().getFirst();
        int prezzo = controller.getNegozio().prezzoAmpliamento(savana);
        giocatore.aggiungiMonete(prezzo);

        assertEquals(EsitoAcquisto.ACQUISTATO, controller.ampliaRecinto(savana));

        assertEquals(2 + Negozio.POSTI_PER_AMPLIAMENTO, savana.getCapienza());
        assertEquals(0, giocatore.getMonete());
    }

    @Test
    void ampliaSenzaMoneteNonCambiaLaCapienza() {
        Recinto savana = zoo.getRecinti().getFirst();

        assertEquals(EsitoAcquisto.MONETE_INSUFFICIENTI, controller.ampliaRecinto(savana));
        assertEquals(2, savana.getCapienza());
    }

    @Test
    void ampliareUnRecintoEstraneoLanciaEccezione() {
        Recinto estraneo = new Recinto("Altro", BiomaEnum.DESERTO, 2);
        assertThrows(IllegalArgumentException.class, () -> controller.ampliaRecinto(estraneo));
    }

    @Test
    void salvaECaricaRipristinaLeMonete() throws Exception {
        giocatore.aggiungiMonete(42);
        controller.salva();
        giocatore.aggiungiMonete(10);

        assertTrue(controller.carica());

        assertEquals(42, controller.getGiocatore().getMonete());
    }
}
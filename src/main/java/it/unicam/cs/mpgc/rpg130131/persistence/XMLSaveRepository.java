package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;
import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.animals.StatoAnimale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static it.unicam.cs.mpgc.rpg130131.persistence.UtilitaXML.figli;
import static it.unicam.cs.mpgc.rpg130131.persistence.UtilitaXML.figlio;

/**
 * Implementazione concreta di {@link SaveRepository} che salva e carica la partita in un file XML.
 *
 * <p>Il salvataggio è validato rispetto allo schema XSD definito in {@code /save/partita.xsd}. Il formato XML
 * include:</p>
 * <ul>
 *   <li><strong>giocatore</strong>: nome, posizione (x, y) e inventario completo</li>
 *   <li><strong>zoo</strong>: nome dello zoo e lista di tutti i recinti contenenti animali</li>
 * </ul>
 *
 * <p>Lo schema XSD garantisce la coerenza strutturale dei dati salvati, prevenendo corruzioni durante il salvataggio.</p>
 *
 * @see SaveRepository
 * @see XMLCatalogReader
 */
public class XMLSaveRepository implements SaveRepository {

    /** Percorso delle risorse dello schema XSD per validazione del file di salvataggio. */
    private static final String RISORSA_XSD = "/save/partita.xsd";

    /** Percorso del file dove salvare il gioco. */
    private final Path file;

    /** Registro delle specie usato per ricostruire gli animali durante il caricamento. */
    private final RegistroSpecie registro;

    /** URL allo schema XSD per la validazione dei file di salvataggio. */
    private final URL schema;

    /**
     * Costruttore che inietta il percorso del file e il registro delle specie.
     *
     * @param file percorso assoluto del file XML dove salvare/caricare la partita
     * @param registro registro delle specie per ricostruire gli animali dal loro tipo
     * @throws IllegalStateException se lo schema XSD non è disponibile nel classpath
     * @throws NullPointerException se file o registro sono null
     */
    public XMLSaveRepository(Path file, RegistroSpecie registro) {
        this.file = Objects.requireNonNull(file, "file");
        this.registro = Objects.requireNonNull(registro, "registro");
        this.schema = XMLSaveRepository.class.getResource(RISORSA_XSD);
        if (schema == null) {
            throw new IllegalStateException("Schema del salvataggio mancante: " + RISORSA_XSD);
        }
    }

    @Override
    /**
     * Salva lo stato corrente della partita nel file XML specificato.
     *
     * <p>La procedura di salvataggio:</p>
     * <ol>
     *   <li>Crea un nuovo documento XML vuoto</li>
     *   <li>Serializza il giocatore con nome, posizione e inventario (strumenti e cibi)</li>
     *   <li>Serializza lo zoo con tutti i recinti e gli animali contenuti</li>
     *   <li>Scrive il documento XML su un file temporaneo (.tmp) per evitare corruzioni</li>
     *   <li>Sposta il file temporaneo sul percorso finale</li>
     * </ol>
     *
     * @param stato lo StatoPartita da salvare, contenente zoo e giocatore
     * @throws PersistenzaException se la scrittura del file fallisce (errori I/O, directory non scrivibile, ecc.)
     */
    public void salva(StatoPartita stato) throws PersistenzaException {
        Objects.requireNonNull(stato, "stato");
        Document documento = UtilitaXML.nuovoDocumento();
        Element radice = documento.createElement("partita");
        documento.appendChild(radice);
        radice.appendChild(scriviGiocatore(documento, stato.giocatore()));
        radice.appendChild(scriviZoo(documento, stato.zoo()));
        UtilitaXML.scrivi(documento, file);
    }

    @Override
    /**
     * Carica lo stato della partita da un file XML esistente.
     *
     * <p>Se il file non esiste ancora o la lettura fallisce, restituisce un Optional vuoto.</p>
     *
     * <p>La procedura di caricamento:</p>
     * <ol>
     *   <li>Controlla l'esistenza del file sul disco</li>
     *   <li>Leggi e valida il file XML contro lo schema XSD</li>
     *   <li>Estrae i dati del giocatore (posizione e inventario)</li>
     *   <li>Estrae i dati dello zoo (recinti e animali)</li>
     *   <li>Ricostruisce e restituisce un oggetto StatoPartita</li>
     * </ol>
     *
     * @return un Optional contenente lo StatoPartita caricato, oppure Optional.empty() se il file non esiste
     * @throws PersistenzaException se la lettura del file fallisce o i dati sono corrotti
     */
    public Optional<StatoPartita> carica() throws PersistenzaException {
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try (InputStream flusso = Files.newInputStream(file)) {
            Element radice = UtilitaXML.leggi(flusso, schema).getDocumentElement();
            Giocatore giocatore = leggiGiocatore(figlio(radice, "giocatore"));
            Zoo zoo = leggiZoo(figlio(radice, "zoo"));
            return Optional.of(new StatoPartita(zoo, giocatore));
        } catch (IOException e) {
            throw new PersistenzaException("Impossibile leggere " + file, e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new PersistenzaException("Salvataggio incoerente: " + e.getMessage(), e);
        }
    }

    /**
     * Serializza il giocatore e il suo inventario in un elemento XML.
     *
     * @param documento il documento XML padre dove aggiungere l'elemento
     * @param giocatore l'oggetto Giocatore da serializzare
     * @return l'elemento XML "giocatore" creato, pronto per essere aggiunto al documento
     */
    private static Element scriviGiocatore(Document documento, Giocatore giocatore) {
        Element elemento = documento.createElement("giocatore");
        elemento.setAttribute("nome", giocatore.getNome());
        elemento.setAttribute("x", String.valueOf(giocatore.getPosizione().x()));
        elemento.setAttribute("y", String.valueOf(giocatore.getPosizione().y()));

        Element inventario = documento.createElement("inventario");
        giocatore.getInventario().getContenutoDi(StrumentoCattura.class).forEach((strumento, quantita) -> {
            Element e = documento.createElement("strumento");
            e.setAttribute("nome", strumento.nome());
            e.setAttribute("bonus", String.valueOf(strumento.bonusCattura()));
            e.setAttribute("quantita", String.valueOf(quantita));
            inventario.appendChild(e);
        });
        giocatore.getInventario().getContenutoDi(Cibo.class).forEach((cibo, quantita) -> {
            Element e = documento.createElement("cibo");
            e.setAttribute("nome", cibo.nome());
            e.setAttribute("bonus", String.valueOf(cibo.bonusEsca()));
            e.setAttribute("quantita", String.valueOf(quantita));
            inventario.appendChild(e);
        });
        elemento.appendChild(inventario);
        return elemento;
    }

    /**
     * Serializza lo zoo con tutti i recinti e gli animali in un elemento XML.
     *
     * @param documento il documento XML padre dove aggiungere l'elemento
     * @param zoo l'oggetto Zoo da serializzare
     * @return l'elemento XML "zoo" creato, pronto per essere aggiunto al documento
     */
    private static Element scriviZoo(Document documento, Zoo zoo) {
        Element elementoZoo = documento.createElement("zoo");
        elementoZoo.setAttribute("nome", zoo.getNome());
        for (Recinto recinto : zoo.getRecinti()) {
            Element elementoRecinto = documento.createElement("recinto");
            elementoRecinto.setAttribute("nome", recinto.getNome());
            elementoRecinto.setAttribute("bioma", recinto.getBioma().name());
            elementoRecinto.setAttribute("capienza", String.valueOf(recinto.getCapienza()));
            for (Animale animale : recinto.getAnimali()) {
                Element elementoAnimale = documento.createElement("animale");
                elementoAnimale.setAttribute("id", animale.getId().toString());
                elementoAnimale.setAttribute("specie", animale.getSpecie());
                elementoAnimale.setAttribute("nome", animale.getNome());
                elementoAnimale.setAttribute("rarita", animale.getRarita().name());
                elementoAnimale.setAttribute("stato", animale.getStato().name());
                elementoRecinto.appendChild(elementoAnimale);
            }
            elementoZoo.appendChild(elementoRecinto);
        }
        return elementoZoo;
    }

    /**
     * Deserializza un oggetto Giocatore da un elemento XML.
     *
     * @param elemento l'elemento XML "giocatore" contenente i dati serializzati
     * @return un oggetto Giocatore ricostruito dai dati XML
     */
    private static Giocatore leggiGiocatore(Element elemento) {
        Inventario inventario = new Inventario();
        Element elementoInventario = figlio(elemento, "inventario");
        for (Element e : figli(elementoInventario, "strumento")) {
            inventario.aggiungi(
                    new StrumentoCattura(e.getAttribute("nome"), Double.parseDouble(e.getAttribute("bonus"))),
                    Integer.parseInt(e.getAttribute("quantita")));
        }
        for (Element e : figli(elementoInventario, "cibo")) {
            inventario.aggiungi(
                    new Cibo(e.getAttribute("nome"), Double.parseDouble(e.getAttribute("bonus"))),
                    Integer.parseInt(e.getAttribute("quantita")));
        }
        Posizione posizione = new Posizione(
                Integer.parseInt(elemento.getAttribute("x")),
                Integer.parseInt(elemento.getAttribute("y")));
        return new Giocatore(elemento.getAttribute("nome"), posizione, inventario);
    }

    /**
     * Deserializza un oggetto Zoo da un elemento XML.
     *
     * @param elemento l'elemento XML "zoo" contenente i dati serializzati
     * @return un oggetto Zoo ricostruito dai dati XML
     */
    private Zoo leggiZoo(Element elemento) {
        Zoo zoo = new Zoo(elemento.getAttribute("nome"));
        for (Element elementoRecinto : figli(elemento, "recinto")) {
            zoo.aggiungiRecinto(leggiRecinto(elementoRecinto));
        }
        return zoo;
    }

    /**
     * Deserializza un oggetto Recinto con i suoi animali da un elemento XML.
     *
     * @param elemento l'elemento XML "recinto" contenente i dati serializzati
     * @return un oggetto Recinto ricostruito dai dati XML, inclusi tutti gli animali
     */
    private Recinto leggiRecinto(Element elemento) {
        Recinto recinto = new Recinto(
                elemento.getAttribute("nome"),
                BiomaEnum.valueOf(elemento.getAttribute("bioma")),
                Integer.parseInt(elemento.getAttribute("capienza")));
        for (Element e : figli(elemento, "animale")) {
            recinto.aggiungi(registro.ricostruisci(
                    e.getAttribute("specie"),
                    UUID.fromString(e.getAttribute("id")),
                    e.getAttribute("nome"),
                    RaritaEnum.valueOf(e.getAttribute("rarita")),
                    StatoAnimale.valueOf(e.getAttribute("stato"))));
        }
        return recinto;
    }
}
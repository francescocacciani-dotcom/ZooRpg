package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;
import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Medicina;
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

/** Salva e ricarica la partita in un file XML validato con {@code /save/partita.xsd}.
 * <p>
 * Questa classe implementa l'interfaccia {@link SaveRepository} ed è responsabile della gestione
 * della persistenza dello stato di una partita del gioco RPG Zoo. La classe legge il catalogo di gioco
 * (per avere accesso al registro delle specie animali) e poi opera su un file XML specificato come
 * luogo di salvataggio/ricarica dei dati.
 * </p>
 * <h3>Dove salvare le partite</h3>
 * I percorsi di file devono essere assoluti e indicano posizioni sulla macchina locale:
 * </p>
 * <ul>
 *   <li>&lt;code&gt;save/partita.xml&lt;/code&gt; - percorso predefinito per il salvataggio principale della partita</li>
 *   <li>I percorsi possono essere cambiati tramite l'overload del costruttore (vedi sotto)</li>
 * </ul>
 * <h3>Struttura dei file salvati</h3>
 * Il formato XML salvato segue lo schema XSD e include:
 * </p>
 * <ul>
 *   <li>&lt;zoo&gt; - l'intero zoo con tutti i recinti e gli animali</li>
 *   <li>&lt;giocatore&gt; - il giocatore corrente con posizione, monete e inventario completo</li>
 * </ul>
 * <p>
 * Importante: la mappa 2D (la griglia del mondo) non viene salvata. Solo gli animali nei recinti del zoo e lo stato del giocatore sono persistenti.
 * </p>
 * @see SaveRepository
 * @see StatoPartita
 * @see XMLCatalogReader
 */
public class XMLSaveRepository implements SaveRepository {

    private static final String RISORSA_XSD = "/save/partita.xsd";

    private final Path file;
    private final RegistroSpecie registro;
    private final URL schema;

    /**
     * Costruttore che inizializza il repository di salvataggio per la partita.
     * <p>
     * Il repository è configurato con un percorso di file assoluto dove salvare e caricare gli stati delle partite.
     * La classe tenta automaticamente di ottenere l'URL allo schema XSD (&lt;/save/partita.xsd&gt;) dalle risorse
     * dell'applicazione; se non viene trovato, genera un'eccezione {@link IllegalStateException}.
     * </p>
     * <p>
     * Il repository deve essere istanziiato con un percorso di file assoluto. La directory parent del file verrà
     * creata automaticamente il primo salvataggio (tramite {@link UtilitaXML#scrivi(Document, Path)}).
     * </p>
     *
     * @param file il percorso assoluto del file XML dove salvare e ricaricare lo stato della partita (es. &lt;/save/partita.xml&gt;)
     * @param registro un oggetto {@link RegistroSpecie} che mapea le specie animali e permette di ricostruire gli istanze di {@code Animale} dallo stato salvato
     * @throws NullPointerException se &code;file</code> o &code;registro</code> sono null
     * @throws IllegalStateException se lo schema XSD del salvataggio non può essere trovato nelle risorse dell'applicazione
     * @see #XMLSaveRepository(Path, RegistroSpecie) per usare un repository personalizzato con percorsi diversi
     */
    public XMLSaveRepository(Path file, RegistroSpecie registro) {
        this.file = Objects.requireNonNull(file, "file");
        this.registro = Objects.requireNonNull(registro, "registro");
        this.schema = XMLSaveRepository.class.getResource(RISORSA_XSD);
        if (schema == null) {
            throw new IllegalStateException("Schema del salvataggio mancante: " + RISORSA_XSD);
        }
    }

    /**
     * Salva lo stato corrente della partita nel file XML specificato durante la creazione del repository.
     * <p>
     * Lo stato da salvare include:
     * </p>
     * <ul>
     *   <li><strong>Zoo</strong>: contiene tutti i recinti e gli animali in essi contenuti (con le loro caratteristiche)</li>
     *   <li><strong>Giocatore</strong>: la posizione attuale (coordinate X,Y), il nome del giocatore, le monete possedute</li>
     * </ul>
     * <p>
     * Importante: lo stato persistente include solo gli animali contenuti nei recinti del zoo. La mappa 2D fissa
     * e gli animali temporaneamente posizionati sulla mappa non sono salvati (sono soggetti a reset tra una partita e l'altra).
     * </p>
     * <h3>Processo di salvataggio</h3>
     * Il metodo esegue:
     * </p>
     * <ol>
     *   <li>Valida che il parametro {@code stato} non sia null (generebbe {@link NullPointerException})</li>
     *   <li>Crea un nuovo documento XML vuoto usando {@link UtilitaXML#nuovoDocumento()}</li>
     *   <li>Crea l'elemento radice &lt;partita&gt;</li>
     *   <li>Aggiunge la sezione {@code /giocatore} creando un nodo figli che contiene:
     *     <ul>
     *       <li>&lt;nome&gt;- il nome del giocatore</li>
     *       <li>&lt;x&gt; e &lt;y&gt; - le coordinate X,Y della posizione corrente</li>
     *       <li>&lt;monete&gt; - il numero di monete possedute dal giocatore</li>
     *     </ul>
     *   </li>
     *   <li>Dopo la sezione giocatore, aggiunge la sezione {@code /inventario} con tutti gli oggetti posseduti:
     *     <ul>
     *       <li>&lt;strumento&gt;- ogni strumento di cattura (tipo e bonus)</li>
     *       <li>&lt;cibo&gt;- ogni alimento esca</li>
     *       <li>&lt;medicina&gt;- ogni medicina curativa</li>
     *     </ul>
     *   </li>
     *   <li>Aggiunge la sezione {@code /zoo} che contiene tutte le informazioni sul zoo:</li>
     * </ol>
     * <p>
     * Ogni recinto e ogni animale viene serializzato come elemento figlio appropriato del documento XML.
     * </p>
     * <h3>Dati persistenti</h3>
     * Lo stato persistente include:
     * </p>
     * <ul>
     *   <li><strong>Zoo</strong>: con tutti i recinti e gli animali in essi contenuti (con le loro caratteristiche)</li>
     *   <li><strong>Giocatore</strong>: posizione attuale, nome, monete e inventario completo</li>
     * </ul>
     * @param stato l'oggetto {@link StatoPartita} che contiene lo stato completo della partita da salvare
     * @throws PersistenzaException se la scrittura del file XML fallisce (errori I/O, directory non scrivibile, ecc.)
     */
    @Override
    public void salva(StatoPartita stato) throws PersistenzaException {
        Objects.requireNonNull(stato, "stato");
        Document documento = UtilitaXML.nuovoDocumento();
        Element radice = documento.createElement("partita");
        documento.appendChild(radice);
        radice.appendChild(scriviGiocatore(documento, stato.giocatore()));
        radice.appendChild(scriviZoo(documento, stato.zoo()));
        UtilitaXML.scrivi(documento, file);
    }

    /**
     * Carica e restituisce lo stato salvato precedentemente dalla partita.
     * <p>
     * Il metodo verifica se il file di salvataggio esiste nel percorso specificato durante la creazione del repository.
     * Se non esiste, restituisce immediatamente un {@link Optional} vuoto senza tentare alcuna operazione di lettura.
     * </p>
     * <h3>Processo di caricamento</h3>
     * Quando il file esiste:
     * </p>
     * <ol>
     *   <li>Apre lo stream del file XML esistente in modalità read-only</li>
     *   <li>Valida il documento contro lo schema XSD (&lt;/save/partita.xsd&gt;) usando {@link UtilitaXML#leggi(InputStream, URL)}</li>
     *   <li>Estrae l'elemento radice &lt;partita&gt; dal documento parseato</li>
     *   <li>Richiama i metodi privati per leggere le sezioni del file:</li>
     *   </ul>
     *   <ul>
     *     <li>{@code leggiGiocatore()} - legge la sezione &lt;/giocatore&gt; e ricostruisce il giocatore corrente</li>
     *     <li>{@code leggiZoo()} - legge la sezione &lt;/zoo&gt; e ricostruisce l'intero zoo con tutti i recinti e gli animali</li>
     *   </ul>
     *   <p>
     * Infine, viene restituito un {@link Optional} contenente un nuovo oggetto {@link StatoPartita} con lo zoo e il giocatore ricostruiti.
     * </p>
     * <h3>Casi in cui restituisce un valore vuoto</h3>
     * L'Optional è vuoto se:
     * </p>
     * <ul>
     *   <li>Nessun file di salvataggio esiste nel percorso specificato durante la creazione del repository (il controllo {@code Files.exists(file)} restituisce false)</li>
     * </ul>
     * <h3>Eccezioni</h3>
     * Genera {@link PersistenzaException} se:
     * </p>
     * <ul>
     *   <li>Nessuna eccezione si verifica, ma il parsing o la validazione XML fallisce</li>
     *   <li>L'eccezione &lt;code&gt;IllegalArgumentException&lt;/code&gt; viene lanciata da {@link UtilitaXML#leggi} o da uno dei metodi di lettura (es. se i nomi delle sezioni nel file non corrispondono allo schema XSD)</li>
     *   <li>L'eccezione &lt;code&gt;IllegalStateException&lt;/code&gt; viene lanciata durante la ricostruzione degli oggetti (es. quando si convertono le enum da stringhe a valori enum, o se i UUID non sono validi)</li>
     * </ul>
     * @return un {@link Optional} contenente uno stato {@link StatoPartita} ricostituito dal file XML, oppure un {@code Optional.empty()} se il file di salvataggio non esiste
     */
    @Override
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

    // ---------- scrittura ----------

    /**
     * Crea un elemento XML {@code /giocatore} che contiene tutte le informazioni sul giocatore corrente.
     * <p>
     * Il metodo crea prima l'elemento principale &lt;giocatore&gt; e gli attribuisce i seguenti attributi:
     * </p>
     * <ul>
     *   <li>&lt;nome&gt;- il nome del giocatore</li>
     *   <li>&lt;x&gt; - la coordinata X della posizione corrente</li>
     *   <li>&lt;y&gt; - la coordinata Y della posizione corrente</li>
     *   <li>&lt;monete&gt; - il numero di monete possedute dal giocatore</li>
     * </ul>
     * <p>
     * Successivamente, viene creato un elemento figlio &lt;inventario&gt; che contiene tutti gli oggetti
     * posseduti dal giocatore (strumenti, cibi e medicine) come elementi figli separati. Ogni tipo di oggetto
     * viene serializzato in modo specifico:
     * </p>
     * <ul>
     *   <li>&lt;/strumento&gt;- ogni elemento contiene: &lt;nome&gt;, &lt;bonus&gt;, &lt;quantita&gt;</li>
     *   <li>&lt;/cibo&gt;- ogni elemento contiene: &lt;nome&gt;, &lt;bonus&gt;, &lt;cura&gt;, &lt;quantita&gt;</li>
     *   <li>&lt;/medicina&gt;- ogni elemento contiene: &lt;nome&gt;, &lt;cura&gt;, &lt;quantita&gt;</li>
     * </ul>
     *
     * @param documento il documento XML padre al quale verranno aggiunti gli elementi del giocatore (non deve essere null)
     * @param giocatore l'oggetto {@link Giocatore} che contiene le informazioni da serializzare nel file XML
     * @return un elemento &lt;/giocatore&gt; pronto ad essere aggiunto come figlio all'elemento radice &lt;/partita&gt;
     */
    private static Element scriviGiocatore(Document documento, Giocatore giocatore) {
        Element elemento = documento.createElement("giocatore");
        elemento.setAttribute("nome", giocatore.getNome());
        elemento.setAttribute("x", String.valueOf(giocatore.getPosizione().x()));
        elemento.setAttribute("y", String.valueOf(giocatore.getPosizione().y()));
        elemento.setAttribute("monete", String.valueOf(giocatore.getMonete()));

        Element inventario = documento.createElement("inventario");
        Inventario zaino = giocatore.getInventario();
        zaino.getContenutoDi(StrumentoCattura.class).forEach((strumento, quantita) -> {
            Element e = documento.createElement("strumento");
            e.setAttribute("nome", strumento.nome());
            e.setAttribute("bonus", String.valueOf(strumento.bonusCattura()));
            e.setAttribute("quantita", String.valueOf(quantita));
            inventario.appendChild(e);
        });
        zaino.getContenutoDi(Cibo.class).forEach((cibo, quantita) -> {
            Element e = documento.createElement("cibo");
            e.setAttribute("nome", cibo.nome());
            e.setAttribute("bonus", String.valueOf(cibo.bonusEsca()));
            e.setAttribute("cura", String.valueOf(cibo.puntiVita()));
            e.setAttribute("quantita", String.valueOf(quantita));
            inventario.appendChild(e);
        });
        zaino.getContenutoDi(Medicina.class).forEach((medicina, quantita) -> {
            Element e = documento.createElement("medicina");
            e.setAttribute("nome", medicina.nome());
            e.setAttribute("cura", String.valueOf(medicina.puntiVita()));
            e.setAttribute("quantita", String.valueOf(quantita));
            inventario.appendChild(e);
        });
        elemento.appendChild(inventario);
        return elemento;
    }

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
                elementoAnimale.setAttribute("vita", String.valueOf(animale.getVita()));
                elementoRecinto.appendChild(elementoAnimale);
            }
            elementoZoo.appendChild(elementoRecinto);
        }
        return elementoZoo;
    }

    // ---------- lettura ----------

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
                    new Cibo(e.getAttribute("nome"),
                            Double.parseDouble(e.getAttribute("bonus")),
                            Integer.parseInt(e.getAttribute("cura"))),
                    Integer.parseInt(e.getAttribute("quantita")));
        }
        for (Element e : figli(elementoInventario, "medicina")) {
            inventario.aggiungi(
                    new Medicina(e.getAttribute("nome"), Integer.parseInt(e.getAttribute("cura"))),
                    Integer.parseInt(e.getAttribute("quantita")));
        }
        Posizione posizione = new Posizione(
                Integer.parseInt(elemento.getAttribute("x")),
                Integer.parseInt(elemento.getAttribute("y")));
        return new Giocatore(
                elemento.getAttribute("nome"),
                posizione,
                inventario,
                Integer.parseInt(elemento.getAttribute("monete")));
    }

    private Zoo leggiZoo(Element elemento) {
        Zoo zoo = new Zoo(elemento.getAttribute("nome"));
        for (Element elementoRecinto : figli(elemento, "recinto")) {
            zoo.aggiungiRecinto(leggiRecinto(elementoRecinto));
        }
        return zoo;
    }

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
                    Integer.parseInt(e.getAttribute("vita"))));
        }
        return recinto;
    }
}
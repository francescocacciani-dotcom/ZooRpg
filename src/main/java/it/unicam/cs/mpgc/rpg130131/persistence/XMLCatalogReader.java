package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.catalog.VoceAnimale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Medicina;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Scorta;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.shop.VoceNegozio;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static it.unicam.cs.mpgc.rpg130131.persistence.UtilitaXML.figli;
import static it.unicam.cs.mpgc.rpg130131.persistence.UtilitaXML.figlio;

/** Legge il catalogo da un documento XML validato con il suo schema XSD.
 * <p>
 * Questa classe implementa l'interfaccia {@link CatalogReader} ed è responsabile del caricamento
 * del catalogo di gioco da risorse incluse nel pacchetto dell'applicazione. Include sia i dati degli
 * animali disponibili che le informazioni sul negozio (articoli in vendita, kit iniziali).
 * </p>
 * <h3>Flusso di lettura</h3>
 * <ol>
 *   <li>Il file XML del catalogo viene aperto in modalità read-only tramite {@code URL.openStream()}</li>
 *   <li>Il documento viene parseato e validato contro lo schema XSD (tramite {@link UtilitaXML#leggi})</li>
 *   <li>Dal documento XML vengono estratti: animali, strumenti di cattura, cibi, medicine, voci di negozio</li>
 *   <li>Vengono risolti i riferimenti per nome agli oggetti del catalogo</li>
 *   <li>Viene restituito un oggetto {@link Catalogo} completo con tutte le informazioni</li>
 * </ol>
 * <h3>Risorse utilizzate</h3>
 * <ul>
 *   <li><code>/catalog/animali.xml</code> - Il file XML del catalogo</li>
 *   <li><code>/catalog/animali.xsd</code> - Lo schema XSD per la validazione</li>
 * </ul>
 * @see CatalogReader
 * @see UtilitaXML
 */
public class XMLCatalogReader implements CatalogReader {

    private static final String RISORSA_XML = "/catalog/animali.xml";
    private static final String RISORSA_XSD = "/catalog/animali.xsd";

    /** Gli oggetti definiti nel catalogo: servono a risolvere i riferimenti per nome di negozio e kit. */
    private record Oggetti(List<StrumentoCattura> strumenti, List<Cibo> cibi, List<Medicina> medicine) {
        Oggetto trova(String tipo, String nome) {
            List<? extends Oggetto> candidati = switch (tipo) {
                case "STRUMENTO" -> strumenti;
                case "CIBO" -> cibi;
                case "MEDICINA" -> medicine;
                default -> throw new IllegalArgumentException("Tipo di oggetto sconosciuto: " + tipo);
            };
            return candidati.stream()
                    .filter(oggetto -> oggetto.nome().equals(nome))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Oggetto non definito nel catalogo: %s \"%s\"".formatted(tipo, nome)));
        }
    }

    private final URL documento;
    private final URL schema;

    /**
     * Costruttore che inizializza il lettore del catalogo con gli URL alle risorse XML e XSD.
     * <p>
     * I parametri devono puntare a file di risorse presenti nel pacchetto dell'applicazione:
     * l'URL del file XML e l'URL dello schema XSD per la validazione. Entrambi i parametri
     * vengono memorizzati come campi finali e non possono essere cambiati dopo la creazione.
     * </p>
     * <ul>
     *   <li><code>documento</code> - URL al file XML del catalogo (es. &lt;/catalog/animali.xml&gt;)</li>
     *   <li><code>schema</code> - URL allo schema XSD per la validazione (es. &lt;/catalog/animali.xsd&gt;)</li>
     * </ul>
     *
     * @param documento URL al file XML del catalogo da leggere e caricare nella memoria
     * @param schema URL allo schema XSD utilizzato per validare il documento XML prima dell'uso
     * @throws NullPointerException se &code;documento</code> o &code;schema</code> sono null
     * @see #predefinito() per ottenere un lettore preconfigurato con le risorse di default
     */
    public XMLCatalogReader(URL documento, URL schema) {
        this.documento = Objects.requireNonNull(documento, "documento");
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    /**
     * Restituisce un lettore del catalogo preconfigurato con le risorse incluse nelle directory delle risorse dell'applicazione.
     * <p>
     * Questo metodo tenta di ottenere gli URL alle risorse &lt;/catalog/animali.xml&gt; e
     * &lt;/catalog/animali.xsd&gt;. Se nessuna delle due risorse viene trovata, genera un'eccezione
     * {@link IllegalStateException} con un messaggio di errore che spiega il problema.
     * </p>
     *
     * @return un nuovo oggetto {@code XMLCatalogReader} configurato con le risorse di default per il catalogo del gioco
     * @throws IllegalStateException se le risorse del catalogo non possono essere trovate nel classpath dell'applicazione
     * @see #XMLCatalogReader(URL, URL) per creare un lettore con risorse personalizzate
     */
    public static XMLCatalogReader predefinito() {
        URL xml = XMLCatalogReader.class.getResource(RISORSA_XML);
        URL xsd = XMLCatalogReader.class.getResource(RISORSA_XSD);
        if (xml == null || xsd == null) {
            throw new IllegalStateException("Risorse del catalogo mancanti");
        }
        return new XMLCatalogReader(xml, xsd);
    }

    /**
     * Legge e restituisce l'oggetto {@link Catalogo} completo contenente tutte le informazioni
     * sugli animali, gli strumenti di cattura e i cibi.
     * <p>
     * Il metodo esegue:
     * </p>
     * <ol>
     *   <li>Apre lo stream del file XML del catalogo tramite {@code documento.openStream()}</li>
     *   <li>Valida il documento contro lo schema XSD usando {@link UtilitaXML#leggi(InputStream, URL)}</li>
     *   <li>Estrae dal documento radice le seguenti sezioni:</li>
     *   </ul>
     *   <ul>
     *     <li>{@code animali} - la lista degli animali disponibili nel gioco</li>
     *     <li>{@code strumenti} - gli oggetti che aiutano a catturare gli animali (es. lancia, rete)</li>
     *     <li>{@code cibi} - gli alimenti usati come esca per attirare gli animali</li>
     *     <li>{@code medicine} - le medicine usabili nel gioco</li>
     *     <li>{@code negozio} - gli articoli venduti al negozio con i loro prezzi</li>
     *     <li>{@code kit} - le scorte iniziali disponibili nel kit di partenza</li>
     *   </ul>
     *   <p>
     * I riferimenti agli oggetti del negozio e ai componenti del kit vengono risolti usando
     * il record interno {@link Oggetti} che contiene le liste di tutti gli oggetti definiti.
     * </p>
     * <h3>Eccezioni</h3>
     * <ul>
     *   <li>{@link PersistenzaException} - se lo stream non può essere aperto, il parsing o la validazione falliscono</li>
     *   <li>{@link PersistenzaException} - anche in caso di {@code IllegalArgumentException}, perché indica un catalogo incoerente con i nomi degli oggetti usati</li>
     * </ul>
     * @return un oggetto {@link Catalogo} popolato con tutti gli animali, strumenti, cibi, medicine, voci negozio e kit del gioco
     * @throws PersistenzaException se il file non può essere letto o contiene dati non validi rispetto allo schema XSD
     */
    public Catalogo leggi() throws PersistenzaException {
        try (InputStream flusso = documento.openStream()) {
            Element radice = UtilitaXML.leggi(flusso, schema).getDocumentElement();
            Oggetti oggetti = new Oggetti(strumenti(radice), cibi(radice), medicine(radice));
            return new Catalogo(
                    animali(radice),
                    oggetti.strumenti(),
                    oggetti.cibi(),
                    oggetti.medicine(),
                    negozio(radice, oggetti),
                    kit(radice, oggetti));
        } catch (IOException e) {
            throw new PersistenzaException("Impossibile leggere " + documento, e);
        } catch (IllegalArgumentException e) {
            throw new PersistenzaException("Catalogo incoerente: " + e.getMessage(), e);
        }
    }

    /**
     * Esegue il parsing della sezione {@code animali} del documento XML.
     * <p>
     * Il metodo estra tutti gli elementi figlio con etichetta &lt;animale&gt; dalla sezione principale
     * {@code /catalog/animali.xml/animali} e costruisce una {@link java.util.List} di oggetti
     * {@link VoceAnimale}. Ogni animale viene istanziato usando:
     * </p>
     * <ul>
     *   <li{@code specie}&lt;span&gt;- attributo del nodo elementare &lt;animale&gt;</span&gt;</li>
     *   <li>{@code rarita}- valore dell'attributo XML convertito a {@link RaritaEnum}</li>
     *   <li>{@code peso}- valore intero dell'attributo che rappresenta il peso specifico della specie</li>
     *   <li>{@code nomi}- lista di tutti i nodi &lt;nome&gt; figlio del nodo &lt;animale&gt;,
     *       estratti e convertiti a Stringa rimuovendo gli spazi bianchi</span&gt;</li>
     * </ul>
     *
     * @param radice il nodo elemento radice (&lt;/catalog/animali.xml&gt;) del documento XML parseato
     * @return una lista di oggetti {@link VoceAnimale} contenente tutte le voci degli animali disponibili nel catalogo del gioco, nell'ordine in cui appaiono nel file XML
     * @see #strumenti(Element)
     * @see #cibi(Element)
     * @see #medicine(Element)
     */
    private static List<VoceAnimale> animali(Element radice) {
        return figli(figlio(radice, "animali"), "animale").stream()
                .map(e -> new VoceAnimale(
                        e.getAttribute("specie"),
                        RaritaEnum.valueOf(e.getAttribute("rarita")),
                        Integer.parseInt(e.getAttribute("peso")),
                        figli(figlio(e, "nomi"), "nome").stream()
                                .map(n -> n.getTextContent().trim())
                                .toList()))
                .toList();
    }

    /**
     * Esegue il parsing della sezione {@code strumenti} del documento XML.
     * <p>
     * Il metodo estrae tutti gli elementi figlio con etichetta &lt;strumento&gt; dalla sezione principale
     * {@code /catalog/animali.xml/strumenti} e costruisce una lista di oggetti {@link StrumentoCattura}.
     * Ogni strumento viene istanziato usando:
     * </p>
     * <ul>
     *   <li>{@code nome}- attributo che identifica lo strumento di cattura</li>
     *   <li>{@code bonus}- valore a virgola mobile (double) dell'attributo che rappresenta il bonus
     *       applicato quando si usa lo strumento per catturare un animale</span&gt;</li>
     * </ul>
     *
     * @param radice il nodo elemento radice (&lt;/catalog/animali.xml&gt;) del documento XML parseato
     * @return una lista di oggetti {@link StrumentoCattura} contenenti tutti gli strumenti disponibili nel catalogo del gioco, nell'ordine in cui appaiono nel file XML
     * @see #animali(Element)
     * @see #cibi(Element)
     * @see #medicine(Element)
     */
    private static List<StrumentoCattura> strumenti(Element radice) {
        return figli(figlio(radice, "strumenti"), "strumento").stream()
                .map(e -> new StrumentoCattura(
                        e.getAttribute("nome"),
                        Double.parseDouble(e.getAttribute("bonus"))))
                .toList();
    }

    /**
     * Esegue il parsing della sezione {@code cibi} del documento XML.
     * <p>
     * Il metodo estrae tutti gli elementi figlio con etichetta &lt;cibo&gt; dalla sezione principale
     * {@code /catalog/animali.xml/cibi} e costruisce una lista di oggetti {@link Cibo}.
     * Ogni cibo viene istanziato usando:
     * </p>
     * <ul>
     *   <li>{@code nome}- attributo che identifica il tipo di cibo usato come esca</li>
     *   <li>{@code bonus}- valore a virgola mobile (double) dell'attributo che rappresenta il bonus
     *       applicato all'esca quando si usa questo cibo per attirare un animale</span&gt;</li>
     *   <li>{@code cura}- valore intero dell'attributo che indica i punti vita restaurati quando si mangia questo cibo</span&gt;</li>
     * </ul>
     *
     * @param radice il nodo elemento radice (&lt;/catalog/animali.xml&gt;) del documento XML parseato
     * @return una lista di oggetti {@link Cibo} contenenti tutti i tipi di cibo disponibili nel catalogo del gioco, nell'ordine in cui appaiono nel file XML
     * @see #animali(Element)
     * @see #strumenti(Element)
     * @see #medicine(Element)
     */
    private static List<Cibo> cibi(Element radice) {
        return figli(figlio(radice, "cibi"), "cibo").stream()
                .map(e -> new Cibo(
                        e.getAttribute("nome"),
                        Double.parseDouble(e.getAttribute("bonus")),
                        Integer.parseInt(e.getAttribute("cura"))))
                .toList();
    }

    /**
     * Esegue il parsing della sezione {@code medicine} del documento XML.
     * <p>
     * Il metodo estrae tutti gli elementi figlio con etichetta &lt;medicina&gt; dalla sezione principale
     * {@code /catalog/animali.xml/medicine} e costruisce una lista di oggetti {@link Medicina}.
     * Ogni medicina viene istanziata usando:
     * </p>
     * <ul>
     *   <li>{@code nome}- attributo che identifica il tipo di medicina</li>
     *   <li>{@code cura}- valore intero dell'attributo che indica i punti vita restaurati quando si usa questa medicina</span&gt;</li>
     * </ul>
     *
     * @param radice il nodo elemento radice (&lt;/catalog/animali.xml&gt;) del documento XML parseato
     * @return una lista di oggetti {@link Medicina} contenenti tutti i tipi di medicina disponibili nel catalogo del gioco, nell'ordine in cui appaiono nel file XML
     * @see #animali(Element)
     * @see #strumenti(Element)
     * @see #cibi(Element)
     */
    private static List<Medicina> medicine(Element radice) {
        return figli(figlio(radice, "medicine"), "medicina").stream()
                .map(e -> new Medicina(
                        e.getAttribute("nome"),
                        Integer.parseInt(e.getAttribute("cura"))))
                .toList();
    }

    /**
     * Esegue il parsing della sezione {@code negozio} del documento XML.
     * <p>
     * Il metodo estrae tutti gli elementi figlio con etichetta &lt;articolo&gt; dalla sezione principale
     * {@code /catalog/animali.xml/negozio} e costruisce una lista di oggetti {@link VoceNegozio}.
     * Ogni voce di negozio viene creata usando:
     * </p>
     * <ul>
     *   <li>{@code tipo}- attributo che indica il tipo di oggetto venduto (deve corrispondere a un valore definito
     *       nel record interno {@link Oggetti}, es. "STRUMENTO", "CIBO", o "MEDICINA")</span&gt;</li>
     *   <li>{@code nome}- attributo che identifica il nome specifico dell'oggetto da vendere</li>
     * </ul>
     * <p>
     * Per risolvere la coppia ({@code tipo}, {@code nome}) in un oggetto effettivo, il metodo usa il record interno
     * {@link Oggetti} passato come parametro, che contiene tutte le liste di oggetti del catalogo.
     * </p>
     * <p>
     * Infine, viene creato una {@link VoceNegozio} con l'oggetto risolvuto e il prezzo indicato dall'attributo &lt;prezzo&gt;.
     * </p>
     *
     * @param radice il nodo elemento radice (&lt;/catalog/animali.xml&gt;) del documento XML parseato
     * @param oggetti il record interno {@link Oggetti} che contiene tutte le liste di oggetti del catalogo (strumenti, cibi, medicine)
     * @return una lista di oggetti {@link VoceNegozio} contenenti tutti gli articoli disponibili per l'acquisto al negozio, nell'ordine in cui appaiono nel file XML
     * @see #animali(Element)
     * @see #strumenti(Element)
     * @see #cibi(Element)
     * @see #medicine(Element)
     */
    private static List<VoceNegozio> negozio(Element radice, Oggetti oggetti) {
        return figli(figlio(radice, "negozio"), "articolo").stream()
                .map(e -> new VoceNegozio(
                        oggetti.trova(e.getAttribute("tipo"), e.getAttribute("nome")),
                        Integer.parseInt(e.getAttribute("prezzo"))))
                .toList();
    }

    /**
     * Esegue il parsing della sezione {@code kit} del documento XML.
     * <p>
     * Il metodo estrae tutti gli elementi figlio con etichetta &lt;voce&gt; dalla sezione principale
     * {@code /catalog/animali.xml/kit} e costruisce una lista di oggetti {@link Scorta}.
     * Ogni voce del kit viene creata usando:
     * </p>
     * <ul>
     *   <li>{@code tipo}- attributo che indica il tipo di oggetto incluso nel kit (deve corrispondere a un valore definito
     *       nel record interno {@link Oggetti}, es. "STRUMENTO", "CIBO", o "MEDICINA")</span&gt;</li>
     *   <li>{@code nome}- attributo che identifica il nome specifico dell'oggetto incluso nel kit</li>
     * </ul>
     * <p>
     * Per risolvere la coppia ({@code tipo}, {@code nome}) in un oggetto effettivo, il metodo usa il record interno
     * {@link Oggetti} passato come parametro.
     * </p>
     * <p>
     * Infine, viene creato una {@link Scorta} con l'oggetto risolvuto e la quantità indicata dall'attributo &lt;quantita&gt;.
     * </p>
     *
     * @param radice il nodo elemento radice (&lt;/catalog/animali.xml&gt;) del documento XML parseato
     * @param oggetti il record interno {@link Oggetti} che contiene tutte le liste di oggetti del catalogo (strumenti, cibi, medicine)
     * @return una lista di oggetti {@link Scorta} contenenti tutti gli elementi del kit iniziale, nell'ordine in cui appaiono nel file XML
     * @see #animali(Element)
     * @see #strumenti(Element)
     * @see #cibi(Element)
     * @see #medicine(Element)
     */
    private static List<Scorta> kit(Element radice, Oggetti oggetti) {
        return figli(figlio(radice, "kit"), "voce").stream()
                .map(e -> new Scorta(
                        oggetti.trova(e.getAttribute("tipo"), e.getAttribute("nome")),
                        Integer.parseInt(e.getAttribute("quantita"))))
                .toList();
    }
}
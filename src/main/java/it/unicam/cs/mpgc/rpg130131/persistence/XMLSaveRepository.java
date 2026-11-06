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

/** Salva e ricarica la partita in un file XML validato con {@code /save/partita.xsd}. */
public class XMLSaveRepository implements SaveRepository {

    private static final String RISORSA_XSD = "/save/partita.xsd";

    private final Path file;
    private final RegistroSpecie registro;
    private final URL schema;

    public XMLSaveRepository(Path file, RegistroSpecie registro) {
        this.file = Objects.requireNonNull(file, "file");
        this.registro = Objects.requireNonNull(registro, "registro");
        this.schema = XMLSaveRepository.class.getResource(RISORSA_XSD);
        if (schema == null) {
            throw new IllegalStateException("Schema del salvataggio mancante: " + RISORSA_XSD);
        }
    }

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
                    new Cibo(e.getAttribute("nome"), Double.parseDouble(e.getAttribute("bonus"))),
                    Integer.parseInt(e.getAttribute("quantita")));
        }
        Posizione posizione = new Posizione(
                Integer.parseInt(elemento.getAttribute("x")),
                Integer.parseInt(elemento.getAttribute("y")));
        return new Giocatore(elemento.getAttribute("nome"), posizione, inventario);
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
                    StatoAnimale.valueOf(e.getAttribute("stato"))));
        }
        return recinto;
    }
}
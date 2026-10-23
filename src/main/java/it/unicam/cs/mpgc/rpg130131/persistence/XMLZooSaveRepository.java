package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;
import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.animals.StatoAnimale;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
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

/** Salva e ricarica lo zoo in un file XML validato con {@code /save/zoo.xsd}. */
public class XMLZooSaveRepository implements ZooSaveRepository {

    private static final String RISORSA_XSD = "/save/zoo.xsd";

    private final Path file;
    private final RegistroSpecie registro;
    private final URL schema;

    public XMLZooSaveRepository(Path file, RegistroSpecie registro) {
        this.file = Objects.requireNonNull(file, "file");
        this.registro = Objects.requireNonNull(registro, "registro");
        this.schema = XMLZooSaveRepository.class.getResource(RISORSA_XSD);
        if (schema == null) {
            throw new IllegalStateException("Schema del salvataggio mancante: " + RISORSA_XSD);
        }
    }

    @Override
    public void salva(Zoo zoo) throws PersistenzaException {
        Objects.requireNonNull(zoo, "zoo");
        Document documento = UtilitaXML.nuovoDocumento();
        Element radice = documento.createElement("zoo");
        radice.setAttribute("nome", zoo.getNome());
        documento.appendChild(radice);

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
            radice.appendChild(elementoRecinto);
        }
        UtilitaXML.scrivi(documento, file);
    }

    @Override
    public Optional<Zoo> carica() throws PersistenzaException {
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try (InputStream flusso = Files.newInputStream(file)) {
            Element radice = UtilitaXML.leggi(flusso, schema).getDocumentElement();
            Zoo zoo = new Zoo(radice.getAttribute("nome"));
            for (Element elementoRecinto : figli(radice, "recinto")) {
                zoo.aggiungiRecinto(leggiRecinto(elementoRecinto));
            }
            return Optional.of(zoo);
        } catch (IOException e) {
            throw new PersistenzaException("Impossibile leggere " + file, e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new PersistenzaException("Salvataggio incoerente: " + e.getMessage(), e);
        }
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
package it.unicam.cs.mpgc.rpg130131.model.map;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Mappa rettangolare di celle. Contiene solo il terreno (non animali né giocatore).
 * La mappa è immutabile dopo la costruzione.
 */
public class Mappa2D {

    private final int larghezza;
    private final int altezza;
    private final Cella[][] celle; // indicizzata [y][x]

    /**
     * Costruisce una mappa larghezza x altezza. Per ogni posizione (x da 0 a larghezza-1,
     * y da 0 a altezza-1) il bioma è deciso da {@code assegnaBioma}.
     *
     * @throws IllegalArgumentException se larghezza o altezza sono <= 0
     * @throws NullPointerException     se assegnaBioma è null o restituisce null
     */
    public Mappa2D(int larghezza, int altezza, Function<Posizione, BiomaEnum> assegnaBioma) {
        if (larghezza <= 0 || altezza <= 0) {
            throw new IllegalArgumentException(
                    "Le dimensioni devono essere positive: %dx%d".formatted(larghezza, altezza));
        }
        Objects.requireNonNull(assegnaBioma, "assegnaBioma");
        this.larghezza = larghezza;
        this.altezza = altezza;
        this.celle = new Cella[altezza][larghezza];
        for (int y = 0; y < altezza; y++) {
            for (int x = 0; x < larghezza; x++) {
                Posizione posizione = new Posizione(x, y);
                celle[y][x] = new Cella(posizione, assegnaBioma.apply(posizione));
            }
        }
    }

    public int getLarghezza() {
        return larghezza;
    }

    public int getAltezza() {
        return altezza;
    }

    /** @return true se la posizione cade dentro i limiti della mappa */
    public boolean contiene(Posizione posizione) {
        Objects.requireNonNull(posizione, "posizione");
        return posizione.x() >= 0 && posizione.x() < larghezza
                && posizione.y() >= 0 && posizione.y() < altezza;
    }

    /**
     * @return la cella in quella posizione
     * @throws IllegalArgumentException se la posizione è fuori dalla mappa
     */
    public Cella getCella(Posizione posizione) {
        if (!contiene(posizione)) {
            throw new IllegalArgumentException("Posizione fuori dalla mappa: " + posizione);
        }
        return celle[posizione.y()][posizione.x()];
    }

    /**
     * @return la posizione raggiunta spostandosi da {@code da} nella direzione indicata,
     *         oppure vuoto se la destinazione è fuori dalla mappa
     */
    public Optional<Posizione> destinazione(Posizione da, Direzione direzione) {
        Posizione arrivo = Objects.requireNonNull(da, "da").verso(direzione);
        return contiene(arrivo) ? Optional.of(arrivo) : Optional.empty();
    }

    /** @return le posizioni adiacenti (nelle 4 direzioni) che stanno dentro la mappa */
    public List<Posizione> vicini(Posizione posizione) {
        Objects.requireNonNull(posizione, "posizione");
        return Arrays.stream(Direzione.values())
                .map(posizione::verso)
                .filter(this::contiene)
                .toList();
    }

    /** @return tutte le celle della mappa, per righe (y crescente, poi x crescente) */
    public Stream<Cella> celle() {
        return Arrays.stream(celle).flatMap(Arrays::stream);
    }

    /** @return le celle del bioma indicato */
    public List<Cella> celleDiBioma(BiomaEnum bioma) {
        Objects.requireNonNull(bioma, "bioma");
        return celle().filter(cella -> cella.bioma() == bioma).toList();
    }
}
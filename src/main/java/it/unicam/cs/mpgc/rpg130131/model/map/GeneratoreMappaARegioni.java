package it.unicam.cs.mpgc.rpg130131.model.map;

import java.util.Comparator;
import java.util.List;

/**
 * Genera una mappa deterministica divisa in quattro regioni: ogni bioma ha un "seme" in un
 * quadrante e ogni cella prende il bioma del seme più vicino. A parità di dimensioni
 * la mappa è sempre la stessa, quindi non serve salvarla.
 */
public class GeneratoreMappaARegioni implements GeneratoreMappa {

    /** Posizione relativa (0..1) del centro di una regione. */
    private record Seme(BiomaEnum bioma, double fx, double fy) {
    }

    private static final List<Seme> SEMI = List.of(
            new Seme(BiomaEnum.SAVANA, 0.25, 0.25),
            new Seme(BiomaEnum.FORESTA, 0.75, 0.25),
            new Seme(BiomaEnum.PALUDE, 0.25, 0.75),
            new Seme(BiomaEnum.DESERTO, 0.75, 0.75));

    @Override
    public Mappa2D genera(int larghezza, int altezza) {
        return new Mappa2D(larghezza, altezza, posizione -> biomaPiuVicino(posizione, larghezza, altezza));
    }

    private static BiomaEnum biomaPiuVicino(Posizione posizione, int larghezza, int altezza) {
        return SEMI.stream()
                .min(Comparator.comparingDouble(seme -> distanzaQuadrata(posizione, seme, larghezza, altezza)))
                .orElseThrow()
                .bioma();
    }

    private static double distanzaQuadrata(Posizione posizione, Seme seme, int larghezza, int altezza) {
        double dx = posizione.x() - (larghezza - 1) * seme.fx();
        double dy = posizione.y() - (altezza - 1) * seme.fy();
        return dx * dx + dy * dy;
    }
}
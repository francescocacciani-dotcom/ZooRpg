package it.unicam.cs.mpgc.rpg130131.model.capture;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Premia ogni cattura con {@value #STRUMENTI_PER_CATTURA} strumenti e {@value #CIBI_PER_CATTURA} cibo.
 * Gli oggetti del catalogo sono ordinati dal più debole al più forte e la rarità dell'animale
 * (COMUNE = 0, RARO = 1, ...) sceglie la posizione, fermandosi all'ultimo oggetto disponibile.
 */
public class RicompensaPerRarita implements PoliticaRicompensa {

    public static final int STRUMENTI_PER_CATTURA = 2;
    public static final int CIBI_PER_CATTURA = 1;

    private final List<StrumentoCattura> strumenti;
    private final List<Cibo> cibi;

    public RicompensaPerRarita(Catalogo catalogo) {
        Objects.requireNonNull(catalogo, "catalogo");
        this.strumenti = catalogo.strumenti().stream()
                .sorted(Comparator.comparingDouble(StrumentoCattura::bonusCattura))
                .toList();
        this.cibi = catalogo.cibi().stream()
                .sorted(Comparator.comparingDouble(Cibo::bonusEsca))
                .toList();
    }

    @Override
    public Map<Oggetto, Integer> perCattura(Animale animale) {
        int livello = animale.getRarita().ordinal();
        Map<Oggetto, Integer> premio = new LinkedHashMap<>();
        if (!strumenti.isEmpty()) {
            premio.put(scegli(strumenti, livello), STRUMENTI_PER_CATTURA);
        }
        if (!cibi.isEmpty()) {
            premio.put(scegli(cibi, livello), CIBI_PER_CATTURA);
        }
        return Collections.unmodifiableMap(premio);
    }

    private static <T> T scegli(List<T> ordinati, int livello) {
        return ordinati.get(Math.min(livello, ordinati.size() - 1));
    }
}
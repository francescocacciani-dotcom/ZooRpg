package it.unicam.cs.mpgc.rpg130131.model.inventory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Zaino del giocatore: tiene il conteggio degli oggetti posseduti. */
public class Inventario {

    private final Map<Oggetto, Integer> contenuto = new LinkedHashMap<>();

    /**
     * @throws IllegalArgumentException se la quantità non è positiva
     */
    public void aggiungi(Oggetto oggetto, int quantita) {
        Objects.requireNonNull(oggetto, "oggetto");
        if (quantita <= 0) {
            throw new IllegalArgumentException("La quantità deve essere positiva: " + quantita);
        }
        contenuto.merge(oggetto, quantita, Integer::sum);
    }

    /**
     * Consuma un esemplare dell'oggetto.
     *
     * @return true se ce n'era almeno uno, false altrimenti
     */
    public boolean usa(Oggetto oggetto) {
        Integer disponibili = contenuto.get(Objects.requireNonNull(oggetto, "oggetto"));
        if (disponibili == null) {
            return false;
        }
        if (disponibili == 1) {
            contenuto.remove(oggetto);
        } else {
            contenuto.put(oggetto, disponibili - 1);
        }
        return true;
    }

    public int quantita(Oggetto oggetto) {
        return contenuto.getOrDefault(Objects.requireNonNull(oggetto, "oggetto"), 0);
    }

    public boolean contiene(Oggetto oggetto) {
        return quantita(oggetto) > 0;
    }

    /** @return vista non modificabile di tutto il contenuto, con le quantità */
    public Map<Oggetto, Integer> getContenuto() {
        return Collections.unmodifiableMap(contenuto);
    }

    /** @return il contenuto di un solo tipo di oggetto (es. solo {@code StrumentoCattura}) */
    public <T extends Oggetto> Map<T, Integer> getContenutoDi(Class<T> tipo) {
        Objects.requireNonNull(tipo, "tipo");
        return contenuto.entrySet().stream()
                .filter(e -> tipo.isInstance(e.getKey()))
                .collect(Collectors.toMap(
                        e -> tipo.cast(e.getKey()),
                        Map.Entry::getValue,
                        Integer::sum,
                        LinkedHashMap::new));
    }
}
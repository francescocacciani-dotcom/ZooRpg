package it.unicam.cs.mpgc.rpg130131.model.player;

import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;

import java.util.Objects;

/** Il giocatore: ha un nome, si trova in una posizione della mappa e porta con sé un inventario. */
public class Giocatore {

    private final String nome;
    private final Inventario inventario;
    private Posizione posizione;

    /**
     * @throws IllegalArgumentException se il nome è vuoto
     */
    public Giocatore(String nome, Posizione posizioneIniziale, Inventario inventario) {
        Objects.requireNonNull(nome, "nome");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        this.nome = nome;
        this.posizione = Objects.requireNonNull(posizioneIniziale, "posizioneIniziale");
        this.inventario = Objects.requireNonNull(inventario, "inventario");
    }

    public String getNome() {
        return nome;
    }

    public Posizione getPosizione() {
        return posizione;
    }

    public Inventario getInventario() {
        return inventario;
    }

    /** Sposta il giocatore. Non controlla i limiti della mappa: spetta a chi coordina il gioco. */
    public void spostaA(Posizione nuovaPosizione) {
        this.posizione = Objects.requireNonNull(nuovaPosizione, "nuovaPosizione");
    }
}
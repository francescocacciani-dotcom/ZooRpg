package it.unicam.cs.mpgc.rpg130131.model.player;

import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;

import java.util.Objects;

/** Il giocatore: ha un nome, una posizione sulla mappa, un inventario e delle monete. */
public class Giocatore {

    private final String nome;
    private final Inventario inventario;
    private Posizione posizione;
    private int monete;

    /** Crea un giocatore senza monete.*/
    public Giocatore(String nome, Posizione posizioneIniziale, Inventario inventario) {
        this(nome, posizioneIniziale, inventario, 0);
    }

    /**
     * @throws IllegalArgumentException se il nome è vuoto o le monete sono negative
     */
    public Giocatore(String nome, Posizione posizioneIniziale, Inventario inventario, int monete) {
        Objects.requireNonNull(nome, "nome");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        if (monete < 0) {
            throw new IllegalArgumentException("Le monete non possono essere negative: " + monete);
        }
        this.nome = nome;
        this.posizione = Objects.requireNonNull(posizioneIniziale, "posizioneIniziale");
        this.inventario = Objects.requireNonNull(inventario, "inventario");
        this.monete = monete;
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

    public int getMonete() {
        return monete;
    }

    /** Sposta il giocatore. Non controlla i limiti della mappa: spetta a chi coordina il gioco. */
    public void spostaA(Posizione nuovaPosizione) {
        this.posizione = Objects.requireNonNull(nuovaPosizione, "nuovaPosizione");
    }

    /**
     * @throws IllegalArgumentException se l'importo non è positivo
     */
    public void aggiungiMonete(int importo) {
        if (importo <= 0) {
            throw new IllegalArgumentException("L'importo deve essere positivo: " + importo);
        }
        monete += importo;
    }

    /**
     * Spende monete se ce ne sono abbastanza.
     *
     * @return true se il pagamento è avvenuto, false se le monete non bastano (nulla cambia)
     * @throws IllegalArgumentException se l'importo non è positivo
     */
    public boolean spendi(int importo) {
        if (importo <= 0) {
            throw new IllegalArgumentException("L'importo deve essere positivo: " + importo);
        }
        if (importo > monete) {
            return false;
        }
        monete -= importo;
        return true;
    }
}
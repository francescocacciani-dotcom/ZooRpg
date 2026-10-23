package it.unicam.cs.mpgc.rpg130131.model.zoo;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Recinto di uno zoo: ospita animali compatibili con il suo bioma, fino alla capienza massima. */
public class Recinto {

    private final String nome;
    private final BiomaEnum bioma;
    private final int capienza;
    private final List<Animale> animali = new ArrayList<>();

    /**
     * @throws IllegalArgumentException se il nome è vuoto o la capienza non è positiva
     */
    public Recinto(String nome, BiomaEnum bioma, int capienza) {
        Objects.requireNonNull(nome, "nome");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        if (capienza <= 0) {
            throw new IllegalArgumentException("La capienza deve essere positiva: " + capienza);
        }
        this.nome = nome;
        this.bioma = Objects.requireNonNull(bioma, "bioma");
        this.capienza = capienza;
    }

    public String getNome() {
        return nome;
    }

    public BiomaEnum getBioma() {
        return bioma;
    }

    public int getCapienza() {
        return capienza;
    }

    public int getPostiLiberi() {
        return capienza - animali.size();
    }

    public boolean isPieno() {
        return getPostiLiberi() == 0;
    }

    /** @return vista non modificabile degli animali ospitati */
    public List<Animale> getAnimali() {
        return Collections.unmodifiableList(animali);
    }

    /** @return true se c'è posto, il bioma nativo è compatibile e l'animale non è già presente */
    public boolean puoOspitare(Animale animale) {
        Objects.requireNonNull(animale, "animale");
        return !isPieno()
                && animale.getBiomaNativo() == bioma
                && !animali.contains(animale);
    }

    /**
     * @throws IllegalStateException se l'animale non può essere ospitato
     */
    public void aggiungi(Animale animale) {
        if (!puoOspitare(animale)) {
            throw new IllegalStateException(
                    "Il recinto \"%s\" non può ospitare %s".formatted(nome, animale));
        }
        animali.add(animale);
    }

    /** @return true se l'animale era presente ed è stato rimosso */
    public boolean rimuovi(Animale animale) {
        return animali.remove(animale);
    }
}
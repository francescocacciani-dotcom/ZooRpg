package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.GameController;

/** Un componente grafico che sa ridisegnarsi leggendo lo stato corrente della partita. */
@FunctionalInterface
public interface VistaAggiornabile {

    void aggiorna(GameController controller);
}

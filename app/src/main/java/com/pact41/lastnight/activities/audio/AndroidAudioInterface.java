package com.pact41.lastnight.activities.audio;

/**
 * Created by Paul-Elian on 04/02/2017.
 */

public interface AndroidAudioInterface {
    /**
     * Ecrit les donnees des mouvements de l’invite au cours d’une soiree dans un fichier
     * @param fileName Le nom du fichier
     */
    public void writeMovesIntoFile(String fileName);

    /**
     * Ecrit les resultats des calculs de correlation mouvements de l’invite / tempo pendant la soiree dans un fichier
     * @param fileName Le nom du fichier
     */
    public void writeResultsIntoFile(String fileName);

}

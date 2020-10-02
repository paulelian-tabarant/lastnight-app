package com.pact41.lastnight.client_server;

import android.graphics.Bitmap;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Paul-Elian on 04/02/2017.
 */

/* Interface de connexion serveur */
// Méthodes commentées : pas encore opérationnelles
public interface AndroidClientServerInterface {

    /**
     * Permet a un utilisateur de se connecter sur son compte personnel
     * @param myId L’identifiant de l’utilisateur
     * @param myPassWord Son mot de passe
     * @return true si la connexion est reussie
     */
    boolean login(String myId, String myPassWord);

    /**
     * Ajoute un utilisateur a la BdD
     * @param myId L’identifiant de l’utilisateur
     * @param firstName Son prenom
     * @param lastName Son nom
     * @param email Son adresse e-mail
     * @param myPassword Son mot de passe
     */
    public boolean addUtilisateur(String myId, String firstName, String lastName, String email, String myPassword);

    /**
     * Recupere le prenom d'un utilisateur
     * @param userId Son identifiant
     * @return Son prenom
     */
    public String getUserFstNme(String userId);

    /**
     * Recupere le nom d'un utilisateur
     * @param userId Son identifiant
     * @return Son nom
     */
    public String getUserLstNme(String userId);

    /**
     * Recupere l'adresse e-mail d'un utilisateur
     * @param userId Son identifiant
     * @return Son adresse e-mail
     */
    public String getUserEmail(String userId);

    /**
     * Recupere le mot de passe d'un utilisateur
     * @param userId Son identifiant
     * @return Son mot de passe
     */
    public String getUserPssWd(String userId);

    /**
     * Modifie les champs renseignes pour l'utilisateur d'identifiant myId
     * @param myId L'identifiant de l'utilisateur
     * @param firstName Son prenom s'il souhaite le modifier, null sinon
     * @param lastName Son nom de famille s'il souhaite le modifier, null sinon
     * @param email Son adresse mail s'il souhaite la modifier, null sinon
     * @param myPassWord Son mot de passe s'il souhaite le modifier, null sinon
     * @return vrai si la modification s'est effectuee correctement
     */
    public boolean changeUtilisateurInfo(String myId, String firstName, String lastName, String email, String myPassWord);

    /**
     * Verifie si l'utilisateur au nom adminId est inscrit comme administrateur a la soiree eventId
     * @param adminId Le nom d'utilisateur a tester
     * @param eventId Le nom de l'evenement
     * @return
     */
    public boolean isAdmin(String adminId, String eventId);

    /**
     * Cree un evenement dans la BdD
     * @param eventID Le nom de l’evenement
     * @param eventPlace Le lieu de l’evenement
     * @param eventPrice Le prix d’acces a la soiree
     * @param eventHour Les horaires sous la forme « hh:mm / hh:mm »
     * @param creator Le nom d’utilisateur de l’organisateur
     * @return true si l'action a bien ete prise en compte par le serveur
     */
    public boolean createEvent(String eventID, String eventPlace, int eventPrice, String eventHour, String creator);

    /**
     * Associe un administrateur a un evenement cree dans la BdD
     * @param adminId Le nom d’utilisateur de l’admin designe
     * @param eventId L'identifiant de l'evenement
     * @return true si l'action a bien ete prise en compte par le serveur
     */
    public boolean addAdminToEvent(String adminId, String eventId);

    /**
     * Recupere le lieu d'un evenement a partir d'un identifiant
     * @param eventID
     * @return Une chaine de caracteres decrivant le lieu
     */
    public String getEventPlace(String eventID);

    /**
     * Recupere le prix d'un evenement a partir d'un identifiant
     * @param eventID
     * @return Un entier donnant le prix
     */
    public int getEventPrice(String eventID);

    /**
     * Recupere les horaires d'un evenement a partir d'un identifiant
     * @param eventID
     * @return Une chaine de caracteres decrivant les horaires
     */
    public String getEventHour(String eventID);

    /**
     *  Modifie les informations liees a un evenement dans la BdD
     * @param eventID Le nom de l’evenement
     * @param eventPlace Le lieu de l’evenement, null si non modifie
     * @param eventPrice Le prix de la soiree, null si non modifie
     * @param eventHour Les horaires sous la forme « hh:mm / hh:mm », null si non modifies
     * @return true si l'action a bien ete prise en compte par le serveur
     */
    public boolean changeEvent(String eventID, String eventPlace, int eventPrice, String eventHour, String creator);

    /**
     * Ajoute un utilisateur a une soiree en cours
     * @param myId Le nom d'utilisateur de l'utilisateur en question
     * @param eventId Le nom de la soiree dans laquelle il souhaite s'authentifier
     */
    //public boolean joinEvent(String myId, String eventId);

    /**
     * Envoie un nouveau sondage a la base de donnees
     * @param sondage un tableau contenant toutes les infos sur le sondage
     *                0: Nom de l'evenement
     *                1: Titre du sondage
     *                2-N: Les options de reponse
     * @return True si le sondage a pu etre cree
     */
    public boolean createSondage(String[] sondage);


    /**
     * Envoie un vote pour une option au sondage en cours a une soiree
     * @param eventName L'evenement durant lequel le sondage a lieu
     * @param choix L'indice du choix par rapport a la liste de choix envoyee par le serveur
     * @return True si le vote a bien ete pris en compte sur le serveur
     */
    public boolean voteSondage(String eventName, String choix);

    /**
     * Renvoie les options du sondage en cours sur un evenement considere
     * @param eventId Le nom de l'evenement en question
     * @return Une liste contenant :
     * 0: le titre du sondage
     * 1-N: les options du sondage
     */
    public String[] getSondageChoix(String eventId);

    /**
     * Renvoie le nombre de votes pour chaque option du sondage en cours dans une soiree donnee
     * @param eventId La soiree en question
     * @return Un tableau avec le nombre de voFates pour chaque option
     */
    public int[] getSondageVote(String eventId);

    /**
     * Poste un message sur le serveur destine a tous les invites d’une soiree en cours
     * @param eventID Le nom de la soiree en cours
     * @param message Le message a diffuser
     * @return true si l'action a bien ete prise en compte par le serveur
     */
    public boolean addMessage(String eventID, String message);

    /**
     * Recupere les messages postes par le DJ et l’organisateur dans l’evenement en cours
     * @param eventID Le nom de la soiree en cours
     * @return La liste des messages postes
     */
    public String[] getAdminMessages(String eventID);


    /* -------------------------- PAN4 --------------------------- */

    /**
     * Ajoute une photo a l’album d’une soiree
     * @param eventId Le nom de l'evenement dans la BdD
     * @param photo Les donnees de la photo qu’il souhaite envoyer
     * @return true si l'action a bien ete prise en compte par le serveur
     */
    //public boolean addPartyPhoto(String eventID, Bitmap photo);

    /**
     * Ajoute une photo au profil de l'utilisateur
     * @param myId Le nom d'utilisateur
     * @param initialImage Un objet Bitmap representant la photo en question
     * @return true si l'action a bien ete prise en compte par le serveur
     */
    //public boolean addProfilePhoto(String myId, Bitmap initialImage);

    /**
     * Recupere la photo de profil d'un utilisateur donne sur le serveur
     * @param myId Le nom d'utilisateur considere
     * @return Un objet Bitmap contenant la representation de sa photo
     */
    //public Bitmap getProfilePhoto(String myId);

    /**
     * Recupere la n-ieme photo associee a un evenement dans la BdD
     * @param partyId Le nom de la soiree consideree
     * @param photoNumber Le rang n de la photo dans l'album de la soiree
     * @return Un objet bitmap contenant la photo en question
     */
    //public Bitmap getPartyPhoto(String partyId,String photoNumber);

    /**
     * Permet a un utilisateur d’ajouter un ami sur le serveur LastNight
     * @param myId Le nom d’utilisateur de la personne
     * @param friendName Le nom d’utilisateur de son ami
     * @return true si l'action a bien ete prise en compte par le serveur
     */
    public boolean addFriend(String myId, String friendName);

    /**
     * Verifie si l'utilisateur connecte est ami avec un utilisateur passe en parametre
     * @param myId L'utilisateur connecte
     * @param friendId Le nom d'utilisateur de la personne a tester
     * @return vrai si les 2 sont amis
     */
    boolean isFriend(String myId, String friendId);

    /**
     * Supprime une amitie entre 2 utilisateurs
     * @param myId L’utilisateur qui souhaite supprimer l’amitie
     * @param friendName Le nom d’utilisateur de son ami
     * @return true si l'action a bien ete prise en compte par le serveur
     */
    public boolean deleteFriend(String myId, String friendName);

    /**
     * Envoie le score d'un participant a l'evenement sur son dernier calcul de tempo
     * @param eventName L'evenement auquel se trouve l'invite
     * @param userId Son nom d'utilisateur
     * @param score Son dernier score
     * @return True si l'envoi a bien ete pris en compte dans la BdD
     */
    //public boolean sendScoreToEvent(String eventName, String userId, double score);

    /**
     * Recupere le score d'un utilisateur present a une soiree donnee
     * @param eventName L'evenement auquel l'utilisateur se trouve
     * @param userId Son nom d'utilisateur
     * @return Son dernier score enregistre dans la BdD
     */
    //public double getUserScore(String eventName, String userId);

    /**
     * Recupere le classement des invites a une soiree en fonction de leur score respectif
     * @param eventName Le nom de l'evenement considere
     * @return Un tableau ordonne contenant le classement des invites (avec leur nom d'utilisateur)
     */
    //public String[] getScoreRanking(String eventName);
}

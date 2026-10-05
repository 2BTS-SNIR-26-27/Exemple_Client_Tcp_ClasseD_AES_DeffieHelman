/*
 * Copyright (c) 2026 Michaël Moser.
 *
 * Auteur : Michaël Moser
 * Tous droits réservés.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.astier.bts.client_tcp_prof.tcp;


import com.astier.bts.client_tcp_prof.HelloController;
import com.astier.bts.client_tcp_prof.aes.Aes_cbc;
import com.astier.bts.client_tcp_prof.deffie_hellman.DeffieHellman;
import com.astier.bts.client_tcp_prof.modeles.ConfigAES;
import exceptions.DiagnosticException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.paint.Color.RED;

/**
 * @author Michael
 */
public class TCP extends Thread {
    public Socket socket;
    int port;
    InetAddress serveur;
    boolean marche = false;
    public OutputStream outBin;
    public InputStream inBin;
    HelloController fxmlCont;
    byte[] bufferEntreeBin = new byte[65535];
    ConfigAES configAES;
    Aes_cbc aes_cbc;
    DeffieHellman clefIV;
    byte[] paramClefEtIV;
    public TCP(InetAddress serveur, int port, HelloController fxmlCont) throws FileNotFoundException {
        this.port = port;
        this.serveur = serveur;
        this.fxmlCont = fxmlCont;

    }


    public void connection() {
        if (this.isAlive())
            return;
        try {
            this.socket = new Socket();
            SocketAddress endpoint = new InetSocketAddress(serveur, port);
            this.socket.connect(endpoint, 2000);
            //this.socket.setSoTimeout(10*1000);
            outBin = socket.getOutputStream();
            inBin = socket.getInputStream();
            //recuper la clef et iv
            clefIV = new DeffieHellman(this,1024);
            paramClefEtIV = clefIV.recuperationParam();
            configAES = new ConfigAES(16,Arrays.copyOfRange(paramClefEtIV,1,17), Arrays.copyOfRange(paramClefEtIV,17,33));
            aes_cbc = new Aes_cbc(configAES.mdpFormat(),configAES.ivFormat());
            System.out.printf("""
                    RECUPERATION DES CLEFS >  
                                        Clef (hex): %s
                                        IV  (hex) : %s
                """.formatted(clefIV.toHex(configAES.mdpFormat()),clefIV.toHex(configAES.ivFormat())));
            Platform.runLater(() -> {
                fxmlCont.textAreaReponses.appendText("    \nINFO RECUPERATION DES CLEFS DE CRYPTAGE >\n");
                fxmlCont.textAreaReponses.appendText("      Clef (hex) : " + clefIV.toHex(configAES.mdpFormat()) + "\n");
                fxmlCont.textAreaReponses.appendText("      IV   (hex) : " + clefIV.toHex(configAES.ivFormat()) + "\n");
            });
            marche = true;
            this.start();
        } catch (Exception e) {
            updateMessage(DiagnosticException.afficheException(e));
        }
    }

    public void deconnection() throws InterruptedException {
        if (this.isAlive()) {
            try {
                fxmlCont.voyant.setFill(RED);
                outBin.write("exit".getBytes(StandardCharsets.UTF_8));
                marche = false;
                Thread.sleep(1000);
                outBin.close();
                inBin.close();
                socket.close();
                fxmlCont.deconnecter.fire();
            } catch (Exception ex) {
                updateMessage(DiagnosticException.afficheException(ex));

            }
        }
    }

    public void requette(String laRequette) throws IOException {
        outBin.write(aes_cbc.cryptage(laRequette.getBytes(StandardCharsets.UTF_8)));
        System.out.println("la requette " + laRequette);
    }

    public void run() {
        String message;
        while (marche) {
            try {
                System.out.println("en attente message");
                int nbLusBin = inBin.read(bufferEntreeBin);
                System.out.println("nbLusBin = " + nbLusBin);
                if (nbLusBin == -1) {
                    marche = false;
                    break;
                }
                byte[] trameUtile = Arrays.copyOfRange(bufferEntreeBin, 0, nbLusBin);

                message = new String(aes_cbc.decryptage(trameUtile));
                if (message.length() != 0) {
                    System.out.println("    MESSAGE SERVEUR >  \n      " + message + "\n");
                    updateMessage(message);
                }

            } catch (Exception ex) {
                updateMessage(DiagnosticException.afficheException(ex));
            }
        }
    }


    /*
    Pour déclencher une opération graphique en dehors du thread graphique  utiliser
    javafx.application.Platform.runLater(java.lang.Runnable)
    Cette méthode permet d'éxécuter le code du runnable par le thread graphique de JavaFX.
    */
    protected void updateMessage(String message) {
        Platform.runLater(() -> fxmlCont.textAreaReponses.appendText("    MESSAGE SERVEUR >  \n      " + message + "\n"));
    }



}
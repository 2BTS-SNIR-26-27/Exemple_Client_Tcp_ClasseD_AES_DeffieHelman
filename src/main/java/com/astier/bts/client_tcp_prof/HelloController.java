/*
 * Copyright (c) 2026 Michaël Moser.
 *
 * Auteur : Michaël Moser
 * Tous droits réservés.
 */

package com.astier.bts.client_tcp_prof;

import com.astier.bts.client_tcp_prof.modeles.Ipv4;
import com.astier.bts.client_tcp_prof.multicast_diffusion.MulticastDiffusion;
import com.astier.bts.client_tcp_prof.outils.ScanInterfaces;
import com.astier.bts.client_tcp_prof.tcp.TCP;
import com.astier.bts.totp.Totp;
import exceptions.DiagnosticException;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;

import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import static javafx.scene.paint.Color.*;

public class HelloController implements Initializable {
    static public TCP tcp;
    static boolean enRun = false;
    public Button button;
    public Button connecter;
    public Button deconnecter;
    public TextField textFieldIP;
    public TextField textFieldPort;
    public TextField textFieldRequette;
    public Circle voyant;
    public TextArea textAreaReponses;
    public ChoiceBox<Ipv4> choixInterface;
    public Button raz;
    MulticastDiffusion multicastDiffusion;
    String adresse, port, interfaceSelectionnee;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        voyant.setFill(RED);
        try {
            textAreaReponses.setStyle("-fx-control-inner-background: #ff5555;");
            detectionIntefaces();
            button.setOnAction(event -> envoyer());
            connecter.setOnAction(event -> connecter());
            deconnecter.setOnAction(event -> deconnecter());
            raz.setOnAction(event -> raz());
            controleTotp();
            choixInterface.getSelectionModel().selectedItemProperty().addListener((observable, ancienneValeur, nouvelleValeur) -> {
                if (nouvelleValeur != null) {
                    System.out.println("Interface sélectionnée : " + nouvelleValeur.nomInterface());
                    System.out.println("IP : " + nouvelleValeur.ipv4());
                    System.out.println("Type : " + nouvelleValeur.interfaceType());
                    interfaceSelectionnee = nouvelleValeur.nomInterface();
                }
                new Thread(() -> recupConfig()).start();
            });
        } catch (Exception e) {
            textAreaReponses.setText(DiagnosticException.afficheException(e));
        }
    }

    private void controleTotp() {
        Totp totp = new Totp();
        boolean codeOk = false;
        while (!codeOk) {
            // Créer un TextInputDialog pour la saisie de texte
            TextInputDialog inDialog = new TextInputDialog();
            inDialog.setTitle("Controle autorisation");
            inDialog.setHeaderText("");
            inDialog.setContentText("Code OTP :");
            Optional<String> textIn = inDialog.showAndWait();
            try {
                if (textIn.isPresent()) {
                    codeOk = totp.testCodeTOTP(Integer.parseInt(textIn.get()));
                } else System.exit(0);
            } catch (Exception e) {
            }
        }
    }

    private void raz() {
        textAreaReponses.clear();
    }

    private void detectionIntefaces() {
        try {
            ArrayList<Ipv4> ipv4s = ScanInterfaces.getSystemIP();
            choixInterface.getItems().clear();
            choixInterface.getItems().addAll(ipv4s);
        } catch (Exception e) {
            textAreaReponses.setText(DiagnosticException.afficheException(e));
        }
    }

    private void recupConfig() {
        try {
            long debut = System.currentTimeMillis();
            long timeout = 10000; //10s
            multicastDiffusion = new MulticastDiffusion(interfaceSelectionnee);
            while ((!multicastDiffusion.ok) && ((System.currentTimeMillis() - debut) < timeout)) {
                voyant.setFill(GREEN);
                Thread.sleep(500);
                voyant.setFill(RED);
                Thread.sleep(500);
                textAreaReponses.setText("Recupération des configs réseau...");
            }
            if (!multicastDiffusion.ok) {
                if (multicastDiffusion.dsReponse != null) {
                    multicastDiffusion.dsReponse.close();
                }
                voyant.setFill(RED);
                textAreaReponses.setText("Aucune configuration réseau reçue après 10 secondes.");
                return;
            }
            port = multicastDiffusion.laConnexion.portTCP();
            adresse = multicastDiffusion.laConnexion.adresseServeur();
            multicastDiffusion.dsReponse.close();
            textFieldIP.setText(adresse);
            textFieldPort.setText(port);
            textAreaReponses.setText("Configuration réseau récupéré!");

        } catch (Exception e) {
            textAreaReponses.setText(DiagnosticException.afficheException(e));
        }
    }

    private void envoyer() {
        try {
            String requette = textFieldRequette.getText();
            if (requette.isEmpty()) return;
            if (requette.equalsIgnoreCase("exit")) tcp.deconnection();
            tcp.requette(requette);
        } catch (Exception e) {
            textAreaReponses.setText(DiagnosticException.afficheException(e));
        }
    }

    private void deconnecter() {
        try {
            if (!enRun) return;
            tcp.deconnection();
            enRun = false;
            textAreaReponses.setStyle("-fx-control-inner-background: #ff5555;");

        } catch (Exception e) {
            textAreaReponses.setText(DiagnosticException.afficheException(e));
        }
    }

    private void connecter() {
        try {
            String adresseDuServeur = textFieldIP.getText();
            String portDuServeur = textFieldPort.getText();
            if (adresseDuServeur.isEmpty() || portDuServeur.isEmpty() || enRun) return;
            tcp = new TCP(InetAddress.getByName(adresseDuServeur), Integer.parseInt(portDuServeur), this);
            tcp.connection();
            if (tcp.socket.isConnected()) {
                enRun = true;
                voyant.setFill(LIME);
                textAreaReponses.setStyle(null);
            }
        } catch (Exception e) {
            textAreaReponses.setText(DiagnosticException.afficheException(e));
        }
    }

}

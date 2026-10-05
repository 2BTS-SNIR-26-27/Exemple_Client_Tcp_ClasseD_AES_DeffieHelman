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
package com.astier.bts.client_tcp_prof.multicast_diffusion;

import com.astier.bts.client_tcp_prof.modeles.Connexion;
import exceptions.DiagnosticException;

import java.io.IOException;
import java.net.*;

/**
 *
 * @author Michael
 */
public class MulticastDiffusion {

    public DatagramPacket dp;
    public DatagramSocket dsReponse;
    public Connexion laConnexion;
    public boolean ok = false;
    public String portServeur = "";
    InetAddress ia;
    byte[] data = "Tu es qui?".getBytes();
    byte[] bufferReponse;
    int port;
    int portReponse;
    byte ttl;
    MulticastSocket ms;

    public MulticastDiffusion(String interfaceName) throws IOException {
        ia = InetAddress.getByName("224.0.0.250");
        bufferReponse = new byte[512];
        port = 5555;
        portReponse = 5556;
        ttl = 60;

        ms = new MulticastSocket();
        NetworkInterface netIf = NetworkInterface.getByName(interfaceName);
        ms.setNetworkInterface(netIf);
        ms.setTimeToLive(ttl);
        DatagramPacket dp = new DatagramPacket(data, data.length, ia, port); //multicast 224.0.0.250:5555
        ms.send(dp);

        new Thread(() -> {
            try {
                dsReponse = new DatagramSocket(portReponse); //udp sur port 5556
                DatagramPacket in = new DatagramPacket(bufferReponse, bufferReponse.length);
                System.out.println("attente réponse");
                dsReponse.receive(in);
                System.out.println("traitement réponse");
                String s = new String(in.getData(), 0, in.getLength());
                String[] chaine = s.split(";");
                portServeur = chaine[1];
                System.out.println(in.getAddress() + " sur le port " + in.getPort() + " dit " + s);
                laConnexion = new Connexion(chaine[0], chaine[1], chaine[2]);
                ok = true;
                //ms.leaveGroup(ia);
                ms.close();
            } catch (Exception ex) {
                System.out.println(DiagnosticException.afficheException(ex));
            }
        }).start();
    }

}

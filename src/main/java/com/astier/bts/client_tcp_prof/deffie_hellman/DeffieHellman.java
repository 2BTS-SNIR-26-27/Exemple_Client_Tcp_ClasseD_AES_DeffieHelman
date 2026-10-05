/*
 * Copyright (c) 2026 Michaël Moser.
 *
 * Auteur : Michaël Moser
 * Tous droits réservés.
 */

package com.astier.bts.client_tcp_prof.deffie_hellman;

import com.astier.bts.client_tcp_prof.tcp.TCP;
import exceptions.DiagnosticException;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

public class DeffieHellman {
    TCP tcp;
    int nbsBits,taille;
    BigInteger p,a,g,B,K,A;
    byte[] byte_B = new byte[65535];

    public DeffieHellman(TCP tcp, int nbsBits) {
        this.nbsBits = nbsBits;
        this.tcp = tcp;
        a = new BigInteger(nbsBits, new SecureRandom());
        p = BigInteger.probablePrime(nbsBits, new SecureRandom());
        do{
            g = new BigInteger(nbsBits, new SecureRandom());
        }while (g.compareTo(p)>0);
    }

    public byte[] recuperationParam(){
        byte[] byteArray;
        try {
            System.out.println("Connexion au serveur: " + tcp.socket.getRemoteSocketAddress().toString());
            // Affiche la clef privée du client
            System.out.println("Serveur : clef privée = " + a);
            //envoi des infos partagées p,g
            tcp.outBin.write(p.toByteArray());
            System.out.println("Client : p = " + p);
            Thread.sleep(50);
            tcp.outBin.write(g.toByteArray());
            System.out.println("Client : g = " + g);
            Thread.sleep(50);
            A = g.modPow(a, p); //calcule A
            System.out.println("Client : A = " + A);
            Thread.sleep(50);
            tcp.outBin.write(A.toByteArray()); // envoie A
            taille=tcp.inBin.read(byte_B);
            B= new BigInteger(Arrays.copyOfRange(byte_B, 0, taille));
            System.out.println("Serveur : Clef publique = " + B);
            K = B.modPow(a, p);// calcul de la clef secréte
            byteArray = K.toByteArray();
            System.out.printf("""
                    Clé totale du cryptage symétrique   ----> %s
                    En héxadecimal                      ----> %s
                    """.formatted(K, toHex(byteArray), new String(byteArray)));
            return byteArray;
        } catch (Exception e) {
            System.out.println(DiagnosticException.afficheException(e));
            return null;
        }
    }
    // Méthode pour convertir byte[] en hexadécimal
    public String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}

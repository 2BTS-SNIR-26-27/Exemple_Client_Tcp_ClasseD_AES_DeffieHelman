/*
 * Copyright (c) 2026 Michaël Moser.
 *
 * Auteur : Michaël Moser
 * Tous droits réservés.
 */

package com.astier.bts.client_tcp_prof.modeles;




import java.util.Arrays;


public record ConfigAES(int tailleEnOctects,byte[] mdp, byte[] iv) {

    public byte[] mdpFormat() {
        return Arrays.copyOfRange(mdp, 0, tailleEnOctects());
    }

    public byte[] ivFormat() {
        return Arrays.copyOfRange(iv, 0, tailleEnOctects());
    }

}


/*
 * Copyright (c) 2026 Michaël Moser.
 *
 * Auteur : Michaël Moser
 * Tous droits réservés.
 */

package com.astier.bts.client_tcp_prof.modeles;

public record Ipv4(String interfaceType, String nomInterface, String ipv4) {
    @Override
    public String toString() {
        return nomInterface;
    }
}

package com.ford.vehicleapi.domain;

/**
 * Perfis de acesso da aplicação.
 * <ul>
 *   <li>ADMIN - gestão completa do catálogo e dos usuários.</li>
 *   <li>DEALER - concessionária: cadastra/edita o catálogo e gerencia test drives.</li>
 *   <li>CUSTOMER - cliente: agenda e acompanha seus próprios test drives.</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    DEALER,
    CUSTOMER
}

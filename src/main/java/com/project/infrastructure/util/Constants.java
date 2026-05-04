package com.project.infrastructure.util;

public final class Constants {

    private Constants() {}

    public static final String ADMIN_USERNAME  = "admin";
    public static final String ADMIN_NOMBRE    = "Admin";
    public static final String ADMIN_APELLIDOS = "4M Drink Team";
    public static final String ADMIN_EMAIL     = "admin@4mdrinkteam.com";

    // Roles (Spring Security prepends ROLE_ automatically in hasRole())
    public static final String ROLE_ADMIN        = "ADMIN";
    public static final String ROLE_LIGA_OWNER   = "LIGA_OWNER";
    public static final String ROLE_EQUIPO_OWNER = "EQUIPO_OWNER";
    public static final String ROLE_JUGADOR      = "JUGADOR";

    /** @deprecated use ROLE_ADMIN */
    @Deprecated
    public static final String ADMIN_ROLE = ROLE_ADMIN;
}

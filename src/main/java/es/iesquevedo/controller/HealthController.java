package es.iesquevedo.controller;

public class HealthController {
    public static final String STATUS_OK = "OK";

    public String health() {
        return STATUS_OK;
    }
}

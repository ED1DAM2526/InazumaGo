package es.iesquevedo.model;

import java.util.Objects;

/**
 * Representa un jugador en una partida.
 */
public class Player {
    private String id;
    private String name;
    private boolean alive;
    private int position; // 0-11 (posición en el campo/tablero)

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.alive = true;
        this.position = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        if (position < 0 || position > 11) {
            throw new IllegalArgumentException("Posición debe estar entre 0 y 11");
        }
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", alive=" + alive +
                ", position=" + position +
                '}';
    }
}

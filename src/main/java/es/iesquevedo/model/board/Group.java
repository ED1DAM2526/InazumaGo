package es.iesquevedo.model.board;

import java.util.*;

/**
 * Representa un grupo (cadena) de piedras del mismo color conectadas ortogonalmente
 */
public class Group {
    private final Stone color;
    private final Set<Position> stones;

    public Group(Stone color) {
        if (!color.isStone()) {
            throw new IllegalArgumentException("Group must have a stone color");
        }
        this.color = color;
        this.stones = new HashSet<>();
    }

    public Stone getColor() {
        return color;
    }

    public Set<Position> getStones() {
        return Collections.unmodifiableSet(stones);
    }

    public void addStone(Position pos) {
        stones.add(pos);
    }

    public void addAllStones(Collection<Position> positions) {
        stones.addAll(positions);
    }

    public int getSize() {
        return stones.size();
    }

    /**
     * Calcula las libertades (intersecciones vacías adyacentes) de este grupo
     */
    public int countLiberties(Board board) {
        Set<Position> liberties = new HashSet<>();
        for (Position stone : stones) {
            for (Position neighbor : stone.getOrthogonalNeighbors()) {
                if (board.isEmpty(neighbor)) {
                    liberties.add(neighbor);
                }
            }
        }
        return liberties.size();
    }

    /**
     * Obtiene las libertades como conjunto
     */
    public Set<Position> getLibertyPositions(Board board) {
        Set<Position> liberties = new HashSet<>();
        for (Position stone : stones) {
            for (Position neighbor : stone.getOrthogonalNeighbors()) {
                if (board.isEmpty(neighbor)) {
                    liberties.add(neighbor);
                }
            }
        }
        return liberties;
    }

    /**
     * Verifica si el grupo está vivo (tiene libertades)
     */
    public boolean isAlive(Board board) {
        return countLiberties(board) > 0;
    }

    /**
     * Verifica si el grupo tiene dos ojos inequívocos
     * Implementación simplificada: contar regiones de libertad separadas
     */
    public int countEyes(Board board) {
        Set<Position> liberties = getLibertyPositions(board);
        Set<Position> visited = new HashSet<>();
        int eyeCount = 0;

        for (Position liberty : liberties) {
            if (!visited.contains(liberty)) {
                // Flood-fill desde esta libertad
                Set<Position> eye = floodFillEye(liberty, board, visited);
                if (eye.size() > 0) {
                    eyeCount++;
                }
            }
        }
        return eyeCount;
    }

    /**
     * Flood-fill para encontrar ojos (grupos de libertades conectadas)
     */
    private Set<Position> floodFillEye(Position start, Board board, Set<Position> visited) {
        Set<Position> eye = new HashSet<>();
        Queue<Position> queue = new LinkedList<>();
        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            eye.add(current);

            for (Position neighbor : current.getOrthogonalNeighbors()) {
                if (!visited.contains(neighbor) && board.isEmpty(neighbor)) {
                    // Verificar que solo está rodeado por este grupo
                    if (isAdjacentOnlyToThisGroup(neighbor, board)) {
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
        }
        return eye;
    }

    /**
     * Verifica que una posición está adyacente solo a piedras de este grupo
     */
    private boolean isAdjacentOnlyToThisGroup(Position pos, Board board) {
        for (Position neighbor : pos.getOrthogonalNeighbors()) {
            Stone stone = board.getStone(neighbor);
            if (stone.isStone() && stone != this.color) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        Group group = (Group) o;
        return color == group.color && stones.equals(group.stones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, stones);
    }

    @Override
    public String toString() {
        return String.format("Group{color=%s, size=%d, stones=%s}", 
            color, stones.size(), stones);
    }
}


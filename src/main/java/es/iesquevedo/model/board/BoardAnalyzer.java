package es.iesquevedo.model.board;

import java.util.*;

/**
 * Analizador del tablero: encuentra grupos, libertades, territorios y capturas
 */
public class BoardAnalyzer {

    /**
     * Encuentra todos los grupos en el tablero
     */
    public List<Group> findAllGroups(Board board) {
        Map<Position, Group> groupMap = new HashMap<>();
        boolean[][] visited = new boolean[9][9];

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Position pos = Position.of(r, c);
                if (!visited[r][c] && !board.isEmpty(pos)) {
                    Group group = floodFillGroup(board, pos, visited);
                    for (Position stone : group.getStones()) {
                        groupMap.put(stone, group);
                    }
                }
            }
        }

        return new ArrayList<>(new HashSet<>(groupMap.values()));
    }

    /**
     * Flood-fill para encontrar un grupo (cadena de piedras del mismo color)
     */
    private Group floodFillGroup(Board board, Position start, boolean[][] visited) {
        Stone color = board.getStone(start);
        Group group = new Group(color);
        Queue<Position> queue = new LinkedList<>();
        queue.offer(start);
        visited[start.getRow()][start.getCol()] = true;

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            group.addStone(current);

            for (Position neighbor : current.getOrthogonalNeighbors()) {
                int nr = neighbor.getRow();
                int nc = neighbor.getCol();
                if (!visited[nr][nc] && board.getStone(neighbor) == color) {
                    visited[nr][nc] = true;
                    queue.offer(neighbor);
                }
            }
        }

        return group;
    }

    /**
     * Encuentra un grupo específico que contiene la posición
     */
    public Group findGroupAt(Board board, Position pos) {
        if (board.isEmpty(pos)) {
            return null;
        }
        boolean[][] visited = new boolean[9][9];
        return floodFillGroup(board, pos, visited);
    }

    /**
     * Detecta grupos capturados (sin libertades) en el tablero
     */
    public List<Group> findCapturedGroups(Board board) {
        List<Group> captured = new ArrayList<>();
        List<Group> allGroups = findAllGroups(board);

        for (Group group : allGroups) {
            if (group.countLiberties(board) == 0) {
                captured.add(group);
            }
        }

        return captured;
    }

    /**
     * Detecta grupos capturados adyacentes a una posición
     * (para optimizar después de un movimiento)
     */
    public List<Group> findCapturedGroupsAdjacentTo(Board board, Position lastMove) {
        List<Group> captured = new ArrayList<>();
        Stone lastColor = board.getStone(lastMove);

        for (Position neighbor : lastMove.getOrthogonalNeighbors()) {
            if (!board.isEmpty(neighbor) && board.getStone(neighbor) != lastColor) {
                Group group = findGroupAt(board, neighbor);
                if (group != null && group.countLiberties(board) == 0) {
                    captured.add(group);
                }
            }
        }

        return captured;
    }

    /**
     * Territorio: región de intersecciones vacías
     */
    public static class Territory {
        private final Set<Position> positions;
        private final Set<Stone> adjacentColors;

        public Territory(Set<Position> positions, Set<Stone> adjacentColors) {
            this.positions = positions;
            this.adjacentColors = adjacentColors;
        }

        public Set<Position> getPositions() {
            return Collections.unmodifiableSet(positions);
        }

        public Set<Stone> getAdjacentColors() {
            return Collections.unmodifiableSet(adjacentColors);
        }

        public boolean isNeutral() {
            return adjacentColors.size() != 1;
        }

        public Stone getOwner() {
            return adjacentColors.size() == 1 
                ? adjacentColors.iterator().next() 
                : null;
        }

        @Override
        public String toString() {
            return String.format("Territory{size=%d, owner=%s}", 
                positions.size(), getOwner());
        }
    }

    /**
     * Encuentra todos los territorios (regiones vacías) en el tablero
     */
    public List<Territory> findTerritories(Board board) {
        List<Territory> territories = new ArrayList<>();
        boolean[][] visited = new boolean[9][9];

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Position pos = Position.of(r, c);
                if (!visited[r][c] && board.isEmpty(pos)) {
                    Territory territory = floodFillTerritory(board, pos, visited);
                    territories.add(territory);
                }
            }
        }

        return territories;
    }

    /**
     * Flood-fill para encontrar un territorio (región de posiciones vacías)
     */
    private Territory floodFillTerritory(Board board, Position start, boolean[][] visited) {
        Set<Position> territory = new HashSet<>();
        Set<Stone> adjacentColors = new HashSet<>();
        Queue<Position> queue = new LinkedList<>();
        queue.offer(start);
        visited[start.getRow()][start.getCol()] = true;

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            territory.add(current);

            for (Position neighbor : current.getOrthogonalNeighbors()) {
                if (board.isEmpty(neighbor)) {
                    int nr = neighbor.getRow();
                    int nc = neighbor.getCol();
                    if (!visited[nr][nc]) {
                        visited[nr][nc] = true;
                        queue.offer(neighbor);
                    }
                } else {
                    // Marcar color adyacente
                    adjacentColors.add(board.getStone(neighbor));
                }
            }
        }

        return new Territory(territory, adjacentColors);
    }

    /**
     * Compara dos tableros
     */
    public boolean boardsEqual(Board board1, Board board2) {
        return board1.equals(board2);
    }
}


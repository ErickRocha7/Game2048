package com.jogo2048;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class Board {
    public static final int SIZE = 4;
    private int[][] grid;
    private Random random;
    private int score;
    private boolean won;

    // Pilhas para desfazer/refazer
    private final Deque<GameState> undoStack = new ArrayDeque<>();
    private final Deque<GameState> redoStack = new ArrayDeque<>();

    public Board() {
        grid = new int[SIZE][SIZE];
        random = new Random();
        reset();
    }

    public void reset() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                grid[i][j] = 0;
        score = 0;
        won = false;
        spawnRandomTile();
        spawnRandomTile();
        undoStack.clear();
        redoStack.clear();
    }

    public int getCell(int row, int col) {
        return grid[row][col];
    }

    public int getScore() {
        return score;
    }

    public boolean hasWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public void spawnRandomTile() {
        int emptyCount = 0;
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (grid[i][j] == 0)
                    emptyCount++;

        if (emptyCount == 0)
            return;

        int target = random.nextInt(emptyCount);
        int val = (random.nextInt(10) == 0) ? 4 : 2;

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (grid[i][j] == 0) {
                    if (target == 0) {
                        grid[i][j] = val;
                        return;
                    }
                    target--;
                }
    }

    // ---------- Métodos públicos de movimento (delegam ao centralizado) ----------
    public boolean moveLeft() {
        return move(Direction.LEFT);
    }

    public boolean moveRight() {
        return move(Direction.RIGHT);
    }

    public boolean moveUp() {
        return move(Direction.UP);
    }

    public boolean moveDown() {
        return move(Direction.DOWN);
    }

    /**
     * Método centralizado de movimento.
     * Gerencia o ciclo completo: salva o estado anterior, executa o deslocamento,
     * gera nova peça se houve alteração e atualiza as pilhas de histórico.
     *
     * @param dir direção do movimento
     * @return true se o tabuleiro foi alterado
     */
    public boolean move(Direction dir) {
        // Salva o estado atual antes de qualquer modificação
        GameState previous = new GameState(grid, score, won);
        boolean moved;
        switch (dir) {
            case LEFT:
                moved = moveLeftInternal();
                break;
            case RIGHT:
                moved = moveRightInternal();
                break;
            case UP:
                moved = moveUpInternal();
                break;
            case DOWN:
                moved = moveDownInternal();
                break;
            default:
                return false;
        }

        if (moved) {
            spawnRandomTile();
            undoStack.push(previous); // registra estado anterior para undo
            redoStack.clear(); // invalida redo após novo movimento
        }
        return moved;
    }

    // ---------- Undo/Redo ----------
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (!canUndo())
            return;
        // Estado atual vai para a pilha de redo
        redoStack.push(new GameState(grid, score, won));
        // Restaura o último estado salvo
        GameState state = undoStack.pop();
        restoreState(state);
    }

    public void redo() {
        if (!canRedo())
            return;
        // Estado atual vai para a pilha de undo
        undoStack.push(new GameState(grid, score, won));
        GameState state = redoStack.pop();
        restoreState(state);
    }

    private void restoreState(GameState state) {
        // Copia grid
        for (int i = 0; i < SIZE; i++)
            System.arraycopy(state.grid[i], 0, grid[i], 0, SIZE);
        this.score = state.score;
        this.won = state.won;
    }

    // ---------- Sub-rotinas internas de movimento (sem spawn) ----------
    private boolean moveLeftInternal() {
        boolean changed = false;
        for (int r = 0; r < SIZE; r++) {
            int[] line = getRow(r);
            int[] original = line.clone();
            processLine(line);
            if (!java.util.Arrays.equals(original, line)) {
                changed = true;
                setRow(r, line);
            }
        }
        return changed;
    }

    private boolean moveRightInternal() {
        boolean changed = false;
        for (int r = 0; r < SIZE; r++) {
            int[] line = getRow(r);
            int[] original = line.clone();
            reverse(line);
            processLine(line);
            reverse(line);
            if (!java.util.Arrays.equals(original, line)) {
                changed = true;
                setRow(r, line);
            }
        }
        return changed;
    }

    private boolean moveUpInternal() {
        boolean changed = false;
        for (int c = 0; c < SIZE; c++) {
            int[] col = getCol(c);
            int[] original = col.clone();
            processLine(col);
            if (!java.util.Arrays.equals(original, col)) {
                changed = true;
                setCol(c, col);
            }
        }
        return changed;
    }

    private boolean moveDownInternal() {
        boolean changed = false;
        for (int c = 0; c < SIZE; c++) {
            int[] col = getCol(c);
            int[] original = col.clone();
            reverse(col);
            processLine(col);
            reverse(col);
            if (!java.util.Arrays.equals(original, col)) {
                changed = true;
                setCol(c, col);
            }
        }
        return changed;
    }

    // ---------- Verificações de estado do jogo ----------
    public boolean isGameOver() {
        if (!isBoardFull())
            return false;
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                int val = grid[i][j];
                if (j < SIZE - 1 && grid[i][j + 1] == val)
                    return false;
                if (i < SIZE - 1 && grid[i + 1][j] == val)
                    return false;
            }
        return true;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (grid[i][j] == 0)
                    return false;
        return true;
    }

    public int checkWinTile() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (grid[i][j] >= 2048)
                    return grid[i][j];
        return 0;
    }

    // ---------- Métodos auxiliares privados ----------
    private int[] getRow(int r) {
        int[] row = new int[SIZE];
        for (int c = 0; c < SIZE; c++)
            row[c] = grid[r][c];
        return row;
    }

    private void setRow(int r, int[] row) {
        for (int c = 0; c < SIZE; c++)
            grid[r][c] = row[c];
    }

    private int[] getCol(int c) {
        int[] col = new int[SIZE];
        for (int r = 0; r < SIZE; r++)
            col[r] = grid[r][c];
        return col;
    }

    private void setCol(int c, int[] col) {
        for (int r = 0; r < SIZE; r++)
            grid[r][c] = col[r];
    }

    private void processLine(int[] line) {
        compact(line);
        for (int i = 0; i < SIZE - 1; i++) {
            if (line[i] != 0 && line[i] == line[i + 1]) {
                int merged = line[i] * 2;
                line[i] = merged;
                line[i + 1] = 0;
                score += merged;
                i++;
            }
        }
        compact(line);
    }

    private void compact(int[] line) {
        int pos = 0;
        for (int i = 0; i < SIZE; i++)
            if (line[i] != 0)
                line[pos++] = line[i];
        while (pos < SIZE)
            line[pos++] = 0;
    }

    private void reverse(int[] arr) {
        for (int i = 0; i < arr.length / 2; i++) {
            int temp = arr[i];
            arr[i] = arr[arr.length - 1 - i];
            arr[arr.length - 1 - i] = temp;
        }
    }

    /**
     * Retorna uma cópia profunda (deep copy) da grade atual do tabuleiro.
     * Útil para preservar o estado antes de um movimento e permitir
     * o cálculo de animações de deslocamento.
     *
     * @return matriz 4x4 idêntica ao grid atual
     */
    public int[][] getGridSnapshot() {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            copy[i] = grid[i].clone();
        return copy;
    }

    // ---------- Enumeração de direções ----------
    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    // ---------- Classe interna para armazenar estado ----------
    private static class GameState {
        final int[][] grid;
        final int score;
        final boolean won;

        GameState(int[][] original, int score, boolean won) {
            this.grid = new int[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++)
                this.grid[i] = original[i].clone();
            this.score = score;
            this.won = won;
        }
    }
}
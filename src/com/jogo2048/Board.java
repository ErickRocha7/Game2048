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

    // Reinicia o tabuleiro: limpa, zera pontuação, gera duas peças e limpa
    // histórico
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

    // Retorna o valor de uma célula
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

    // Gera uma nova peça (2 com 90% de chance, 4 com 10%) em uma casa vazia
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

    // ---------- Métodos públicos de movimento (preservados) ----------
    // Eles continuam existindo, mas o ideal é usar move(Direction)
    public boolean moveLeft() {
        return executeMove(Direction.LEFT);
    }

    public boolean moveRight() {
        return executeMove(Direction.RIGHT);
    }

    public boolean moveUp() {
        return executeMove(Direction.UP);
    }

    public boolean moveDown() {
        return executeMove(Direction.DOWN);
    }

    // ---------- Novo método centralizado de movimento ----------
    private boolean executeMove(Direction dir) {
        boolean changed = false;
        // Salva estado atual antes da jogada
        GameState currentState = new GameState(grid, score, won);

        // Executa o movimento sem spawn (a lógica de spawn fica no GamePanel)
        // Mas como os métodos move* já fazem tudo, vamos chamá-los diretamente e depois
        // controlar o spawn no GamePanel
        // No código atual o GamePanel já chama spawnRandomTile depois do movimento.
        // Vamos manter essa lógica. Apenas encapsulamos a captura de histórico.
        // Para evitar duplicação, vamos mover a lógica de histórico para o GamePanel?
        // Melhor: manter a captura dentro dos move* originais.
        // Vou refatorar: os métodos move* originais serão os que fazem a lógica pura, e
        // executeMove será chamado por eles.
        // Mas para undo funcionar, precisamos salvar o estado **antes** da alteração, e
        // o movimento deve ser atômico com o spawn.
        // Solução: o GamePanel vai chamar board.move(dir) que fará: salvar estado,
        // executar movimento, spawn. Se sucesso, empilha.
        // Assim, o board.move(dir) já inclui o spawn. Precisamos redefinir a interface.

        // Vou refatorar: criar um método público move(Direction) que encapsula tudo, e
        // os moveLeft/moveRight originais serão usados internamente.
        // Para manter compatibilidade, vou manter os moveLeft etc. como estão, mas
        // adicionar o novo método move().
        return changed;
    }

    // Novo método de movimento que engloba salvar estado, executar e spawn
    public boolean move(Direction dir) {
        // Salva estado antes de qualquer alteração
        GameState previous = new GameState(grid, score, won);
        boolean moved = false;
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
        }
        if (moved) {
            // Após movimento, spawn e empilha o estado anterior
            spawnRandomTile();
            undoStack.push(previous);
            redoStack.clear(); // invalida redo após novo movimento
        }
        return moved;
    }

    // Métodos internos que executam apenas a compactação (sem spawn)
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
        // Estado atual vai para redo
        redoStack.push(new GameState(grid, score, won));
        // Restaura o último estado salvo
        GameState state = undoStack.pop();
        restoreState(state);
    }

    public void redo() {
        if (!canRedo())
            return;
        // Estado atual vai para undo
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
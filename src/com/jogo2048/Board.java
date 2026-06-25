package com.jogo2048;

import java.util.Random;

public class Board {
    public static final int SIZE = 4;
    private int[][] grid;
    private Random random;
    private int score;
    private boolean won;

    public Board() {
        grid = new int[SIZE][SIZE];
        random = new Random();
        reset();
    }

    // Reinicia o tabuleiro: limpa, zera pontuação e gera duas peças iniciais
    public void reset() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                grid[i][j] = 0;
        score = 0;
        won = false;
        spawnRandomTile();
        spawnRandomTile();
    }

    // Retorna o valor de uma célula
    public int getCell(int row, int col) {
        return grid[row][col];
    }

    // Pontuação atual
    public int getScore() {
        return score;
    }

    // Verifica se o jogador já venceu
    public boolean hasWon() {
        return won;
    }

    // Define estado de vitória
    public void setWon(boolean won) {
        this.won = won;
    }

    // Gera uma nova peça (2 ou 4) em uma posição vazia aleatória
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

    // ---------- Métodos públicos de movimento ----------

    public boolean moveLeft() {
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

    public boolean moveRight() {
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

    public boolean moveUp() {
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

    public boolean moveDown() {
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

    // Processa uma linha/coluna: compacta, mescla peças iguais e compacta novamente
    private void processLine(int[] line) {
        compact(line);
        for (int i = 0; i < SIZE - 1; i++) {
            if (line[i] != 0 && line[i] == line[i + 1]) {
                int merged = line[i] * 2;
                line[i] = merged;
                line[i + 1] = 0;
                score += merged;
                i++; // pula a próxima posição já usada na mescla
            }
        }
        compact(line);
    }

    // Remove zeros e empurra todos os números para a esquerda
    private void compact(int[] line) {
        int pos = 0;
        for (int i = 0; i < SIZE; i++)
            if (line[i] != 0)
                line[pos++] = line[i];
        while (pos < SIZE)
            line[pos++] = 0;
    }

    // Inverte um array (útil para movimentos direita/baixo)
    private void reverse(int[] arr) {
        for (int i = 0; i < arr.length / 2; i++) {
            int temp = arr[i];
            arr[i] = arr[arr.length - 1 - i];
            arr[arr.length - 1 - i] = temp;
        }
    }
}
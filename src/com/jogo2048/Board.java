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

    // Verifica se o jogador já venceu (usado para não perguntar várias vezes)
    public boolean hasWon() {
        return won;
    }

    // Define estado de vitória
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

    // ----- Métodos privados para mover e mesclar as linhas/colunas -----

    // Obtém uma linha do grid
    private int[] getRow(int r) {
        int[] row = new int[SIZE];
        for (int c = 0; c < SIZE; c++)
            row[c] = grid[r][c];
        return row;
    }

    // Define uma linha no grid
    private void setRow(int r, int[] row) {
        for (int c = 0; c < SIZE; c++)
            grid[r][c] = row[c];
    }

    // Obtém uma coluna
    private int[] getCol(int c) {
        int[] col = new int[SIZE];
        for (int r = 0; r < SIZE; r++)
            col[r] = grid[r][c];
        return col;
    }

    // Define uma coluna
    private void setCol(int c, int[] col) {
        for (int r = 0; r < SIZE; r++)
            grid[r][c] = col[r];
    }

    // Processa uma linha (ou coluna) em uma direção: compacta, mescla e compacta de
    // novo
    private void processLine(int[] line) {
        compact(line);
        for (int i = 0; i < SIZE - 1; i++) {
            if (line[i] != 0 && line[i] == line[i + 1]) {
                int merged = line[i] * 2;
                line[i] = merged;
                line[i + 1] = 0;
                score += merged; // acumula a pontuação
                i++; // pula a próxima peça, já mesclada
            }
        }
        compact(line);
    }

    // Remove os zeros e empurra os números para o início da linha
    private void compact(int[] line) {
        int pos = 0;
        for (int i = 0; i < SIZE; i++)
            if (line[i] != 0)
                line[pos++] = line[i];
        while (pos < SIZE)
            line[pos++] = 0;
    }

    // Inverte a ordem dos elementos de um array (útil para movimentos
    // direita/baixo)
    private void reverse(int[] arr) {
        for (int i = 0; i < arr.length / 2; i++) {
            int temp = arr[i];
            arr[i] = arr[arr.length - 1 - i];
            arr[arr.length - 1 - i] = temp;
        }
    }
}
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

    public void reset() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                grid[i][j] = 0;
        score = 0;
        won = false;
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
}

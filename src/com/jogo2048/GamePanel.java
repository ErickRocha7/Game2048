package com.jogo2048;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GamePanel extends JPanel {
    private Board board;
    private Consumer<Integer> scoreListener;
    private static final int COLS = 4, ROWS = 4;
    private static final int PADDING = 10;
    private static final int CELL_SIZE = 100;
    private static final int WIDTH = COLS * CELL_SIZE + (COLS + 1) * PADDING;
    private static final int HEIGHT = ROWS * CELL_SIZE + (ROWS + 1) * PADDING;

    private final Color emptyColor = new Color(0xCDC1B4);
    private final Map<Integer, Color> tileColors = new HashMap<>();

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(0xBBADA0));
        setFocusable(true);
        board = new Board();

        tileColors.put(2,   new Color(0xEEE4DA));
        tileColors.put(4,   new Color(0xEDE0C8));
        tileColors.put(8,   new Color(0xF2B179));
        tileColors.put(16,  new Color(0xF59563));
        tileColors.put(32,  new Color(0xF67C5F));
        tileColors.put(64,  new Color(0xF65E3B));
        tileColors.put(128, new Color(0xEDCF72));
        tileColors.put(256, new Color(0xEDCC61));
        tileColors.put(512, new Color(0xEDC850));
        tileColors.put(1024,new Color(0xEDC53F));
        tileColors.put(2048,new Color(0xEDC22E));
        tileColors.put(4096,new Color(0xFE3A3A));
        tileColors.put(8192,new Color(0xFF2020));
    }

    public void setScoreListener(Consumer<Integer> listener) {
        this.scoreListener = listener;
    }

    public Board getBoard() { return board; }
}

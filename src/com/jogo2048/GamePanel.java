package com.jogo2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

        tileColors.put(2, new Color(0xEEE4DA));
        tileColors.put(4, new Color(0xEDE0C8));
        tileColors.put(8, new Color(0xF2B179));
        tileColors.put(16, new Color(0xF59563));
        tileColors.put(32, new Color(0xF67C5F));
        tileColors.put(64, new Color(0xF65E3B));
        tileColors.put(128, new Color(0xEDCF72));
        tileColors.put(256, new Color(0xEDCC61));
        tileColors.put(512, new Color(0xEDC850));
        tileColors.put(1024, new Color(0xEDC53F));
        tileColors.put(2048, new Color(0xEDC22E));
        tileColors.put(4096, new Color(0xFE3A3A));
        tileColors.put(8192, new Color(0xFF2020));

        // Listener de teclado para movimentos
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean moved = false;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        moved = board.moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        moved = board.moveRight();
                        break;
                    case KeyEvent.VK_UP:
                        moved = board.moveUp();
                        break;
                    case KeyEvent.VK_DOWN:
                        moved = board.moveDown();
                        break;
                    default:
                        return;
                }
                if (moved) {
                    board.spawnRandomTile();
                    int winTile = board.checkWinTile();
                    if (winTile > 0 && !board.hasWon()) {
                        board.setWon(true);
                        showWinDialog(winTile);
                    }
                    if (scoreListener != null)
                        scoreListener.accept(board.getScore());
                    repaint();
                    if (board.isGameOver())
                        showGameOverDialog();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // fundo do tabuleiro
        g2.setColor(new Color(0xBBADA0));
        g2.fillRoundRect(PADDING, PADDING,
                COLS * CELL_SIZE + (COLS - 1) * PADDING,
                ROWS * CELL_SIZE + (ROWS - 1) * PADDING, 10, 10);

        // desenha cada peça
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int value = board.getCell(r, c);
                int x = PADDING + c * (CELL_SIZE + PADDING);
                int y = PADDING + r * (CELL_SIZE + PADDING);
                Color bg = tileColors.getOrDefault(value, value > 0 ? new Color(0xCDA97A) : emptyColor);
                g2.setColor(bg);
                g2.fillRoundRect(x, y, CELL_SIZE, CELL_SIZE, 10, 10);

                if (value != 0) {
                    g2.setFont(new Font("Arial", Font.BOLD,
                            value < 100 ? 36 : value < 1000 ? 32 : 28));
                    String text = String.valueOf(value);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (CELL_SIZE - fm.stringWidth(text)) / 2;
                    int ty = y + (CELL_SIZE - fm.getAscent()) / 2 + fm.getAscent() - 2;
                    g2.setColor(value <= 4 ? new Color(0x776E65) : Color.WHITE);
                    g2.drawString(text, tx, ty);
                }
            }
        }
    }

    public void setScoreListener(Consumer<Integer> listener) {
        this.scoreListener = listener;
    }

    public Board getBoard() {
        return board;
    }

    // Diálogo de vitória – pergunta se quer continuar
    private void showWinDialog(int value) {
        int opt = JOptionPane.showConfirmDialog(this,
                "Você atingiu " + value + "! Deseja continuar jogando?",
                "Vitória!", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.NO_OPTION)
            System.exit(0);
    }

    // Diálogo de game over – pergunta se quer jogar novamente
    private void showGameOverDialog() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Fim de jogo! Pontuação: " + board.getScore() + "\nJogar novamente?",
                "Game Over", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            board.reset();
            repaint();
            if (scoreListener != null)
                scoreListener.accept(board.getScore());
        }
    }
}
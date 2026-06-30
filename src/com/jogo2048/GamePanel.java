package com.jogo2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class GamePanel extends JPanel {
    private Board board;
    private Consumer<Integer> scoreListener;
    private static final int COLS = 4, ROWS = 4;
    private static final int PADDING = 10;
    private static final int CELL_SIZE = 100;
    private static final int WIDTH = COLS * CELL_SIZE + (COLS + 1) * PADDING;
    private static final int HEIGHT = ROWS * CELL_SIZE + (ROWS + 1) * PADDING;
    private static final int ANIM_DURATION_MS = 150; // ms

    private final Color emptyColor = new Color(0xCDC1B4);
    private final Map<Integer, Color> tileColors = new HashMap<>();

    // Infraestrutura de animação
    private final List<AnimatedTile> animatedTiles = new ArrayList<>();
    private Timer animTimer;
    private boolean animating = false;

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
        tileColors.put(16384, new Color(0xFF6B6B));
        tileColors.put(32768, new Color(0xFF8C42));
        tileColors.put(65536, new Color(0xFFD700));
        tileColors.put(131072, new Color(0xADFF2F));

        // Timer de animação (ainda sem lógica real)
        animTimer = new Timer(10, e -> {
            // Nos próximos commits, atualizaremos os tiles aqui
            repaint();
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (animating)
                    return;

                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_Z && board.canUndo()) {
                        board.undo();
                        repaint();
                        updateScore();
                    } else if (e.getKeyCode() == KeyEvent.VK_Y && board.canRedo()) {
                        board.redo();
                        repaint();
                        updateScore();
                    }
                    return;
                }

                Board.Direction dir = null;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        dir = Board.Direction.LEFT;
                        break;
                    case KeyEvent.VK_RIGHT:
                        dir = Board.Direction.RIGHT;
                        break;
                    case KeyEvent.VK_UP:
                        dir = Board.Direction.UP;
                        break;
                    case KeyEvent.VK_DOWN:
                        dir = Board.Direction.DOWN;
                        break;
                    default:
                        return;
                }

                boolean moved = board.move(dir);
                if (moved) {
                    updateScore();
                    repaint();
                    int winTile = board.checkWinTile();
                    if (winTile > 0 && !board.hasWon()) {
                        board.setWon(true);
                        showWinDialog(winTile);
                    }
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
        g2.setColor(new Color(0xBBADA0));
        g2.fillRoundRect(PADDING, PADDING,
                COLS * CELL_SIZE + (COLS - 1) * PADDING,
                ROWS * CELL_SIZE + (ROWS - 1) * PADDING, 10, 10);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int value = board.getCell(r, c);
                int x = PADDING + c * (CELL_SIZE + PADDING);
                int y = PADDING + r * (CELL_SIZE + PADDING);
                Color bg = getTileColor(value);
                g2.setColor(bg);
                g2.fillRoundRect(x, y, CELL_SIZE, CELL_SIZE, 10, 10);
                if (value != 0) {
                    g2.setFont(new Font("Arial", Font.BOLD, value < 100 ? 36 : value < 1000 ? 32 : 28));
                    String text = String.valueOf(value);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (CELL_SIZE - fm.stringWidth(text)) / 2;
                    int ty = y + (CELL_SIZE - fm.getAscent()) / 2 + fm.getAscent() - 2;
                    g2.setColor(chooseTextColor(bg));
                    g2.drawString(text, tx, ty);
                }
            }
        }
    }

    private Color getTileColor(int value) {
        if (value == 0)
            return emptyColor;
        Color c = tileColors.get(value);
        if (c != null)
            return c;
        int r = Math.min(255, 180 + (value * 37) % 76);
        int g = Math.min(255, 160 + (value * 73) % 96);
        int b = Math.min(255, 100 + (value * 127) % 156);
        return new Color(r, g, b);
    }

    private Color chooseTextColor(Color bg) {
        double lum = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255;
        return lum > 0.5 ? new Color(0x776E65) : Color.WHITE;
    }

    private void updateScore() {
        if (scoreListener != null)
            scoreListener.accept(board.getScore());
    }

    public void setScoreListener(Consumer<Integer> listener) {
        this.scoreListener = listener;
    }

    public Board getBoard() {
        return board;
    }

    private void showWinDialog(int value) {
        int opt = JOptionPane.showConfirmDialog(this,
                "Você atingiu " + value + "! Deseja continuar jogando?",
                "Vitória!", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.NO_OPTION)
            System.exit(0);
        requestFocusInWindow();
    }

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
        requestFocusInWindow();
    }

    // ---------- Classes internas de animação (Commit 2) ----------
    private static class TileMovement {
        int fromRow, fromCol, toRow, toCol, value;
        boolean isMerge;
        boolean isDisappearing;

        TileMovement(int fr, int fc, int tr, int tc, int v, boolean merge, boolean disappear) {
            fromRow = fr;
            fromCol = fc;
            toRow = tr;
            toCol = tc;
            value = v;
            isMerge = merge;
            isDisappearing = disappear;
        }
    }

    private static class TileData {
        int value;
        int originalIndex;

        TileData(int v, int i) {
            value = v;
            originalIndex = i;
        }
    }

    private class AnimatedTile {
        int value;
        float curX, curY;
        float startX, startY;
        float targetX, targetY;
        long startTime;
        boolean finished;
        boolean disappearing;
        boolean appearing;
        float scale = 1.0f;

        AnimatedTile(int value, int sx, int sy, int tx, int ty, boolean merge, boolean disappear, long now) {
            this(value, sx, sy, tx, ty, merge, disappear, now, false);
        }

        AnimatedTile(int value, int sx, int sy, int tx, int ty, boolean merge, boolean disappear, long now,
                boolean appear) {
            this.value = value;
            this.startX = sx;
            this.startY = sy;
            this.targetX = tx;
            this.targetY = ty;
            this.startTime = now;
            this.disappearing = disappear;
            this.appearing = appear;
            curX = sx;
            curY = sy;
            if (appear)
                scale = 0.0f;
            finished = false;
        }

        void update(long now) {
            if (finished)
                return;
            float elapsed = (now - startTime) / (float) ANIM_DURATION_MS;
            if (elapsed >= 1.0f) {
                elapsed = 1.0f;
                finished = true;
            }
            curX = startX + (targetX - startX) * elapsed;
            curY = startY + (targetY - startY) * elapsed;
            if (disappearing) {
                scale = 1.0f - elapsed;
            } else if (appearing) {
                if (elapsed < 0.7f)
                    scale = 1.2f * (elapsed / 0.7f);
                else
                    scale = 1.2f - 0.2f * ((elapsed - 0.7f) / 0.3f);
                if (scale > 1.2f)
                    scale = 1.2f;
                if (scale < 1.0f && finished)
                    scale = 1.0f;
            } else if (finished) {
                scale = 1.0f;
            }
        }
    }
}
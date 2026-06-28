package com.jogo2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Game2048 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("2048");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // Painel do jogo (tabuleiro)
            GamePanel panel = new GamePanel();

            // Rótulo de pontuação
            JLabel scoreLabel = new JLabel("Pontuação: 0", SwingConstants.CENTER);
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
            scoreLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            // Atualiza o placar sempre que a pontuação mudar
            panel.setScoreListener(score -> scoreLabel.setText("Pontuação: " + score));

            // Layout da janela
            frame.setLayout(new BorderLayout());
            frame.add(scoreLabel, BorderLayout.NORTH);
            frame.add(panel, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);

            // Garante que o painel capture as teclas assim que a janela abrir
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    panel.requestFocusInWindow();
                }
            });

            frame.setVisible(true);
        });
    }
}
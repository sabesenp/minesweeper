package Minesweeper.pathmanathansabesenfinalproject;

import javax.swing.*;
import java.awt.*;

public class minesweeperGameOver extends JFrame {
    public minesweeperGameOver(String time) {
        super("Game Over");
        setSize(320, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JLabel msg = new JLabel("💥 You hit a mine.", SwingConstants.CENTER);
        JLabel t = new JLabel("Time: " + time, SwingConstants.CENTER);
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> dispose());

        add(msg, BorderLayout.NORTH);
        add(t, BorderLayout.CENTER);
        add(ok, BorderLayout.SOUTH);
    }
}

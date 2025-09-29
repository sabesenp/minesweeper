import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.Random;

public class Minesweeper extends JFrame {
    // Board config
    private static final int ROWS = 8;
    private static final int COLS = 8;
    private static final int MINES = 6;

    // UI
    private final JButton[][] buttons = new JButton[ROWS][COLS];
    private final JLabel minesLeftLabel = new JLabel();
    private final JLabel timeLabel = new JLabel("00:00");
    private final JButton restartBtn = new JButton("Restart");

    // Game state
    private final int[][] field = new int[ROWS][COLS]; // 99 = mine, else neighbor count
    private final boolean[][] revealed = new boolean[ROWS][COLS];
    private final boolean[][] flagged = new boolean[ROWS][COLS];
    private int minesLeft = MINES;
    private boolean gameOver = false;

    // Timer
    private int elapsedSeconds = 0;
    private final Timer swingTimer;

    public Minesweeper() {
        super("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        top.add(new JLabel("Mines Left:"));
        minesLeftLabel.setFont(minesLeftLabel.getFont().deriveFont(Font.BOLD, 16f));
        top.add(minesLeftLabel);
        top.add(new JLabel("Time:"));
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.BOLD, 16f));
        top.add(timeLabel);
        top.add(restartBtn);
        add(top, BorderLayout.NORTH);

        // Grid
        JPanel grid = new JPanel(new GridLayout(ROWS, COLS, 2, 2));
        grid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                JButton b = new JButton();
                b.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
                b.setFocusable(false);
                b.setMargin(new Insets(0, 0, 0, 0));
                buttons[r][c] = b;
                final int R = r, C = c;

                // Left click (reveal)
                b.addActionListener(e -> handleReveal(R, C));
                // Right click (flag)
                b.addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) handleFlag(R, C);
                    }
                });

                grid.add(b);
            }
        }
        add(grid, BorderLayout.CENTER);

        // Timer (tick every second)
        swingTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            timeLabel.setText(formatTime(elapsedSeconds));
        });

        // Restart
        restartBtn.addActionListener(e -> resetGame());

        // Start
        resetGame();
        setVisible(true);
    }

    /* ===== Game Setup / Reset ===== */

    private void resetGame() {
        // Stop timer & reset counters
        swingTimer.stop();
        elapsedSeconds = 0;
        timeLabel.setText("00:00");
        gameOver = false;
        minesLeft = MINES;
        minesLeftLabel.setText(String.valueOf(minesLeft));

        // Reset arrays
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                field[r][c] = 0;
                revealed[r][c] = false;
                flagged[r][c] = false;
                JButton b = buttons[r][c];
                b.setEnabled(true);
                b.setText("");
                b.setBackground(null);
            }
        }

        // Place mines
        placeMines();

        // Compute neighbor counts
        computeNeighborCounts();

        // Start timer
        swingTimer.start();
    }

    private void placeMines() {
        Random rnd = new Random();
        int placed = 0;
        while (placed < MINES) {
            int rr = rnd.nextInt(ROWS);
            int cc = rnd.nextInt(COLS);
            if (field[rr][cc] != 99) {
                field[rr][cc] = 99;
                placed++;
            }
        }
    }

    private void computeNeighborCounts() {
        int[] dr = {-1,-1,-1, 0,0, 1,1,1};
        int[] dc = {-1, 0, 1,-1,1,-1,0,1};
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (field[r][c] == 99) continue;
                int cnt = 0;
                for (int k = 0; k < 8; k++) {
                    int nr = r + dr[k], nc = c + dc[k];
                    if (inBounds(nr, nc) && field[nr][nc] == 99) cnt++;
                }
                field[r][c] = cnt;
            }
        }
    }

    /* ===== User Actions ===== */

    private void handleFlag(int r, int c) {
        if (gameOver || revealed[r][c]) return;
        JButton b = buttons[r][c];

        if (!flagged[r][c]) {
            flagged[r][c] = true;
            b.setText("🚩");
            minesLeft = Math.max(0, minesLeft - 1);
        } else {
            flagged[r][c] = false;
            b.setText("");
            minesLeft++;
        }
        minesLeftLabel.setText(String.valueOf(minesLeft));
    }

    private void handleReveal(int r, int c) {
        if (gameOver || revealed[r][c] || flagged[r][c]) return;

        // Hit a mine
        if (field[r][c] == 99) {
            revealAllMines();
            buttons[r][c].setBackground(Color.PINK);
            endGame(false);
            return;
        }

        // Reveal safe tile (flood fill zeros)
        floodReveal(r, c);

        // Check win
        if (checkWin()) endGame(true);
    }

    /* ===== Reveal Helpers ===== */

    private void floodReveal(int r, int c) {
        ArrayDeque<Point> q = new ArrayDeque<>();
        q.add(new Point(r, c));

        while (!q.isEmpty()) {
            Point p = q.removeFirst();
            int rr = p.x, cc = p.y;
            if (!inBounds(rr, cc) || revealed[rr][cc] || field[rr][cc] == 99) continue;

            revealed[rr][cc] = true;
            JButton b = buttons[rr][cc];
            b.setEnabled(false);

            if (field[rr][cc] == 0) {
                b.setText(""); // show empty
                // expand neighbors
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = rr + dr, nc = cc + dc;
                        if (inBounds(nr, nc) && !revealed[nr][nc]) q.add(new Point(nr, nc));
                    }
                }
            } else {
                b.setText(Integer.toString(field[rr][cc]));
            }
        }
    }

    private void revealAllMines() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (field[r][c] == 99) {
                    JButton b = buttons[r][c];
                    b.setEnabled(false);
                    b.setText("X");
                    b.setBackground(new Color(255, 220, 220));
                }
            }
        }
    }

    private boolean checkWin() {
        int safe = ROWS * COLS - MINES;
        int revealedCount = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (revealed[r][c] && field[r][c] != 99) revealedCount++;
            }
        }
        return revealedCount == safe;
    }

    private void endGame(boolean won) {
        gameOver = true;
        swingTimer.stop();

        String msg = won ? "🎉 You cleared all safe tiles!\nTime: " + formatTime(elapsedSeconds)
                         : "💥 You hit a mine.\nTime: " + formatTime(elapsedSeconds);
        int option = JOptionPane.showOptionDialog(
                this,
                msg + "\n\nPlay again?",
                won ? "You Win!" : "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"Restart", "Close"},
                "Restart"
        );

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            // disable all buttons
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) buttons[r][c].setEnabled(false);
            }
        }
    }

    /* ===== Utils ===== */

    private static boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    private static String formatTime(int sec) {
        int m = sec / 60, s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Minesweeper::new);
    }
}

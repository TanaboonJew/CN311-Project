import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConnectFour extends JFrame {
    private static final int ROWS = ConnectFourGame.ROWS;
    private static final int COLS = ConnectFourGame.COLS;
    private JButton[][] boardButtons;
    private JPanel boardPanel;
    private ConnectFourGame game;

    public ConnectFour() {
        game = new ConnectFourGame();

        setTitle("Connect Four");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLayout(new BorderLayout());

        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(ROWS, COLS));
        boardButtons = new JButton[ROWS][COLS];

        // Initialize the board with buttons
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                boardButtons[row][col] = new JButton();
                boardButtons[row][col].setBackground(Color.WHITE);
                boardButtons[row][col].setEnabled(true);
                final int finalCol = col;
                boardButtons[row][col].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (game.dropToken(finalCol)) {
                            updateBoard();
                        }
                    }
                });
                boardPanel.add(boardButtons[row][col]);
            }
        }

        add(boardPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void updateBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int value = game.getBoard()[row][col];
                if (value == 1) {
                    boardButtons[row][col].setBackground(Color.RED);
                } else if (value == 2) {
                    boardButtons[row][col].setBackground(Color.YELLOW);
                } else {
                    boardButtons[row][col].setBackground(Color.WHITE);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ConnectFour();
            }
        });
    }
}

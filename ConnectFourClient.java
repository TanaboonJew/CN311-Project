import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ConnectFourClient extends JFrame {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private JButton[][] boardButtons;
    private JPanel boardPanel;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ConnectFourClient() {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
                            dropToken(finalCol);
                        }
                    });
                    boardPanel.add(boardButtons[row][col]);
                }
            }

            add(boardPanel, BorderLayout.CENTER);
            setVisible(true);

            updateBoard();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dropToken(int col) {
        out.println("DROP " + col);
        try {
            String response = in.readLine();
            if (response.equals("SUCCESS")) {
                updateBoard();
            } else if (response.equals("FAILURE")) {
                JOptionPane.showMessageDialog(this, "Column is full!");
            } else if (response.equals("RED")) {
                JOptionPane.showMessageDialog(this, "Red Win!");
                updateBoard();
            } else if (response.equals("YELLOW")) {
                JOptionPane.showMessageDialog(this, "Yellow Win!");
                updateBoard();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateBoard() {
        out.println("GET_BOARD");
        try {
            for (int row = 0; row < ROWS; row++) {
                String[] line = in.readLine().split(" ");
                for (int col = 0; col < COLS; col++) {
                    int value = Integer.parseInt(line[col]);
                    if (value == 1) {
                        boardButtons[row][col].setBackground(Color.RED);
                    } else if (value == 2) {
                        boardButtons[row][col].setBackground(Color.YELLOW);
                    } else {
                        boardButtons[row][col].setBackground(Color.WHITE);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ConnectFourClient();
            }
        });
    }
}

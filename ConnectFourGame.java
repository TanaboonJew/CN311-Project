import javax.swing.JOptionPane;

public class ConnectFourGame {
    public static final int ROWS = 6;
    public static final int COLS = 7;
    private int[][] board;
    private int currentPlayer;
    private final Object lock = new Object();
    private int winState;

    public ConnectFourGame() {
        board = new int[ROWS][COLS];
        currentPlayer = 1; // Player 1: Red, Player 2: Yellow
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int[][] getBoard() {
        return board;
    }

    public int dropToken(int col) {
        synchronized (lock) {
            for (int row = ROWS - 1; row >= 0; row--) {
                if (board[row][col] == 0) {
                    board[row][col] = currentPlayer;
                    final int finalRow = row; // Make row effectively final
                    final int finalCol = col; // Make col effectively final
                    new Thread(() -> {
                        winState = checkForWin(finalRow, finalCol);
	                }).start();
                    if (winState == 1) {
                        return 2;
                    }
                    else if (winState == 2) {
                        return 3;
                    }
                    currentPlayer = (currentPlayer == 1) ? 2 : 1;
                    return 1;
                }
            }
            return 0;
        }
    }

    private int checkForWin(int row, int col) {
        synchronized (lock) {
            if (checkVertical(row, col) || checkHorizontal(row, col) ||
                checkDiagonal1(row, col) || checkDiagonal2(row, col)) {
                int winner = board[row][col];
                return winner;
            }
            else {return 0;}
        }
    }

    private boolean checkVertical(int row, int col) {
        int count = 0;
        for (int r = row; r < ROWS; r++) {
            if (board[r][col] == board[row][col]) {
                count++;
                if (count >= 4) return true;
            } else {
                break;
            }
        }
        return false;
    }

    private boolean checkHorizontal(int row, int col) {
        int count = 0;
        for (int c = 0; c < COLS; c++) {
            if (board[row][c] == board[row][col]) {
                count++;
                if (count >= 4) return true;
            } else {
                count = 0;
            }
        }
        return false;
    }

    private boolean checkDiagonal1(int row, int col) {
        int count = 0;
        for (int r = row, c = col; r >= 0 && c < COLS; r--, c++) {
            if (board[r][c] == board[row][col]) {
                count++;
            } else {
                break;
            }
        }
        for (int r = row + 1, c = col - 1; r < ROWS && c >= 0; r++, c--) {
            if (board[r][c] == board[row][col]) {
                count++;
            } else {
                break;
            }
        }
        return count >= 4;
    }

    private boolean checkDiagonal2(int row, int col) {
        int count = 0;
        for (int r = row, c = col; r < ROWS && c < COLS; r++, c++) {
            if (board[r][c] == board[row][col]) {
                count++;
            } else {
                break;
            }
        }
        for (int r = row - 1, c = col - 1; r >= 0 && c >= 0; r--, c--) {
            if (board[r][c] == board[row][col]) {
                count++;
            } else {
                break;
            }
        }
        return count >= 4;
    }

    public void resetGame() {
        synchronized (lock) {
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    board[row][col] = 0;
                }
            }
            currentPlayer = 1;
        }
    }
}

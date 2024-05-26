import java.io.*;
import java.net.*;

public class ConnectFourGameServer {
    public static final int PORT = 12345;
    private ConnectFourGame game;

    public ConnectFourGameServer() {
        game = new ConnectFourGame();
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started. Waiting for clients...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket, game).start();
        }
    }

    public static void main(String[] args) throws IOException {
        new ConnectFourGameServer().start();
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private ConnectFourGame game;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, ConnectFourGame game) {
        this.socket = socket;
        this.game = game;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String request;
            while ((request = in.readLine()) != null) {
                String[] parts = request.split(" ");
                if (parts[0].equals("DROP")) {
                    int col = Integer.parseInt(parts[1]);
                    int success = game.dropToken(col);
                    if (success == 1) {
                        out.println("SUCCESS");
                    }
                    else if (success == 0) {
                        out.println("FAILURE");
                    }
                    else if (success == 2) {
                        out.println("RED");
                        game.resetGame();
                    }
                    else if (success == 3) {
                        out.println("YELLOW");
                        game.resetGame();
                    }
                } else if (parts[0].equals("GET_BOARD")) {
                    int[][] board = game.getBoard();
                    for (int[] row : board) {
                        for (int cell : row) {
                            out.print(cell + " ");
                        }
                        out.println();
                    }
                } else if (parts[0].equals("GET_PLAYER")) {
                    out.println(game.getCurrentPlayer());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

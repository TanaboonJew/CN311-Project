import javax.swing.JTextArea;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferHandler {

    private int port;
    private JTextArea logTextArea;

    public FileTransferHandler(int port, JTextArea logTextArea) {
        this.port = port;
        this.logTextArea = logTextArea;
    }

    public void startServer() {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                logTextArea.append("Server started on port " + port + "\n");

                while (true) {
                    Socket socket = serverSocket.accept();
                    logTextArea.append("New client connected\n");
                    new ClientHandler(socket).start();
                }
            } catch (IOException ex) {
                logTextArea.append("Error: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });
        serverThread.start();
    }

    public void uploadFile(String filePath, String targetClient, int serverPort) {
        Thread uploadThread = new Thread(() -> {
            try (Socket socket = new Socket(targetClient, serverPort);
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 FileInputStream fis = new FileInputStream(filePath)) {

                File file = new File(filePath);
                dos.writeUTF("UPLOAD");
                dos.writeUTF(file.getName());
                dos.writeLong(file.length());

                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) > 0) {
                    dos.write(buffer, 0, read);
                }

                logTextArea.append("File uploaded successfully to " + targetClient + "\n");

            } catch (IOException ex) {
                logTextArea.append("Error: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });
        uploadThread.start();
    }

    public void downloadFile(String fileName, String targetClient, int serverPort) {
        Thread downloadThread = new Thread(() -> {
            try (Socket socket = new Socket(targetClient, serverPort);
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dis = new DataInputStream(socket.getInputStream())) {

                dos.writeUTF("DOWNLOAD");
                dos.writeUTF(fileName);

                String response = dis.readUTF();
                if ("OK".equals(response)) {
                    long fileSize = dis.readLong();
                    File file = new File("client_files/" + fileName);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = dis.read(buffer, 0, Math.min(buffer.length, (int) fileSize))) > 0) {
                            fos.write(buffer, 0, read);
                            fileSize -= read;
                        }
                        logTextArea.append("File downloaded successfully from " + targetClient + "\n");
                    }
                } else {
                    logTextArea.append("File not found on server.\n");
                }

            } catch (IOException ex) {
                logTextArea.append("Error: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });
        downloadThread.start();
    }

    private class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

                String command = dis.readUTF();
                if (command.equals("UPLOAD")) {
                    receiveFile(dis);
                } else if (command.equals("DOWNLOAD")) {
                    sendFile(dis, dos);
                }
            } catch (IOException ex) {
                logTextArea.append("Error: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        }

        private void receiveFile(DataInputStream dis) throws IOException {
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            File dir = new File("server_files");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(new File(dir, fileName))) {
                byte[] buffer = new byte[4096];
                int read;
                while (fileSize > 0 && (read = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                    fos.write(buffer, 0, read);
                    fileSize -= read;
                }
                logTextArea.append("File " + fileName + " received.\n");
            }
        }

        private void sendFile(DataInputStream dis, DataOutputStream dos) throws IOException {
            String fileName = dis.readUTF();
            File file = new File("server_files/" + fileName);
            if (file.exists()) {
                dos.writeUTF("OK");
                dos.writeLong(file.length());
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = fis.read(buffer)) > 0) {
                        dos.write(buffer, 0, read);
                    }
                }
                logTextArea.append("File " + fileName + " sent.\n");
            } else {
                dos.writeUTF("File not found");
            }
        }
    }
}

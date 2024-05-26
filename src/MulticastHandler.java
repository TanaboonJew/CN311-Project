import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class MulticastHandler {

    private String multicastAddress;
    private int multicastPort;
    private JTextArea logTextArea;
    private JComboBox<String> clientList;
    private MulticastSocket socket;
    private Set<String> clients;

    public MulticastHandler(String multicastAddress, int multicastPort, JTextArea logTextArea, JComboBox<String> clientList) {
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.logTextArea = logTextArea;
        this.clientList = clientList;
        this.clients = new HashSet<>();
    }

    public void startMulticastListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                socket = new MulticastSocket(multicastPort);
                InetAddress group = InetAddress.getByName(multicastAddress);
                socket.joinGroup(group);

                byte[] buffer = new byte[256];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());
                    if (message.startsWith("PRESENT:")) {
                        String clientAddress = message.substring(8);
                        if (clients.add(clientAddress)) {
                            SwingUtilities.invokeLater(() -> clientList.addItem(clientAddress));
                            logTextArea.append("New client discovered: " + clientAddress + "\n");
                        }
                    }
                }
            } catch (IOException ex) {
                logTextArea.append("Multicast Listener Error: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });
        listenerThread.start();
    }

    public void announcePresence() {
        Thread announceThread = new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                InetAddress group = InetAddress.getByName(multicastAddress);
                String message = "PRESENT:" + InetAddress.getLocalHost().getHostAddress();
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), group, multicastPort);
                while (true) {
                    socket.send(packet);
                    Thread.sleep(5000);
                }
            } catch (IOException | InterruptedException ex) {
                logTextArea.append("Multicast Announce Error: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });
        announceThread.start();
    }
}

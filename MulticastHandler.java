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
    private Set<String> clients;

    public MulticastHandler(String multicastAddress, int multicastPort, JTextArea logTextArea, JComboBox<String> clientList) {
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.logTextArea = logTextArea;
        this.clientList = clientList;
        this.clients = new HashSet<>();
    }

    public void startMulticastListener() {
        Thread listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (MulticastSocket socket = new MulticastSocket(multicastPort)) {
                    InetAddress group = InetAddress.getByName(multicastAddress);
                    socket.joinGroup(group);

                    logTextArea.append("Listening for multicast messages on " + multicastAddress + ":" + multicastPort + "\n");

                    while (true) {
                        byte[] buffer = new byte[256];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        String message = new String(packet.getData(), 0, packet.getLength());
                        if (message.startsWith("PRESENT:")) {
                            String clientAddress = message.substring(8);
                            if (!clients.contains(clientAddress)) {
                                clients.add(clientAddress);
                                clientList.addItem(clientAddress);
                                logTextArea.append("Client discovered: " + clientAddress + "\n");
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        listenerThread.start();
    }

    public void announcePresence() {
        Thread announcerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (DatagramSocket socket = new DatagramSocket()) {
                    InetAddress group = InetAddress.getByName(multicastAddress);
                    String message = "PRESENT:" + InetAddress.getLocalHost().getHostAddress();
                    byte[] buffer = message.getBytes();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);
                    socket.send(packet);

                    logTextArea.append("Announced presence: " + message + "\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        announcerThread.start();
    }
}

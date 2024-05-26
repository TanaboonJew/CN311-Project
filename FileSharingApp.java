import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class FileSharingApp extends JFrame {

    private JTextArea logTextArea;
    private JTextField fileNameField;
    private JButton uploadButton;
    private JButton downloadButton;
    private JButton browseButton;
    private JComboBox<String> clientList;
    private JFileChooser fileChooser;
    private FileTransferHandler fileTransferHandler;
    private MulticastHandler multicastHandler;

    public FileSharingApp() {
        setTitle("P2P File Sharing App");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        fileNameField = new JTextField(20);
        uploadButton = new JButton("Upload");
        downloadButton = new JButton("Download");
        browseButton = new JButton("Browse");
        clientList = new JComboBox<>();
        inputPanel.add(fileNameField);
        inputPanel.add(browseButton);
        inputPanel.add(uploadButton);
        inputPanel.add(downloadButton);
        inputPanel.add(clientList);
        add(inputPanel, BorderLayout.SOUTH);

        fileChooser = new JFileChooser();

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    fileNameField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = fileNameField.getText();
                String targetClient = (String) clientList.getSelectedItem();
                if (filePath.isEmpty() || targetClient == null) {
                    logTextArea.append("Please select a file and target client.\n");
                } else {
                    uploadFile(filePath, targetClient);
                }
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = fileNameField.getText();
                String targetClient = (String) clientList.getSelectedItem();
                if (fileName.isEmpty() || targetClient == null) {
                    logTextArea.append("Please enter a file name and target client.\n");
                } else {
                    downloadFile(fileName, targetClient);
                }
            }
        });

        fileTransferHandler = new FileTransferHandler(12345, logTextArea);
        multicastHandler = new MulticastHandler("230.0.0.0", 4446, logTextArea, clientList);

        fileTransferHandler.startServer();
        multicastHandler.startMulticastListener();
        multicastHandler.announcePresence();
    }

    private void uploadFile(String filePath, String targetClient) {
        fileTransferHandler.uploadFile(filePath, targetClient, 12345);
    }

    private void downloadFile(String fileName, String targetClient) {
        fileTransferHandler.downloadFile(fileName, targetClient, 12345);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileSharingApp().setVisible(true);
            }
        });
    }
}

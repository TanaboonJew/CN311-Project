import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FileSharingApp extends JFrame {

    private static final int SERVER_PORT = 12345;
    private static final String MULTICAST_ADDRESS = "230.0.0.0";
    private static final int MULTICAST_PORT = 12346;

    private JTextArea logTextArea;
    private JTextField fileNameField;
    private JButton uploadButton;
    private JButton downloadButton;
    private JButton browseButton;
    private JComboBox<String> clientList;

    private MulticastHandler multicastHandler;
    private FileTransferHandler fileTransferHandler;
    private JFileChooser fileChooser;

    public FileSharingApp() {
        setTitle("P2P File Sharing App");
        setSize(500, 400);
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
                int returnVal = fileChooser.showOpenDialog(FileSharingApp.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileNameField.setText(file.getAbsolutePath());
                }
            }
        });

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = fileNameField.getText();
                String selectedClient = (String) clientList.getSelectedItem();
                if (!filePath.isEmpty() && selectedClient != null) {
                    uploadFile(filePath, selectedClient);
                } else {
                    logTextArea.append("Please select a file and a client.\n");
                }
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = fileNameField.getText();
                String selectedClient = (String) clientList.getSelectedItem();
                if (!fileName.isEmpty() && selectedClient != null) {
                    downloadFile(fileName, selectedClient);
                } else {
                    logTextArea.append("Please enter a file name and select a client.\n");
                }
            }
        });

        fileTransferHandler = new FileTransferHandler(SERVER_PORT, logTextArea);
        multicastHandler = new MulticastHandler(MULTICAST_ADDRESS, MULTICAST_PORT, logTextArea, clientList);

        fileTransferHandler.startServer();
        multicastHandler.startMulticastListener();
        multicastHandler.announcePresence();
    }

    private void uploadFile(String filePath, String targetClient) {
        File file = new File(filePath);
        if (!file.exists()) {
            logTextArea.append("Error: File not found: " + filePath + "\n");
            return;
        }
        fileTransferHandler.uploadFile(filePath, targetClient, SERVER_PORT);
    }

    private void downloadFile(String fileName, String targetClient) {
        fileTransferHandler.downloadFile(fileName, targetClient, SERVER_PORT);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileSharingApp().setVisible(true));
    }
}

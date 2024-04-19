package fr.uga.miashs.dciss.chatservice.client;


import fr.uga.miashs.dciss.chatservice.server.UserMsg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.net.Socket;

import static java.lang.System.out;

public class InstantMessengerGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JTextField usernameField;
    private JButton connectButton;
    private ClientMsg client;
    private JButton createGroupButton;
    private JList<String> contactList;
    private JButton addContactButton;
    private JButton sendFileButton;

    JTextField aQuiTextField;

    public InstantMessengerGUI() {
        setTitle("Messagerie Instantanée");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        //connectToServer();
    }
    private DataOutputStream out;



    //Pour déplacer le bouton "Créer un groupe" dans l'onglet de chat, vous pouvez simplement déplacer le code qui ajoute le bouton à l'onglet de connexion vers l'onglet de chat. Voici comment vous pouvez le faire :

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel connectionPanel = new JPanel();
        connectionPanel.setLayout(new BorderLayout());

        JPanel connectInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        connectInputPanel.add(new JLabel("Pseudo:"));
        usernameField = new JTextField(15);
        connectInputPanel.add(usernameField);
        connectButton = new JButton("Se connecter");

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
                JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
            }
        });
        connectInputPanel.add(connectButton);
        connectionPanel.add(connectInputPanel, BorderLayout.CENTER);

        aQuiTextField = new JTextField(2);
        aQuiTextField.setText("1");
        connectionPanel.add(aQuiTextField, BorderLayout.SOUTH);

        tabbedPane.addTab("Connexion", connectionPanel);

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Envoyer");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);
        //Ajoutez le bouton "Envoyer un fichier"
        sendFileButton = new JButton("Envoyer un fichier");
        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendFile();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        bottomPanel.add(sendFileButton, BorderLayout.SOUTH);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Chat", chatPanel);
        add(tabbedPane);


        // Ajoutez le bouton "Créer un groupe" ici
        createGroupButton= new JButton("Créer un groupe");
        createGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    createGroup();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        bottomPanel.add(createGroupButton, BorderLayout.WEST);

        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Chat", chatPanel);

        add(tabbedPane);


        JPanel directoryPanel = new JPanel(new BorderLayout());
        addContactButton = new JButton("Ajouter un contact");
        addContactButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    addContact();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        directoryPanel.add(addContactButton, BorderLayout.NORTH);

        // Ajoutez la liste de contacts au panel du répertoire
        contactList = new JList<>();
        directoryPanel.add(new JScrollPane(contactList), BorderLayout.CENTER);
        tabbedPane.addTab("Répertoire", directoryPanel);
        add(tabbedPane);

    }

    private void connect() {
        String username = usernameField.getText().trim();
        if (!username.isEmpty()) {
            try {
                // Créer une instance de ClientMsg
                this.client = new ClientMsg("localhost", 1666);
                // Appeler la méthode start (ou une autre méthode qui démarre le client)
                client.startSession();
                client.setName(username);

                client.addMessageListener((packet) -> {
                    chatArea.append(packet.srcId + ": " + new String(packet.data) + "\n");
                });
                chatArea.append("Connecté en tant que: " + username + "\n");

                // Vous pouvez également changer d'onglet après la connexion réussie
                JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
                tabbedPane.setSelectedIndex(1); // Onglet de chat
                updateContactList();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur de connexion");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Erreur de connexion");
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendPacket(Integer.parseInt(aQuiTextField.getText()), message.getBytes());
            chatArea.append("Moi: " + message + "\n");
            messageField.setText("");
        }
    }

    private void updateContactList() {
        // Récupérez la liste des contacts
        Map<Integer, String> contacts = client.getContacts();

        // Convertissez cette liste en un tableau de chaînes
        String[] contactArray = contacts.values().toArray(new String[0]);

        // Utilisez ce tableau pour créer un nouveau modèle de liste
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String contact : contactArray) {
            listModel.addElement(contact);
        }

        // Définissez ce modèle comme le modèle de votre JList
        contactList.setModel(listModel);
    }

    private void sendFile() throws IOException{
        String input = JOptionPane.showInputDialog(this, "Quel est le chemin du fichier que vous souhaitez envoyer ?");
        String title = JOptionPane.showInputDialog(this, "Quel est son titre ?");
        if (input != null && title!=null) {
            client.sendFile(Integer.parseInt(aQuiTextField.getText()), Paths.get(input),title);
        }

        /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        // byte 8 : send a file
        dos.writeByte(8);
        String input = JOptionPane.showInputDialog(this, "Quel est le chemin du fichier que vous souhaitez envoyer ?");
        if (input != null) {
            dos.writeUTF(input);
            client.sendPacket(Integer.parseInt(aQuiTextField.getText()), bos.toByteArray());
        }*/
    }

    private void addContact() throws IOException {
        String input = JOptionPane.showInputDialog(this, "Entrez l'ID de la personne que vous souhaitez ajouter à votre liste de contacts:");
        String name = JOptionPane.showInputDialog(this, "Quel est le nom de votre contact ?");
        if (input != null && name != null) {
            int id = Integer.parseInt(input);
            client.askAddContact(id);
            client.addContact(id, name);
            updateContactList();
        }
    }

    private void createGroup() throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        // byte 1 : create group on server
        dos.writeByte(1);
        String input = JOptionPane.showInputDialog(this, "Combien de personnes voulez-vous ajouter à votre groupe ?");
        int nb = Integer.parseInt(input);
        if (input != null) {
            // nb members
            dos.writeInt(nb);
        }
        dos.write(client.getIdentifier());
        for (int i = 1; i < nb; i++) {
            // Pour chaque personne, demandez à l'utilisateur l'identifiant de la personne
            input = JOptionPane.showInputDialog(this, "Entrez l'ID de la personne " + i + " :");
            if (input != null) {
                int id = Integer.parseInt(input);
                // Ajoutez l'identifiant de la personne à la liste des membres
                dos.writeInt(id);
            }


        }
        dos.flush();
        client.sendPacket(0, bos.toByteArray());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Nombre d'interfaces à lancer
                int numberOfInterfaces = 4;

                for (int i = 0; i < numberOfInterfaces; i++) {
                    InstantMessengerGUI gui = new InstantMessengerGUI();
                    gui.setVisible(true);
                }
            }
        });
    }
}

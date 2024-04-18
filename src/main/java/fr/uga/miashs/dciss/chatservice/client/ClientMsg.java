/*
 * Copyright (c) 2024.  Jerome David. Univ. Grenoble Alpes.
 * This file is part of DcissChatService.
 *
 * DcissChatService is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * DcissChatService is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.uga.miashs.dciss.chatservice.client;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.uga.miashs.dciss.chatservice.common.Packet;
import fr.uga.miashs.dciss.chatservice.server.GroupMsg;
import fr.uga.miashs.dciss.chatservice.server.ServerMsg;
import fr.uga.miashs.dciss.chatservice.server.UserMsg;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;
/**
 * Manages the connection to a ServerMsg. Method startSession() is used to²
 * establish the connection. Then messages can be send by a call to sendPacket.
 * The reception is done asynchronously (internally by the method receiveLoop())
 * and the reception of a message is notified to MessagesListeners. To register
 * a MessageListener, the method addMessageListener has to be called. Session
 * are closed thanks to the method closeSession().
 */
public class ClientMsg {

	private String serverAddress;
	private int serverPort;

	private Socket s;
	private DataOutputStream dos;
	private DataInputStream dis;

	private int identifier;

	private List<MessageListener> mListeners;
	private List<ConnectionListener> cListeners;

	private String name;
	//private UserMsg userServer;

	private Map<Integer,String> contacts ;
	private Logger LOG = Logger.getLogger(ClientMsg.class.getName());



	/**
	 * Create a client with an existing id, that will connect to the server at the
	 * given address and port
	 * 
	 * @param id      The client id
	 * @param address The server address or hostname
	 * @param port    The port number
	 */
	public ClientMsg(int id, String name, String address, int port) {
		if (id < 0)
			throw new IllegalArgumentException("id must not be less than 0");
		if (port <= 0)
			throw new IllegalArgumentException("Server port must be greater than 0");
		serverAddress = address;
		serverPort = port;
		identifier = id;
		mListeners = new ArrayList<>();
		cListeners = new ArrayList<>();
		this.name = name;
		contacts = new TreeMap<Integer,String>();
		/*try {
			ServerMsg server = new ServerMsg(port); // Création de l'instance de ServerMsg avec le port spécifié
			userServer = new UserMsg(id, server); // Initialisation de UserMsg avec l'identifiant et l'objet ServerMsg
		} catch (IOException e) {

			e.printStackTrace(); // Affichage de l'erreur
		}*/
	}

	/**
	 * Create a client without id, the server will provide an id during the the
	 * session start
	 * 
	 * @param address The server address or hostname
	 * @param port    The port number
	 */

	public ClientMsg(String address, int port) throws IOException {
		this(0, "defaultName", address, port);
	}

	public String getName() {
		return name;
	}


/*	public void setName(String name) {
		this.name = name;
		Set<UserMsg> s = new HashSet<>();

		// pas s'envoyer le changement à soit meme
		s.remove(this);

		ByteBuffer buf = ByteBuffer.allocate(1+name.getBytes().length);
		buf.put((byte) 9);
		buf.put(name.getBytes());
		// on a mis 0 en dest mais on aurai pu mettre n'importe quoi
		Packet p = new Packet(identifier,0,buf.array());
		for (UserMsg u : s) {
			u.process(p);
		}
	}
*/


	public Map<Integer,String> getContacts(){
		return contacts;
	}
	/**
	 * Register a MessageListener to the client. It will be notified each time a
	 * message is received.
	 * 
	 * @param l
	 */
	public void addMessageListener(MessageListener l) {
		if (l != null)
			mListeners.add(l);
	}
	protected void notifyMessageListeners(Packet p) {
		mListeners.forEach(x -> x.messageReceived(p));
	}
	
	/**
	 * Register a ConnectionListener to the client. It will be notified if the connection  start or ends.
	 * 
	 * @param l
	 */
	public void addConnectionListener(ConnectionListener l) {
		if (l != null)
			cListeners.add(l);
	}
	protected void notifyConnectionListeners(boolean active) {
		cListeners.forEach(x -> x.connectionEvent(active));
	}


	public int getIdentifier() {
		return identifier;
	}

	/**
	 * Method to be called to establish the connection.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void startSession() throws UnknownHostException {
		if (s == null || s.isClosed()) {
			try {
				s = new Socket(serverAddress, serverPort);
				dos = new DataOutputStream(s.getOutputStream());
				dis = new DataInputStream(s.getInputStream());
				dos.writeInt(identifier);
				dos.flush();
				if (identifier == 0) {
					identifier = dis.readInt();
				}
				if (name == null) {
					name = "defaultName  "+identifier;
				}
				// start the receive loop
				new Thread(() -> receiveLoop()).start();
				notifyConnectionListeners(true);
			} catch (IOException e) {
				e.printStackTrace();
				// error, close session
				closeSession();
			}
		}
	}

	/**
	 * Send a packet to the specified destination (etiher a userId or groupId)
	 * 
	 * @param destId the destinatiion id
	 * @param data   the data to be sent
	 */
	public void sendPacket(int destId, byte[] data) {
		try {
		//	LOG.warning("dos : " + dos);
			synchronized (dos) {
				dos.writeInt(destId);
				dos.writeInt(data.length);
				dos.write(data);
				dos.flush();
			}
		} catch (IOException e) {
			// error, connection closed
			closeSession();
		}
		
	}

	//changer le nom des deux méthodes suivantes
	// Méthode pour envoyer un fichier avec un titre (elle sera utiliser dans la méthode suivante : sendFile. Cette dernière permet de récupérer les données et de les envoyés grâce à la méthode sendFileAndTitle)
	public void sendFileAndTitle(int destId, byte[] data, byte[] title) {
		try {
			synchronized (dos) {
				dos.writeInt(destId);
				dos.writeInt(data.length);
				dos.write(data);
				dos.writeInt(title.length); //on envoie la taille du titre
				dos.write(title); //on envoie le titre
				dos.flush();
			}
		} catch (IOException e) {
			// error, connection closed
			closeSession();
		}
		
	}
	// Méthode pour envoyer un fichier grâce à la méthode sendFileAndTitle 
	public void sendFile(int destId, Path filePath, String title) throws IOException { 
		byte[] titleBytes = title.getBytes(); //on récupère le titre du fichier et on le transforme en bytes
		byte[] fileData = Files.readAllBytes(filePath); //on récupère le contenu fichier et on le transforme en bytes
		sendFileAndTitle(destId, fileData, titleBytes); //on envoie le fichier via la métjode sendPacket
		System.out.println("Le fichier s'est bien envoyé"); //on vérifie que le fichier s'est bien envoyé
		
	}


	public void setName(String name) {

		// Le client modifie son nom
		this.name = name;

		// Le client envoie le packet au serveur
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos1 = new DataOutputStream(bos);

		try {
			dos1.writeByte(9);
			dos1.writeUTF(name);
			dos1.flush();
			LOG.warning("dos size : " + bos.toByteArray());

			sendPacket(0, bos.toByteArray());
			System.out.println("Packet de modification du pseudo envoyé ");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void askAddContact(int id) {

		// Le client envoie le packet au serveur
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		dos = new DataOutputStream(bos);

		try {
			dos.writeByte(5);
//			dos.writeInt(4);
			dos.writeInt(id);
			dos.flush();
			sendPacket(0, bos.toByteArray());
			System.out.println("Demande d'ajout de contact au serveur envoyée");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//
//	public void addContact(int id, String name) {
//		// Le client ajoute un nom à ses contacts
//		if(askAddContact(id) ){
//			contacts.put(id, name);
//		}
//		else
//
//		contacts.put(id, name);
//	}
	
	/*public void sendFile(int destId, Paths filePath, byte [] fileTitle) throws IOException {
		byte[] fileData = Files.readAllBytes(Paths.get(filePath.toString()));
		//ajouter nom fichier et type fichier : dcp faire une autre sendPacket diff avec en paramètre nom et type
		sendPacket(destId, fileData);/*
	

	/**
	 * Start the receive loop. Has to be called only once.
	 */
	private void receiveLoop() {
		try {
			while (s != null && !s.isClosed()) {

				int sender = dis.readInt();
				int dest = dis.readInt();
				int length = dis.readInt();
				byte[] data = new byte[length];
				dis.readFully(data);
				notifyMessageListeners(new Packet(sender, dest, data));

			}
		} catch (IOException e) {
			// error, connection closed
		}
		closeSession();
	}

	
	public void closeSession() {
		try {
			if (s != null)
				s.close();
		} catch (IOException e) {
		}
		s = null;
		notifyConnectionListeners(false);
	}

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		ClientMsg c = new ClientMsg("localhost", 1666);
		//c.setName("Ilias");
		//c.askAddContact(1);

		// //test fichier
		// c.addMessageListener(p -> {
		// 	if (p.titleBytes != null) {
		// 		String title = new String(p.titleBytes);
		// 		try (FileOutputStream fos = new FileOutputStream(title)) {
		// 			fos.write(p.data);
		// 		} catch (IOException e) {
		// 			System.err.println("Error while saving file: " + e.getMessage());
		// 		}
		// 	} else {
		// 		System.out.println(p.srcId + " says to " + p.destId + ": " + new String(p.data));
		// 	}
		// });

		// add a dummy listener that print the content of message as a string
		c.addMessageListener(p -> System.out.println(p.srcId + " says to " + p.destId + ": " + new String(p.data)));
		
		// add a connection listener that exit application when connection closed
		c.addConnectionListener(active ->  {if (!active) System.exit(0);});

		c.startSession();
		//c.setName("toto");


		//test fichier : envoyer fichier
		//  Path path = Paths.get("path/to/your/file.txt");
		//  byte[] data = Files.readAllBytes(path);
		//  byte[] titleBytes = path.getFileName().toString().getBytes();
		//  dos.writeInt(8); //on lui dit qu'on est dans le protocole 8 : celui des fichiers
		 
		//  c.sendPacket(new Packet(1, 2, data, titleBytes));


		System.out.println("Vous êtes : " + c.getName());

		if (c.getIdentifier() == 5) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			// protocole 8 : fichiers
			dos.writeByte(8);
			// list members
			dos.writeInt(1);
			dos.writeInt(3);
			dos.flush();

			c.sendPacket(0, bos.toByteArray());

		}


		// Thread.sleep(5000);

		// l'utilisateur avec id 4 crée un grp avec 1 et 3 dedans (et lui meme)
		if (c.getIdentifier() == 4) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			// byte 1 : create group on server
			dos.writeByte(1);

			// nb members
			dos.writeInt(2);
			// list members
			dos.writeInt(1);
			dos.writeInt(3);
			dos.flush();

			c.sendPacket(0, bos.toByteArray());

		}

		/* TEST POUR METHODE REMOVE USER
		if (c.getIdentifier() == 5) {

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			// byte 1 : create group on server
			dos.writeByte(4);

			// nb members
			dos.writeInt(-1);
			// list members
			dos.writeInt(1);
			dos.writeInt(1);
			dos.flush();


			c.sendPacket(0, bos.toByteArray());

		} */
		
		

		Scanner sc = new Scanner(System.in);
		String lu = null;

		System.out.println("\nNouveau nom d'utilisateur : ");
		String newUsername = sc.nextLine();
		c.setName(newUsername);
		System.out.println("Vous êtes " + c.getName());

		while (!"\\quit".equals(lu)) {
			try {
				// test modification de name

//
//				System.out.println("Qui voulez vous ajouter dans vos contacts");
//				int id = Integer.parseInt(sc.nextLine());
//				c.askAddContact(id);
//				System.out.println("Demande d'ajout de contact envoyée");

				System.out.println("A qui voulez vous écrire ? ");
				int dest = Integer.parseInt(sc.nextLine());

				System.out.println("Votre message ? ");
				lu = sc.nextLine();
				c.sendPacket(dest, lu.getBytes());
			} catch (InputMismatchException | NumberFormatException e) {
				System.out.println("Mauvais format");
			}

		}

		/*
		 * int id =1+(c.getIdentifier()-1) % 2; System.out.println("send to "+id);
		 * c.sendPacket(id, "bonjour".getBytes());
		 * 
		 * 
		 * Thread.sleep(10000);
		 */

		c.closeSession();

	}
	// methode pour distinguer les messages recus peu importe le format
	private void receiveFile() {
		try {
			while (s != null && !s.isClosed()) {
				int sender = dis.readInt();
				int dest = dis.readInt();
				int length = dis.readInt();
				byte[] data = new byte[length];
				dis.readFully(data);
				String messageType = getMessageType(data);
				System.out.println("Received a " + messageType + " message.");
				notifyMessageListeners(new Packet(sender, dest, data));
			}
		} catch (IOException e) {
			// error, connection closed
		}
		closeSession();
	}


	public String getMessageType(byte[] data) {
		String type = "unknown";
		if (data != null && data.length > 0) {
			switch (data[0]) {
				case (byte) 0xFF:
					if (data.length > 1 && data[1] == (byte) 0xD8) {
						type = "image/jpeg";
					}
					break;
				case (byte) 0x89:
					if (data.length > 3 && data[1] == (byte) 0x50 && data[2] == (byte) 0x4E && data[3] == (byte) 0x47) {
						type = "image/png";
					}
					break;
				case (byte) 0x47:
					if (data.length > 3 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46 && data[3] == (byte) 0x38) {
						type = "image/gif";
					}
					break;
				case (byte) 0x25:
					if (data.length > 4 && data[1] == (byte) 0x50 && data[2] == (byte) 0x44 && data[3] == (byte) 0x46) {
						type = "application/pdf";
					}
					break;
				default:
					type = "text";
					break;
			}
		}
		return type;


	}

	/*public Object fileTypeReader(){
		// faire une methode qui transforme le tableau de bytes en le type de fichiers que la methode precedente a dit.
	String type = getMessageType(data);
	Object file= null;

	switch (type){
		case "image/jpeg":
		case "image/png":
		case "image/gif":
			try {
				ByteArrayOutputStream bis = new ByteArrayOutputStream(data);
				BufferedImage bImage = ImageIO.read(bis);
				file = bImage;
			}
			catch (IOException e){
				e.printStackTrace();
			}
			break;
		case "text":
			file = new String(data, StandardCharsets.UTF_8);
			break;
		default:
			System.out.println("Type de fichier non pris en charge: " + type);
			break;
	}
	return file;
	}

	 */
}

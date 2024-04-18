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

package fr.uga.miashs.dciss.chatservice.server;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import fr.uga.miashs.dciss.chatservice.common.Packet;

public class ServerPacketProcessor implements PacketProcessor {
	private final static Logger LOG = Logger.getLogger(ServerPacketProcessor.class.getName());
	private ServerMsg server;

	public ServerPacketProcessor(ServerMsg s) {
		this.server = s;
	}

	@Override
	public void process(Packet p) {
		LOG.info("Un paket est reçu par le serveur");
		// ByteBufferVersion. On aurait pu utiliser un ByteArrayInputStream + DataInputStream à la place
		ByteBuffer buf = ByteBuffer.wrap(p.data);
		byte type = buf.get();
		LOG.info("ServerPacketProcessor: type=" + type);

		if (type == 1) { // cas creation de groupe
			createGroup(p.srcId, buf);
		} else if (type == 2) { // cas suppression de groupe
			deleteGroup(p.srcId, buf);
		} else if (type == 3) { //ajout d'un ou plusieurs membres
			addMembers(p.srcId, buf);
		} else if (type == 4) { //suppression d'un ou plusieurs membres
			removeMembers(p.srcId, buf);
		}else if (type == 5) { //ajout d'un contact
//
			LOG.info("userId " + p.srcId + " a demandé un ajout " + buf.getInt() + " à sa liste de contacts");
			int userId = p.srcId;
			int contactId = buf.getInt();
//			String contactPseudo = new String(p.data);
			if (server.getUser(contactId) != null) {
//				server.getUser(userId).addContact(server.getUser(contactId));
				LOG.info("userId " + userId + " a ajouté le contact " + contactId + " à sa liste de contacts");
				confirmAddContact(userId, contactId);
			} else {
				LOG.warning("userId " + userId + " a essayé d'ajouter un contact inexistant");
			}
//		}else if (type == 6) { //modification nom d'un contact
//			renameContact(p.srcId, buf);
//		}else if (type == 7 ) { //suppression d'un contact
//			removeContact(p.srcId, buf);
//		}else if (type ==8) { //envoi de fichier
//			sendFile(p.destId, p.srcId, buf);
		} /*else if (type == 9) {
				int userId = p.srcId;
				int length = buf.getInt();
				byte [] usernameByte = new byte[length];
				buf.get(usernameByte);
				String usernameByteString = new String(usernameByte, StandardCharsets.UTF_8);
				String name = new String(p.data);
				UserMsg u = server.getUser(userId);
				server.getUser(userId).setName(usernameByteString);
				LOG.info("userId " + userId + " a mis à jour son username en " + usernameByteString);
				//TRACE : print every userid and their username
				LOG.info(server.getUser(p.srcId).getId() + " " + server.getUser(p.srcId).getName());*/
		else if (type == 9) {
			int userId = p.srcId;
			String usernameByteString = new String(p.data,1,p.data.length-1,StandardCharsets.UTF_8);
			UserMsg user = server.getUser(userId);
			if (user != null) {
				user.setName(usernameByteString);
				LOG.info("userId " + userId + " a mis à jour son nom en " + usernameByteString);
			} else {
				LOG.warning("Utilisateur avec l'ID " + userId + " non trouvé pour mettre à jour le nom.");
			}
		} else {
			LOG.warning("Server message of type=" + type + " not handled by procesor");
		}
	}

	public void createGroup(int ownerId, ByteBuffer data) {
		int nb = data.getInt();
		GroupMsg g = server.createGroup(ownerId);
		for (int i = 0; i < nb; i++) {
			g.addMember(server.getUser(data.getInt()));
		}
	}

	public void deleteGroup(int userId, ByteBuffer data) {
		int groupId = data.getInt();
		GroupMsg group = server.getGroup(groupId);
		if (group.getOwner().getId() != userId) {
			throw new IllegalArgumentException("User with id=" + userId + " is not the owner of the group with id=" + groupId);
		}
		server.removeGroup(groupId);
	}

	public void addMembers(int userId, ByteBuffer data) {
		int groupId = data.getInt();
		GroupMsg group = server.getGroup(groupId);
		if (group.getOwner().getId() != userId) {
			throw new IllegalArgumentException("User with id=" + userId + " is not the owner of the group with id=" + groupId);
		}
		int nb = data.getInt();
		for (int i = 0; i < nb; i++) {
			group.addMember(server.getUser(data.getInt()));
		}
	}

	public void removeMembers(int userId, ByteBuffer data) {
		int groupId = data.getInt();
		GroupMsg group = server.getGroup(groupId);
		if (group.getOwner().getId() != userId) {
			throw new IllegalArgumentException("User with id=" + userId + " is not the owner of the group with id=" + groupId);

		}
		int nb = data.getInt();
		for (int i = 0; i < nb; i++) {
			group.removeMember(server.getUser(data.getInt()));
		}
	}

	public void confirmAddContact(int userId, int contactId) {
		LOG.info("INFO 2 userId " + userId + " a ajouté le contact " + contactId + " à sa liste de contacts");
		UserMsg contact = server.getUser(contactId);
		String contactName = contact.getName();

		ByteBuffer buf = ByteBuffer.allocate(4 + contactName.getBytes().length);
		buf.putInt(contactId);
		buf.put(contactName.getBytes());

		UserMsg user = server.getUser(userId);
		user.process(new Packet(0, userId, buf.array()));
	}
	/*	public void addContact(int userId, ByteBuffer data) {
			int contactId = data.getInt();
			server.getUser(userId).addContact(server.getUser(contactId));
		}

		public void removeContact(int userId, ByteBuffer data) {
			int contactId = data.getInt();
			server.getUser(userId).removeContact(server.getUser(contactId));
		}

		public void renameContact(int userId, ByteBuffer data) {
			int contactId = data.getInt();
			String newName = new String(data.array(), data.position(), data.remaining());
			server.getUser(userId).renameContact(server.getUser(contactId), newName);
		}
*/
		public void sendFile(int destId, int userId, ByteBuffer data, ByteBuffer title) {
			int size = data.getInt();
			byte[] fileData = new byte[size];
			data.get(fileData);
			server.getUser(destId).receiveFile(userId, fileData, title);
		}
		//ancienne version 
// 		public void sendFile(int destId, int userId, ByteBuffer data) {
// 			int size = data.getInt();
// 			byte[] file = new byte[size];
// 			data.get(file);
// 			server.getUser(destId).receiveFile(userId, file);
// 		}
}
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

import fr.uga.miashs.dciss.chatservice.common.Packet;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.*;


public class PacketListener implements MessageListener {

    private ClientMsg client;
    private Logger LOG = Logger.getLogger(ClientMsg.class.getName());


    PacketListener(ClientMsg client) {
        this.client = client;
        client.addMessageListener(this);
    }
    @Override
    public void messageReceived(Packet p) {
        if (p.srcId==0 && p.data[0] == 5 ) {
            addContact(p);
        }
    }

    public void addContact(Packet p) {
        ByteBuffer buffer = ByteBuffer.wrap(p.data, 1, p.data.length - 1);

        // Assuming the first 4 bytes represent the contact ID
        int contactId = buffer.getInt();
        LOG.info("Received contact id: " + contactId);
        // Assuming the remaining bytes represent the contact name
        String contactName = new String(p.data, 5, p.data.length - 4 - 1, StandardCharsets.UTF_8);
        LOG.info("Received contact name: " + contactName);
        // Now you can add the contact to the client's contact list
        client.addContact(contactId, contactName);

    }
}

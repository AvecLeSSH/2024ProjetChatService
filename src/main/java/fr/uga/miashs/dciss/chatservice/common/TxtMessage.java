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

package fr.uga.miashs.dciss.chatservice.common;

public class TxtMessage extends Packet {

    public static byte TYPE=56;
    public TxtMessage(int srcId, int destId, byte[] data) {
        super(srcId, destId, data);
        if (data[0] != TYPE) {
            throw new IllegalArgumentException("The first byte of the data must be the type of the message");
        }
    }

    public String getMessage() {
        return new String(data,1,data.length-1);
    }
}

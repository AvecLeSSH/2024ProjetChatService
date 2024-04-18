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

public class Message {

    private int id_src;
    private int id_dest;
    private String content;

    public Message(int id_src, int id_dest, String content) {
        this.id_src = id_src;
        this.id_dest = id_dest;
        this.content = content;
    }

    public int getUserId() {
        return id_src;
    }

    public int getDestId() {
        return id_dest;
    }

    public String getContent() {
        return content;
    }

}

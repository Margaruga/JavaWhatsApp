
package com.nqysit.whatsapp;

import java.util.*;

public final class Contact {

    private final String number;
    private final ArrayDeque<String> newmessages;

    public Contact(String number) {

        this.number = number;
        this.newmessages = new ArrayDeque<>();
    }

    public void addIncomingMessage(String message) {

        this.newmessages.add(message);
    }

    public String getUnseenMessage() {

        if (this.newmessages.isEmpty()) {
            return "";
        } else {
            return this.newmessages.remove();
        }
    }

    public boolean AreThereNewMessages() {

        return (!this.newmessages.isEmpty());
    }

    public String getNumber() {

        return this.number;
    }
}

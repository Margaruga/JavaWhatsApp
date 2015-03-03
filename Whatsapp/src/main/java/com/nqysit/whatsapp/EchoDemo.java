/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nqysit.whatsapp;

import java.util.ArrayList;

/**
 *
 * @author developer
 */
public class EchoDemo {

    public static void main(String[] args) throws ClientIsNotInit {

        boolean run = true;
        
        Client.init("/usr/bin/python", "JavaWhatsApp/yowsup/yowsupclient.py");
        
        while (run) {

            Client.ListenIncomingMessages();

            ArrayList<Contact> unreaden = Client.getUnseenContacts();

            if (!unreaden.isEmpty()) {

                for (Contact ur : unreaden) {

                    String from = ur.getNumber();
                    String text = ur.getUnseenMessage();

                    System.out.println(from + "-" + text);

                    if (text.equals("stop")) {
                        Client.killit();
                        run = false;
                        break;
                    }

                    Client.sendMessage(from, text);

                }

            } else {

                System.out.println("There are not new messages");

            }
        }
    }

}

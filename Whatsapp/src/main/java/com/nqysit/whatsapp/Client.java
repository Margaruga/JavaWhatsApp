/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nqysit.whatsapp;

import java.io.*;
import java.util.*;
import com.google.gson.*;

/**
 *
 * @author developer
 */
public final class Client {

    private static boolean isInit = false;
    private static Process yowsup = null;
    private static BufferedReader input;
    private static BufferedWriter output;
    private static final HashMap<String, Contact> contactos = new HashMap<>();
    private static final String getJSON = "{\"action\":\"get\"}";
    private static final String killJSON = "{\"action\":\"kill\"}";

    /**
    * This launchs a background process that will be hearing for new messages.
    * 
    * @param pythonPath Path to python interpreter
    * @param yowsupPath Path to the yowsup program
    */
    public static void init(String pythonPath, String yowsupPath) {
        
        try {
        
            if (!Client.isInit) {
                // TO-DO Check if the paths exists
                Client.yowsup = Runtime.getRuntime().exec(pythonPath + " " + yowsupPath);
                Client.input  = new BufferedReader(new InputStreamReader(Client.yowsup.getInputStream()));
                Client.output = new BufferedWriter(new OutputStreamWriter(Client.yowsup.getOutputStream()));
                Client.isInit = true;
            }
        
        } catch (IOException err) {
            System.out.println(Arrays.toString(err.getStackTrace()));
        }
    }

    /**
    * Get the new messages from the listening process
    *
    * @throws com.nqysit.whatsapp.ClientIsNotInit
    */
    public static void ListenIncomingMessages() throws ClientIsNotInit {
        
        if (Client.isInit) {
            String answer = Client.pipe(Client.getJSON);
            Client.parseMessages(answer);
        } else {
            throw new ClientIsNotInit("You have not done WhatsApp.init()");
        }
        
    }

    /**
     *
     * @param to the number of the contact you are sending a message.
     * @param message the message itself.
     * @throws com.nqysit.whatsapp.ClientIsNotInit
     */
    public static void sendMessage(String to, String message) throws ClientIsNotInit {

        if (Client.isInit) {
            String sendJSON = "{\"action\":\"send\",\"to\":\"" + to + "\",\"message\":\"" + message + "\"}";
            Client.pipe(sendJSON);
        } else {
            throw new ClientIsNotInit("You have not done WhatsApp.init()");
        }
    }
    
    /**
     * Kill the backgrounds processes
     */
    public static void killit() {

        Client.sendKill();
        Client.yowsup.destroy();
        Client.isInit = false;
    }

    /**
     *
     * @return a list filled with contacts that have unreaden messages
     */
    public static ArrayList<Contact> getUnseenContacts() {

        ArrayList<Contact> contacts = new ArrayList<>();

        for (Contact c : Client.contactos.values()) {
            if (c.AreThereNewMessages()) {
                contacts.add(c);
            }
        }
        return contacts;
    }

    private static void saveMessage(JsonObject message) {

        String from = message.get("from").toString().split("@")[0];
        from = from.replace("\"", "");
        String content = message.get("message").toString().replace("\"", "");

        if (Client.contactos.containsKey(from)) {

            Client.contactos.get(from).addIncomingMessage(content);

        } else {

            Contact newcontacto = new Contact(from);
            newcontacto.addIncomingMessage(content);
            Client.contactos.put(from, newcontacto);

        }
    }

    private static void parseMessages(String messages) {

        JsonElement json = new JsonParser().parse(messages);
        JsonObject jobject = json.getAsJsonObject();
        int i = 0;

        while (true) {

            json = jobject.get(Integer.toString(i));

            if (json != null) {

                JsonObject message = json.getAsJsonObject();
                Client.saveMessage(message);
                i++;

            } else {

                break;
            }
        }
    }

    private static void sendKill() {
        Client.pipe(Client.killJSON);
    }

    private static String pipe(String message) {

        String rcv;
        String tosend = message + "\n";
        try {
            Client.output.write(tosend);
            Client.output.flush();
            rcv = Client.input.readLine();
            return rcv;
        } catch (Exception err) {
            System.out.println(err.getMessage());
        }
        return "";
    }

}

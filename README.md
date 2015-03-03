

# JavaWhatsApp

Send and listen messages through WhatsApp from a Java Program.

This is done thanks to Yowsup: https://github.com/tgalal/yowsup

This works as is today but it hasn't been tested deeply, I just shared it because
it may be helpful for someone.

# Steps

  * Register your phone number in yowsup, follow these instructions.
  https://github.com/tgalal/yowsup/wiki/yowsup-cli-2.0

  * Fill the fields in yowsup/yowsup-cli.config
- cc=
- phone=
- id=
- password=

  * The Whatsapp folder is a Netbeans Maven project that contains a package with all the
  necessary. It also has a demo to show how it works.

    ```java
    package com.nqysit.whatsapp;

    import java.util.ArrayList;


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

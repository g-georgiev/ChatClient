package Model;

import AllClientsView.Controller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Georgi on 16.11.2015 г..
 */
public class SocketListener extends Thread {
    private static ObservableList clientNames;
    private final String host = "localhost";
    private final int port = 8008;
    private Socket socket;
    private String serverMessage;
    private Controller controllerAllClientsView;

    public SocketListener(Controller controller) {
        this.controllerAllClientsView = controller;
        socket = new Socket();
    }

    @Override
    public void run() {
        try {
            /*connection init*/
            socket.connect(new InetSocketAddress(host, port));
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            /*client init*/
            //send INIT msg to server
            outputStream.write(" &COMMAND&INIT");
            outputStream.newLine();
            outputStream.flush();

            /*ready to work. Starting to listen to server socket for ADD updates*/
            String[] msgParts;
            for (; ; ) {
                serverMessage = inputStream.readLine();
                if (serverMessage == null) {
                    socket.close();
                    break;
                }
                System.out.println(serverMessage);
                msgParts = serverMessage.split("&");

                if (msgParts[2].equals("INIT")) {
                    //set current self id
                    ClientContext.setCurrentId(Integer.parseInt(msgParts[1]));

                    if (!msgParts[0].equals("")) {
                        //parse and set all other online clients
                        String[] otherClients = msgParts[0].split(";");
                        for (String clientId : otherClients) {
                            ClientContext.setClientName(Integer.parseInt(clientId), clientId);
                        }
                        //populate online clients list
                        clientNames = FXCollections.observableArrayList(otherClients);
                        updatePeople(clientNames);
                    }

                } else if (msgParts[2].equals("ADD")) {
                    ClientContext.setClientName(Integer.parseInt(msgParts[0]), msgParts[0]);
                    //populate online clients list
                    clientNames = FXCollections.observableArrayList(ClientContext.getAllClientNames());
                    updatePeople(clientNames);
                } else if (msgParts[2].equals("REMOVE")) {
                    ClientContext.removeClient(msgParts[0]);
                    //populate online clients list
                    clientNames = FXCollections.observableArrayList(ClientContext.getAllClientNames());
                    updatePeople(clientNames);
                } else {
                    //get current client id for later use
                    String currentClientID = ClientContext.getCurrentId().toString();
                    //define a collection for the selected people
                    ArrayList<String> selectedPeople = new ArrayList<>();
                    //add the source person, he's obviously in the chat...
                    selectedPeople.add(msgParts[0]);
                    //add everyone else in the chat that ISN'T the current user. Fuck him
                    for (String clientID : msgParts[1].split(";")) {
                        if (!clientID.equals(currentClientID)) selectedPeople.add(clientID);
                    }
                    //Sort the collection
                    Collections.sort(selectedPeople);

                    //join the sorted collection into a chatID
                    String chatID = String.join("", selectedPeople);

                    //attempt to find the controller for the joined chatID
                    ChatView.Controller chatViewController = ClientContext.getActiveChatController(chatID);

                    //restore escaped amps
                    String textToDisplay = msgParts[2].replaceAll("#amp;", "&");

                    //if the controller exists, then update its chat area
                    if (chatViewController != null) {
                        updateChatArea(chatViewController, "\nPerson " + msgParts[0] + " said: " + textToDisplay);
                    } else { //otherwise start a new chat window
                        startNewChat(selectedPeople.toArray(new String[selectedPeople.size()]), "\nPerson " + msgParts[0] + " said: " + textToDisplay);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startNewChat(final String[] selectedPeople, final String initialMsg) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                controllerAllClientsView.startNewChat(selectedPeople, initialMsg);
            }
        });
    }

    private void updateChatArea(final ChatView.Controller chatViewController, final String message) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                chatViewController.updateChatArea(message);
            }
        });
    }

    private void updatePeople(final ObservableList clientNames) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                controllerAllClientsView.updatePeople(clientNames);
            }
        });
    }
}

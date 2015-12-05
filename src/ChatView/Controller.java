package ChatView;

import Model.ClientContext;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Georgi on 1.12.2015 г..
 */
public class Controller implements Initializable {
    private final String host = "localhost";
    private final int port = 8008;
    @FXML
    private Label peopleLabel;
    @FXML
    private TextField newMsgField;
    @FXML
    private TextArea chatArea;

    private String[] people;

    public void setInitialMsg(String initialMsg) {
        chatArea.appendText(initialMsg);
    }

    public void setPeople(String[] people) {
        this.people = people;
        peopleLabel.setText("Talking to people: " + String.join(", ", people));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //pointless validation
        if (peopleLabel == null)
            throw new AssertionError("fx:id=\"peopleLabel\" was not injected: check your FXML file 'ChatView.fxml'.");
        if (newMsgField == null)
            throw new AssertionError("fx:id=\"newMsgField\" was not injected: check your FXML file 'ChatView.fxml'.");
        if (chatArea == null)
            throw new AssertionError("fx:id=\"chatArea\" was not injected: check your FXML file 'ChatView.fxml'.");

        //disable the text area control, so that its only manipulated by the program
        chatArea.setDisable(true);
    }

    /*
     * On pressing the Enter key the message in newMsgField put into a packet and sent to the server.
     * The chatArea is also updated.
     */
    @FXML
    public void onEnter(ActionEvent actionEvent) {
        //get the text from newMsgField
        String textToSend = newMsgField.getText();

        //if there is no text then fuck off
        if (textToSend != null && !textToSend.equals("")) {
            //append the text that was just send to the textArea, so that the patient can see what he typed
            updateChatArea("\nYou said: " + textToSend);
            newMsgField.clear();

            //escape amps before sending to the server
            textToSend = textToSend.replaceAll("&", "#amp;");
            System.out.println("Trying to send: " + textToSend);
            //Prepare a message to be sent
            StringBuffer message = new StringBuffer();
            //append source client to message
            message.append(ClientContext.getCurrentId()).append("&");
            //append destination clients and message body
            message.append(String.join(";", people)).append("&").append(textToSend);

            try {
                Socket socket = new Socket();
                //connect to the server
                socket.connect(new InetSocketAddress(host, port));
                //get output stream
                BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                //write message
                outputStream.write(message.toString());
                //write a new line, so that the server can getLine()
                outputStream.newLine();
                //ALWAYS flush after you've done your business
                outputStream.flush();
                //disconnect from server
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void updateChatArea(String newText) {
        chatArea.appendText(newText);
    }

}

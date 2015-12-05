package AllClientsView;

import Model.ClientContext;
import Model.SocketListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Created by Georgi on 16.11.2015 г..
 */
public class Controller implements Initializable {
    @FXML
    public Label clientId;

    @FXML
    private Button chatButton;

    @FXML
    private ListView<String> clientsList;

    private SocketListener chatServerConnection;
    private final String host = "localhost";
    private final int port = 8008;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //pointless validation
        if (chatButton == null)
            throw new AssertionError("fx:id=\"chatButton\" was not injected: check your FXML file 'AllClientsView.fxml'.");
        if (clientsList == null)
            throw new AssertionError("fx:id=\"clientsList\" was not injected: check your FXML file 'AllClientsView.fxml'.");

        //make the clients list view multi-selectable
        clientsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //start Server connection
        chatServerConnection = new SocketListener(this);
        chatServerConnection.start();

        //actively wait to receive current client id. Shouldn't be too long.
        Integer currentId;
        while((currentId = ClientContext.getCurrentId()) == null);
        clientId.setText("Your id is: " + currentId.toString());

        //set Chat button event handler
        chatButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                //get the selected clients
                ObservableList<String> tempSelectedPeople = clientsList.getSelectionModel().getSelectedItems();
                String[] selectedPeople = tempSelectedPeople.toArray(new String[tempSelectedPeople.size()]);
                //sort the list, because startNewChat needs it sorted
                Arrays.sort(selectedPeople);

                if (ClientContext.getActiveChatController(String.join("", selectedPeople)) == null) {
                    //chat up the selected clients
                    startNewChat(selectedPeople, null);
                }
            }
        });

        clientsList.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    //get the selected clients
                    ObservableList<String> tempSelectedPeople = clientsList.getSelectionModel().getSelectedItems();
                    String[] selectedPeople = tempSelectedPeople.toArray(new String[tempSelectedPeople.size()]);
                    //sort the list, because startNewChat needs it sorted
                    Arrays.sort(selectedPeople);

                    if (ClientContext.getActiveChatController(String.join("", selectedPeople)) == null) {
                        //chat up the selected clients
                        startNewChat(selectedPeople, null);
                    }
                }
            }
        });
    }

    /**
     * Method for opening a new chat window
     *
     * @param selectedPeople people in the chat
     */
    public synchronized void startNewChat(String[] selectedPeople, String initialMsg) {
        //JavaFX new window creation bullshit start
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../ChatView/ChatView.fxml"));
        Parent root = null;
        try {
            root = (Parent) fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        //JavaFX new window creation bullshit end

        //form chatID
        String chatID = String.join("", selectedPeople);

        //remove the controller when the window is closed
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                ClientContext.removeController(chatID);
            }
        });

        //set window title
        stage.setTitle("Chat with people: " + String.join(",", selectedPeople));

        //get the new window's controller and set the people that will be chatted up
        ChatView.Controller controller = fxmlLoader.<ChatView.Controller>getController();
        controller.setPeople(selectedPeople);
        if (initialMsg != null)
            controller.setInitialMsg(initialMsg);
        //create a new conversation in the context
        ClientContext.setActiveChatController(chatID, controller);

        //show the window with update label (hopefully)
        stage.show();
    }

    /**
     * Method for updating online clients list control
     *
     * @param clientNames online clients to be displayed in the control
     */
    public void updatePeople(ObservableList clientNames) {
        clientsList.setItems(clientNames);
    }

    public void disconnectFromServer(){
        Socket socket = new Socket();
        try {
            //connect to the server
            socket.connect(new InetSocketAddress(host, port));
            //get output stream
            BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //write message
            outputStream.write(ClientContext.getCurrentId().toString() + "&COMMAND&DISCONNECT");
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

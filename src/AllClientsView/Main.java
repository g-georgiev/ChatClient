package AllClientsView;

import Model.ClientContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Georgi on 16.11.2015 г..
 */
public class Main extends Application {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(AllClientsView.Main.class, (java.lang.String[]) null);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("AllClientsView.fxml"));
            GridPane page = (GridPane) loader.load();
            Scene scene = new Scene(page);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Chat client");

            primaryStage.show();

            final AllClientsView.Controller controller = (AllClientsView.Controller)loader.getController();
            //remove the controller when the window is closed
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    controller.disconnectFromServer();
                    Platform.exit();
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
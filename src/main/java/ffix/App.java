package ffix;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/ffix/sample.fxml"));
        primaryStage.setTitle("Dot fix test");
        primaryStage.setScene(new Scene(root, 2000, 1300));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

package dsa.group.one.plagiarismchecker;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;

public class DashBoard extends Application {

    double version = 1.2;

    //this class is used to "create" the windows and other UI elements using the Stage class.
    @Override
    public void start(Stage window) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(DashBoard.class.getResource("dashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1010, 625);
//        window.setMinHeight(500);
//        window.setMinWidth(500);
        window.setTitle("PLAGCHECK 9000 v-" + version + " | A Plagiarism Checker by Group 1");
        window.setResizable(false);
        window.setScene(scene);
        window.show();

    }

    public static void main(String[] args) {
        launch();
    }
}
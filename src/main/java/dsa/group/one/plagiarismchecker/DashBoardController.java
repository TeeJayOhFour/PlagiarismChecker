package dsa.group.one.plagiarismchecker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import io.github.palexdev.materialfx.controls.MFXTextField;

public class DashBoardController {
    //The controller class handles the interaction between the fxml and java classes.
    @FXML public VBox parentWindow;
    @FXML public Button browseSource;
    @FXML public Button browsePattern;
    @FXML private Label welcomeText;
    @FXML private TextFlow sourceTxt;
    @FXML private TextFlow patternTxt;

    @FXML
    protected void otherStuff() {

        welcomeText.setText("Yeah, the button works");

        Text redText = new Text("YOUR MOM IS");
        redText.setFill(Color.RED);

        Text greenText = new Text(" GAY");
        greenText.setFill(Color.GREEN);

    }

    @FXML
    protected void browseSourceOnClick(ActionEvent event) {

        Stage window = (Stage) parentWindow.getScene().getWindow();

        FileChooser file = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        file.getExtensionFilters().add(extFilter);
        file.setTitle("Open Source");
        File selectedFile = file.showOpenDialog(window);

        if(selectedFile != null) {
            previewFile(selectedFile, sourceTxt);
        }
    }


    @FXML
    protected void browsePatternOnClick(ActionEvent event) {

        Stage window = (Stage) parentWindow.getScene().getWindow();

        FileChooser file = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        file.getExtensionFilters().add(extFilter);
        file.setTitle("Open Pattern");
        File selectedFile = file.showOpenDialog(window);

        if(selectedFile != null) {
            previewFile(selectedFile, patternTxt);
        }

    }

    protected void previewFile(File selectedFile, TextFlow target) {

        target.getChildren().clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            String text;
            while ((text = reader.readLine()) != null) {
                System.out.println(text);

                Text convertedTxt = new Text (text + "\n");
                target.getChildren().add(convertedTxt);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        target.setMaxHeight(313);
        target.setMaxWidth(258);

    }

    protected void algorithm() {

        //TODO: ZAKI'S PART



    }

}
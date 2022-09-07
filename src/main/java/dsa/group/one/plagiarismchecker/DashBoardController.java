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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.palexdev.materialfx.controls.MFXTextField;

public class DashBoardController {
    //The controller class handles the interaction between the fxml and java classes.
    @FXML public VBox parentWindow;
    @FXML public Button browseSource;
    @FXML public Button browsePattern;
    @FXML private Label welcomeText;
    @FXML private TextFlow sourceTxt;
    @FXML private TextFlow patternTxt;
    @FXML private Button checkBtn;

    //Global vars for files
    private File src = null;
    private File pat = null;

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
            src = selectedFile;
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
            pat = selectedFile;
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

    }

    @FXML
    protected void plagiarizeCheck(ActionEvent event) {

        int plagiarized = 0;
        int totalCharCount = 0;

        //null check
        if (src == null || pat == null) {
            welcomeText.setText("Error, please select a pattern and source file!");
            return;
        }

        checkBtn.setText("Checking...");
        checkBtn.disarm();

        String[] patList = null;
        String[] srcList = null;
        List<String> plagList = new ArrayList<>();

        String tempPattern;
        String tempSource;
        boolean sourceLooped = false;

        //converting to readers
        try (BufferedReader pattern = new BufferedReader(new FileReader(pat))) {

            try (BufferedReader source = new BufferedReader(new FileReader(src))) {


                patList = pattern.lines().collect(Collectors.joining()).split("\\.");
                srcList = source.lines().collect(Collectors.joining()).split("\\.");


            } catch (IOException e) {
                e.printStackTrace();
                welcomeText.setText("Error, pattern file not found! It was either renamed, moved or deleted");
            }

        } catch(IOException e) {
            e.printStackTrace();
            welcomeText.setText("Error, source file not found! It was either renamed, moved or deleted");
        }


        System.out.println("Pattern: " + Arrays.toString(patList));
        System.out.println("Sources: " + Arrays.toString(srcList));
        System.out.println();

        //Main matching algorithm:
        if (patList != null) for (String pattern : patList) {
            pattern = pattern.trim();
            System.out.println("\nChecking with new pattern : \n" + pattern + " size: " + pattern.length());
            for (String source : srcList) {
                source = source.trim();
                System.out.println("\nwith source: \n" + source);

                //checking if a pattern repeated in the source again
                if (!plagList.contains(pattern)) {

                    //adding length of source to total
                    if (!sourceLooped) totalCharCount += source.length();

                    //break if source sentences is smaller than pattern
                    System.out.print("\nIs pattern larger than source? ");
                    if (pattern.length() <= source.length()) {
                        System.out.print(true);

                        //checking hash values
                        System.out.print("\nAre hash codes equal? ");
                        if (source.hashCode() == pattern.hashCode()) {
                            System.out.print(true);

                            //checking actual content of sentences.
                            System.out.print("\nAre strings equal? ");
                            if (source.equals(pattern)) {
                                System.out.print(true);

                                //code reaches this point if it actually is an EXACT match.
                                System.out.println("\nPlagiarism found, adding count");
                                plagiarized += source.length();
                                plagList.add(source);

                            } else System.out.print(false);
                        } else System.out.print(false);
                    } else System.out.print(false);

                }
            }

            sourceLooped = true;

        }


//        System.out.println("\nComparing '" + tempPattern + "' \nwith \n'" + line + "'");
//        tempPattern = tempPattern.trim();
//
//        //adding length of source to total
//        if (!sourceFinished) totalCharCount += tempSource.length();
//
//        //break if source sentences is smaller than pattern
//        System.out.print("\nis pattern larger than source? ");
//        if (tempPattern.length() <= tempSource.length()) {
//            System.out.print(true);
//
//            //checking hash values
//            System.out.print("\nAre hash codes equal? ");
//            if (tempSource.hashCode() == tempPattern.hashCode()) {
//                System.out.print(true);
//
//                //checking actual content of sentences.
//                System.out.print("\nAre strings equal? ");
//                if (tempSource.equals(tempPattern)) {
//                    System.out.print(true);
//
//                    //code reaches this point if it actually is an EXACT match.
//                    System.out.println("\nPlagiarism found, adding count");
//                    plagiarized += tempSource.length();
//
//                } else System.out.print(false);
//            } else System.out.print(false);
//        } else System.out.print(false);

        float percentage = (float) plagiarized / totalCharCount * 100;

        if (percentage > 100.0) percentage = 100;

        welcomeText.setText("Plagiarized percentage is " + percentage);
        System.out.println("\nPlagiarized percentage is " + percentage);
        System.out.println("Total source count is " + totalCharCount);
        System.out.println("Plagiarized count is " + plagiarized);

        checkBtn.setText(percentage + "%");
        checkBtn.arm();

    }

}
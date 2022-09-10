package dsa.group.one.plagiarismchecker;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import io.github.palexdev.materialfx.controls.MFXTextField;

public class DashBoardController {
    //The controller class handles the interaction between the fxml and java classes.
    @FXML public AnchorPane parentWindow;
    @FXML public Button browseSource;
    @FXML public Button browsePattern;
    @FXML private Label welcomeText;
    @FXML private TextFlow sourceTxt;
    @FXML private TextFlow patternTxt;
    @FXML private Button checkBtn;
    @FXML private Gauge gauge;


    @FXML private Label sourceFileName;
    @FXML private Label patternFileName;

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
            sourceFileName.setText("Loaded file: '" + selectedFile.getName() + "'");

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
            patternFileName.setText("Loaded file: '" + selectedFile.getName() + "'");
        }


    }

    protected void previewFile(File selectedFile, TextFlow target) {

//        chart.setData();

        target.getChildren().clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            String text;
            while ((text = reader.readLine()) != null) {
                System.out.println(text);

                Text convertedTxt = new Text (text + " ");
                target.getChildren().add(convertedTxt);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        gauge.setValue(0);

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

        checkBtn.disarm();

        String[] patList = null;
        String[] srcList = null;
        List<String> plagList = new ArrayList<>();

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

        ArrayList<Integer> patFlags = new ArrayList<>();
        ArrayList<Integer> srcFlags = new ArrayList<>();

        Integer patIndex = 0;

        //Main matching algorithm:
        if (patList != null) for (String pattern : patList) {

            Integer sourceIndex = 0;

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
                                patFlags.add(patIndex);
                                srcFlags.add(sourceIndex);

                            } else System.out.print(false);
                        } else System.out.print(false);
                    } else System.out.print(false);

                }

                sourceIndex++;
            }

            sourceLooped = true;
            patIndex++;
        }

        //readding the text in the textflow with highlighting
        patternTxt.getChildren().clear();


        for(int i = 0; i < patList.length; i++) {

            Text temp = new Text (patList[i].trim() +". ");
            if (srcFlags.contains(i)) {
                temp.setFill(Color.RED);
                temp.setFont(Font.font("Calibri", FontWeight.BOLD,12));
            }
            patternTxt.getChildren().add(temp);


        }
        sourceTxt.getChildren().clear();

        for(int i = 0; i < srcList.length; i++) {

            Text temp = new Text (srcList[i].trim() +". ");
            if (srcFlags.contains(i)) {
                temp.setFill(Color.RED);
                temp.setFont(Font.font("Calibri", FontWeight.BOLD,12));
            }
            sourceTxt.getChildren().add(temp);



        }

        float percentage = (float) plagiarized / totalCharCount * 100;

        if (percentage > 100.0) percentage = 100;

        if (Double.isNaN(percentage)) percentage = 0;

        Color init = gauge.getBarColor();

        gauge.setValue(percentage);
        double hueVal = 100 - percentage;
        gauge.setBarColor(Color.hsb(hueVal,init.getSaturation(),init.getBrightness()));


        welcomeText.setText("Debug: " + plagiarized + " / " + totalCharCount + " = " + percentage);
        System.out.println("\nPlagiarized percentage is " + percentage);
        System.out.println("Total source count is " + totalCharCount);
        System.out.println("Plagiarized count is " + plagiarized);

        checkBtn.arm();

    }

    @FXML
    protected void transitionGauge() {


        gauge.setAnimationDuration(150);

        double rand = Math.random() * (100-0 + 1) + 0;

        gauge.setValue(rand);

        double hueVal = 100-rand;





    }
}
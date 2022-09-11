package dsa.group.one.plagiarismchecker;

import eu.hansolo.medusa.Gauge;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
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
import java.util.*;
import java.util.stream.Collectors;

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
    protected void browseSourceOnClick() {

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
    protected void browsePatternOnClick() {

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
    protected void plagiarizeCheck() {

        int plagiarized = 0;
        int totalCharCount = 0;

        //null check
        if (src == null || pat == null) {
            welcomeText.setText("Error, please select a pattern and source file!");
            return;
        }

        checkBtn.disarm();
        checkBtn.setDisable(true);

        String[] patList = null;
        String[] srcList = null;

        List<String> plagList = new ArrayList<>();

        boolean sourceLooped = false;
        boolean deepCheck = false;

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

        int patIndex = 0;
        int deepCheckCount = 0;

        //Main matching algorithm:
        if (patList != null && !deepCheck) for (String pattern : patList) {

            int sourceIndex = 0;

            pattern = pattern.trim();
            System.out.println("\nChecking with new pattern : \n" + pattern + " size: " + pattern.length());
            for (String source : srcList) {

                source = source.trim();
                System.out.println("\nwith source: \n" + source);

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


                sourceIndex++;
            }

            sourceLooped = true;
            patIndex++;
        }

        ArrayList<Text> textArrayUsingSourceWords = new ArrayList<>();
        ArrayList<Text> textArrayUsingPatternWords = new ArrayList<>();

        Map<Integer,Integer> patOffsetStart = new LinkedHashMap<>();
        Map<Integer,Integer> patOffsetEnd = new LinkedHashMap<>();

        Map<Integer,Integer> srcOffsetStart = new LinkedHashMap<>();
        Map<Integer,Integer> srcOffsetEnd = new LinkedHashMap<>();


        //patList and srcList both have each sentences separated by the delimiter '.'
        //for deep check if 5 or more words match consecutively, then count as plagiarism

        //the var pattern has a sentence,
        //1. hash the first 5 words
        //2. check with source 5 words
        //3. if they check out, then mark them as plagiarism

        if (patList != null && deepCheck) for (String pattern : patList) {

            int sourceIndex = 0;

            pattern = pattern.trim();
            System.out.println("\nChecking with new pattern : \n" + pattern + " size: " + pattern.length());
            for (String source : srcList) {

                String[] choppedPattern = pattern.split(" ");
                String[] choppedSource = source.split(" ");

                if (choppedPattern.length < 5 || choppedSource.length < 5) continue;    //goes to next iteration

                //calculating hash codes
//                for (int i = patIndex; i < 5; i++) patSum += (choppedPattern[i].trim().toLowerCase()).hashCode();
//                for (int i = sourceIndex; i < 5; i++) srcSum += (choppedSource[i].trim().toLowerCase()).hashCode();

                //adding length of source to total
                if (!sourceLooped) totalCharCount += source.length();

                //checking each word
                for (int i = 0; i < choppedPattern.length; i++) {

                    int hitPerSentence = 0;
                    int tempCounter = 0;

                    for (int j = 0; j < choppedSource.length; j++) {

                        if (i+j >= choppedPattern.length) {
                            System.out.println("Pattern reached end of sentence, Source is bigger than pattern");
                            //TODO: Make it check after a full match from the start again.
                            break;
                        }

                        if (!Objects.equals(choppedPattern[i+j].trim().toLowerCase(), choppedSource[j].trim().toLowerCase())) {
                            if (tempCounter >= 5) hitPerSentence++;  //iterating hitPerSentence if there were previous matches
                            tempCounter = 0;    //resetting word count in case there's a mismatch in between words
                        } else tempCounter++;

                        if (tempCounter >= 5) {

                            srcOffsetStart.put(hitPerSentence, j-tempCounter);
                            srcOffsetEnd.put(hitPerSentence, j);

                            patOffsetStart.put(hitPerSentence, i);
                            patOffsetEnd.put(hitPerSentence, i+tempCounter);


                        }

                    }

                    for (int index = 0; index <= hitPerSentence; index++) {

                        for (int jojo = 0; jojo < choppedSource.length; jojo++) {

                            String delimiter= " ";

                            if (jojo == choppedSource.length-1) delimiter = ".";

                            //checking if the index was caught as plagiarism
                            if (jojo == srcOffsetStart.get(i) && jojo <= srcOffsetEnd.get(i)) {

                                //adding to plagiarism counter
                                plagiarized += choppedSource[jojo].length();

                                //add as red text
                                Text tempFlow = new Text(choppedSource[jojo] + delimiter);
                                tempFlow.setFill(Color.RED);
                                tempFlow.setFont(Font.font("Calibri", FontWeight.BOLD,12));
                                textArrayUsingSourceWords.add(tempFlow);

                            } else {
                                //add as regular text
                                Text tempFlow = new Text(choppedSource[jojo] + delimiter);
                                textArrayUsingSourceWords.add(tempFlow);
                            }

                        }

                        for (int dio = 0; dio < choppedPattern.length; dio++) {

                            String delimiter= " ";

                            if (dio == choppedPattern.length-1) delimiter = ". ";

                            //checking if the index was caught as plagiarism
                            if (dio == patOffsetStart.get(i) && dio <= patOffsetEnd.get(i)) {

                                //adding to plagiarism counter
                                plagiarized += choppedPattern[dio].length();

                                //add as red text
                                Text tempFlow = new Text(choppedPattern[dio] + delimiter);
                                tempFlow.setFill(Color.RED);
                                tempFlow.setFont(Font.font("Calibri", FontWeight.BOLD,12));
                                textArrayUsingPatternWords.add(tempFlow);

                            } else {
                                //add as regular text
                                Text tempFlow = new Text(choppedPattern[dio] + delimiter);
                                textArrayUsingPatternWords.add(tempFlow);
                            }
                        }
                    }
                }

                sourceIndex++;
            }

            sourceLooped = true;
            patIndex++;
        }


        patternTxt.getChildren().clear();
        sourceTxt.getChildren().clear();


        //reading the text in the text flow with highlighting
        if (!deepCheck) {
            if (patList != null) {
                for(int i = 0; i < patList.length; i++) {

                    Text temp = new Text (patList[i].trim() +". ");
                    if (patFlags.contains(i)) {
                        temp.setFill(Color.RED);
                        temp.setFont(Font.font("Calibri", FontWeight.BOLD,12));
                    }
                    patternTxt.getChildren().add(temp);

                }
            }

            if (srcList != null) {
                for(int i = 0; i < srcList.length; i++) {

                    Text temp = new Text (srcList[i].trim() +". ");
                    if (srcFlags.contains(i)) {
                        temp.setFill(Color.RED);
                        temp.setFont(Font.font("Calibri", FontWeight.BOLD,12));
                    }
                    sourceTxt.getChildren().add(temp);

                }
            }

        } else {

            for (Text t : textArrayUsingPatternWords) {
                patternTxt.getChildren().addAll(t);
            }
            for (Text t : textArrayUsingSourceWords) {
                sourceTxt.getChildren().addAll(t);
            }
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
        checkBtn.setDisable(false);

    }

}
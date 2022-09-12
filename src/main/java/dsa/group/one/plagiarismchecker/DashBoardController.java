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
import java.net.URI;
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

    @FXML private Label Title;

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

        ArrayList<plagSentence> plaggedPattern = new ArrayList<>();
        ArrayList<plagSentence> plaggedSource = new ArrayList<>();


        //patList and srcList both have each sentences separated by the delimiter '.'
        //for deep check if 5 or more words match consecutively, then count as plagiarism

        int deepCheckSensitivity = 4;


        if (patList != null && deepCheck) for (String pattern : patList) {

            int patWordIndex = 0;

            String[] choppedPattern = pattern.split(" ");
            if (choppedPattern.length < deepCheckSensitivity + 1) continue;    //skip to next pattern if sentence is too small.

            while (patWordIndex < choppedPattern.length) {

                boolean patternEnd = false;

                int patSum = 0;

                if (patWordIndex + deepCheckSensitivity > choppedPattern.length) break;    //meaning pattern doesn't any more words so no need to check, move to next pattern

                System.out.println("\nComparing : ");
                System.out.println("-------------------------");
                for (int i = patWordIndex; i < patWordIndex + deepCheckSensitivity; i++) {
                    patSum += (choppedPattern[i].trim().toLowerCase()).hashCode();
                    System.out.print(choppedPattern[i] + " ");
                }

                for (String source : srcList) {

                    int srcWordIndex = 0;
                    String[] choppedSource = source.split(" ");

                    if (choppedSource.length < deepCheckSensitivity + 1) continue;    //goes to next iteration since this sentence is too small
                    while (srcWordIndex < choppedSource.length) {

                        boolean sourceEnd = false;
                        int srcSum = 0;

                        if (srcWordIndex + deepCheckSensitivity + 1 > choppedSource.length) break;    //reached/exceed the length of the array

                        System.out.println("\n-------------------------");
                        //calculating hash codes
                        for (int i = srcWordIndex; i < srcWordIndex + deepCheckSensitivity; i++) {
                            srcSum += (choppedSource[i].trim().toLowerCase()).hashCode();
                            System.out.print(choppedSource[i] + " ");
                        }
                        System.out.println();

                        if (patSum == srcSum) {
                            //meaning there was a match of 5 words.
                            //now checking how many more words might have matched afterwards
                            int offset = 0;

                            int localPatSum = 0;
                            int localSrcSum = 0;


                            while (localPatSum == localSrcSum) {

                                offset++;

                                if (patWordIndex + offset + deepCheckSensitivity > choppedPattern.length) {
                                    offset = choppedPattern.length - 1 - patWordIndex;
                                    patternEnd = true;
                                    break;
                                }
                                if (srcWordIndex + offset + deepCheckSensitivity > choppedSource.length) {
                                    sourceEnd = true;
                                    break;
                                }

                                for (int i = patWordIndex; i < patWordIndex + offset + deepCheckSensitivity; i++)
                                    localPatSum += (choppedPattern[i].trim().toLowerCase()).hashCode();
                                for (int i = srcWordIndex; i < srcWordIndex + offset + deepCheckSensitivity; i++)
                                    localSrcSum += (choppedSource[i].trim().toLowerCase()).hashCode();


                            }

                            //checking actual strings to make sure they actually match
                            for (int i = offset; i > 0; i--) {

                                //checking string content to make sure they actually match from the end of the string.
                                if (!choppedPattern[patWordIndex + i].equalsIgnoreCase(choppedSource[srcWordIndex + i])) {
                                    //if a mismatch was found at the end, decrease offset.
                                    offset--;
                                }

                                if ((offset + deepCheckSensitivity) < deepCheckSensitivity) break;  //that means only 4 words matched, which won't be counted

                            }

                            //store the starting index of both files with their offset for highlighting later
                            if ((offset + deepCheckSensitivity) >= deepCheckSensitivity) {

                                plagSentence temp = new plagSentence();
                                temp.offset = offset;
                                temp.startIndex = patWordIndex;
                                temp.sentence = pattern;

                                plagSentence temp2 = new plagSentence();
                                temp2.offset = offset;
                                temp2.startIndex = srcWordIndex;
                                temp2.sentence = source;

                                plaggedPattern.add(temp);
                                plaggedSource.add(temp2);

                                srcWordIndex += offset;

                            }
                            //breaks after the first mismatch

                        }

                        //source is shorter than pattern but there's at least 5 matches
                        //can't compare source to this pattern anymore, move to next pattern
                        //or if pattern ends then there's no need to compare it to this source anymore.
                        if (sourceEnd || patternEnd) break;
                        else srcWordIndex++;

                    }

                    if (patternEnd) break;

                }
                if (patternEnd) break;
                patWordIndex++;
            }
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

            System.out.println("plagged patterns: " + plaggedPattern.size());

            for (plagSentence item : plaggedPattern) {
                System.out.println("\nSentence index = " + item.sentence);
                System.out.println("Word index = " + item.startIndex);
                System.out.println("Offset = " + item.offset);

                for (String sentence : patList) {

                    if (sentence.equalsIgnoreCase(item.sentence)) {
                        //this sentence was plagiarised, outputting as red

                        String[] choppedSentences = sentence.split(" ");

                        for (int i = 0; i < choppedSentences.length; i++) {

                            Text temp = new Text();

                            if (i == choppedSentences.length-1) temp.setText(choppedSentences[i] + ". ");
                            else temp.setText(choppedSentences[i] + " ");

                            if (i >= item.startIndex && i <= (item.startIndex + item.offset)) {
                                plagiarized += choppedSentences[i].length();
                                temp.setFill(Color.RED);
                                temp.setFont(Font.font("Calibri", FontWeight.BOLD,12));
                            }
                            patternTxt.getChildren().add(temp);
                        }

                    } else {
                        //output as regular text
                        patternTxt.getChildren().add(new Text(sentence + "."));
                    }

                }

            }
            System.out.println("-----------------------------------");
            System.out.println("\nPlagged sources: " + plaggedSource.size());
            for (plagSentence item : plaggedSource) {
                System.out.println("Sentence index = " + item.sentence);
                System.out.println("Word index = " + item.startIndex);
                System.out.println("Offset = " + item.offset);

                for (String sentence : srcList) {

                    if (sentence.equalsIgnoreCase(item.sentence)) {
                        //this sentence was plagiarised, outputting as red
                        String[] choppedSentences = sentence.split(" ");
                        for (int i = 0; i < choppedSentences.length; i++) {

                            totalCharCount += choppedSentences[i].length();

                            Text temp = new Text();

                            if (i == choppedSentences.length-1) temp.setText(choppedSentences[i] + ". ");
                            else temp.setText(choppedSentences[i] + " ");

                            if (i >= item.startIndex && i <= (item.startIndex + item.offset)) {
                                temp.setFill(Color.RED);
                                temp.setFont(Font.font("Calibri", FontWeight.BOLD,12));
                            }

                            sourceTxt.getChildren().add(temp);

                        }
                    } else {
                        //output as regular text
                        patternTxt.getChildren().add(new Text(sentence + "."));
                    }

                }
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

    public class plagSentence {

        String sentence = null;
        int startIndex = 0;
        int offset = 0;

    }

    @FXML
    protected void  openGit() {

        try {

            URI uri= new URI("https://github.com/TeeJayOhFour/PlagiarismChecker");
            java.awt.Desktop.getDesktop().browse(uri);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

}
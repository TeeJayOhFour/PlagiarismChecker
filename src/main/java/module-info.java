module dsa.group.one.plagiarismchecker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires MaterialFX;

    opens dsa.group.one.plagiarismchecker to javafx.fxml;
    exports dsa.group.one.plagiarismchecker;
}
module dsa.group.one.plagiarismchecker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires MaterialFX;
    requires eu.hansolo.medusa;
    requires eu.hansolo.toolbox;
    requires eu.hansolo.toolboxfx;
    requires javafx.swingEmpty;

    opens dsa.group.one.plagiarismchecker to javafx.fxml;
    exports dsa.group.one.plagiarismchecker;
}
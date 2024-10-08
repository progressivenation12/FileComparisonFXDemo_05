package com.application;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ControllerErrorWindow {
    public Button buttonClose;
    public AnchorPane errorWindow;

    public void closeErrorWindow(ActionEvent actionEvent) {
        Node node = (Node) actionEvent.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }
}

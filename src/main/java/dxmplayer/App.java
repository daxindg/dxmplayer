package dxmplayer;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;



public class App extends Application{
    static Stage mainStage;
    @Override
    public void start(Stage stage) {
        mainStage = stage;

        stage.setTitle("");

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
        Layout root = new Layout();
        Scene scene = new Scene(root, 1040, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        scene.setFill(Color.web("#222222"));
        ResizeHelper.addResizeListener(stage);

    }
    public static void main (String[] args) {
        launch();
    }
}

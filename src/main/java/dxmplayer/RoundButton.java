package dxmplayer;

import dxmplayer.icons.*;

import javafx.scene.control.Button;
import javafx.scene.layout.Border;

public class RoundButton extends Button{
    RoundButton(String content) {
        getStyleClass().add("btn");
        setBorder(Border.EMPTY);
        setText(content);
        setOnMousePressed(e -> setStyle("-fx-background-color: #142434"));
        setOnMouseReleased(e -> setStyle(null));
    }

}

class PlayButton extends RoundButton {
    PlayButton() {
        super("");
        setGraphic(new PlayIcon());
        setPickOnBounds(true);
        setOnMouseClicked(e -> Player.changePlayState());
    }
}

class NextButton extends Button {
    NextButton() {
        super("");
        setGraphic(new NextIcon());
        setPickOnBounds(true);
        setStyle("-fx-border-style: none; -fx-background-color: transparent");
        setOnMouseClicked(e -> Player.next());
    }
}

class PrevButton extends Button {
    PrevButton() {
        super("");
        setGraphic(new PrevIcon());
        setPickOnBounds(true);
        setStyle("-fx-border-style: none; -fx-background-color: transparent");
        setOnMouseClicked(e -> Player.prev());
    }
}

class MuteButton extends Button {
    MuteButton() {
        setGraphic(new AudioIcon());
        setPickOnBounds(true);
        setStyle("-fx-border-style: none; -fx-background-color: transparent");
    }
}



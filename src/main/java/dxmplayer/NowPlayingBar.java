package dxmplayer;

import dxmplayer.icons.*;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class NowPlayingBar extends HBox{
    static Button play = new PlayButton();
    static Button next = new NextButton();
    static Button prev = new PrevButton();
    static Button repeat = new Button();
    static Button shuffle = new Button();


    static PlaybackBar playbackBar = new PlaybackBar();
    static Label time = new Label("00:00");
    static Label totalTime = new Label("00:00");

    static VolumeBar volumeBar = new VolumeBar();

    static Left left = new Left();
    static Center center= new Center();
    static Right right = new Right();


    NowPlayingBar() {
        getStyleClass().add("now-playing-bar");
        Arrays.asList(left, center, right).forEach(e -> setHgrow(e, Priority.ALWAYS));
        getChildren().addAll(left, center, right);

        for (var x : Arrays.asList(left, center, right)) {
            x.prefWidthProperty().bind(widthProperty().divide(3));
        }
    }

    static ChangeListener<Number> nowPlayingChangeListener = (ob, old, val) -> {
        if (!Player.getPlaylist().isEmpty()) {
            left.title.setText((String)Player.getPlaylist().get(val.intValue()).meta.getOrDefault("title", ""));
            left.artist.setText((String)Player.getPlaylist().get(val.intValue()).meta.getOrDefault("artist", ""));
            Layout.leftPane.imageView.setImage((Image)Player.getPlaylist().get(val.intValue()).meta.get("image"));
        }
    };
    static void addListener() {
        Player.now.addListener(nowPlayingChangeListener);
    }
    static void removeListener() {
        Player.now.removeListener(nowPlayingChangeListener);
    }

    private static class Left extends VBox {

        Label title = new Label("");
        Label artist = new Label("");



        Left() {
            getChildren().addAll(title, artist);
            title.setStyle("-fx-font-size: 14px;-fx-text-fill: #fff");
            artist.setStyle("-fx-font-size: 12;-fx-text-fill: #b3b3b3");

            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(0, 0,0 ,20));

        }


    }

    private static class Right extends HBox {
        Button lrcToggle = new Button();
        Right() {

            lrcToggle.setGraphic(new LrcIcon());
            lrcToggle.setOnMouseClicked(e -> {
                lrcToggle.getStyleClass().clear();
                if (Layout.rightPane.showing instanceof LyricsView) {
                    Layout.rightPane.setupPlaylist();
                    lrcToggle.getStyleClass().addAll("button");
                }
                else {
                    Layout.rightPane.setupLyricsView();
                    lrcToggle.getStyleClass().addAll("button", "active");

                }
            });

            getChildren().addAll(lrcToggle, volumeBar);
            setAlignment(Pos.CENTER_RIGHT);
            setPadding(new Insets(0, 10, 0, 0));
        }
    }

    private static class Center extends VBox {
        Center() {

            shuffle.setGraphic(new ShuffleIcon());
            shuffle.setOnMouseClicked(e -> {
                Player.shuffle ^= true;
                shuffle.getStyleClass().clear();

                if (Player.shuffle) {
                    shuffle.getStyleClass().addAll("active", "button");
                }
                else {
                    shuffle.getStyleClass().addAll("button");
                }
            });

            repeat.setGraphic(new RepeatIcon());
            repeat.getStyleClass().add("active");
            repeat.setOnMouseClicked(e -> {
                Player.repeat += 1;
                Player.repeat %= 3;
                repeat.getStyleClass().clear();
                if (Player.repeat == 1) {
                    repeat.getStyleClass().addAll("button", "active");
                }
                else if (Player.repeat == 2) {
                    repeat.getStyleClass().addAll("button", "active");
                    repeat.setGraphic(new RepeatOnceIcon());
                }
                else {
                    repeat.getStyleClass().addAll("button");
                    repeat.setGraphic(new RepeatIcon());
                }
            });

            prev.setDisable(true);
            next.setDisable(true);


            var hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            hbox.getChildren().addAll(shuffle, prev, play, next, repeat);
            hbox.setSpacing(30);


            setStyle("-fx-min-width: 400px");
            var playbackBarContainer = new HBox(time, playbackBar, totalTime);
            getChildren().addAll(hbox, playbackBarContainer);
            HBox.setHgrow(playbackBar, Priority.ALWAYS);
            playbackBarContainer.setAlignment(Pos.CENTER);
            playbackBarContainer.setSpacing(10);


            time.setStyle("-fx-text-fill: #9b9b9b");
            totalTime.setStyle("-fx-text-fill: #9b9b9b");
            setVgrow(hbox, Priority.ALWAYS);
            setPadding(new Insets(0, 0, 10, 0));
        }
    }
}


class ColorSlider extends Slider {
    String trackColor;
    String backgroundColor;
    ColorSlider(String trackColor, String backgroundColor, double min, double max, double value) {
        super(min, max, value);
        this.trackColor = trackColor;
        this.backgroundColor = backgroundColor;
        getStyleClass().add("color-slider");
        setOnMouseEntered(e -> {
            lookup(".thumb").setStyle("-fx-background-color: " + trackColor);
            this.trackColor = "#1db954";
            seek((int)(getValue() * 100 / getMax()));
        });
        setOnMouseExited(e -> {
            lookup(".thumb").setStyle("-fx-scale-y: 0.5;-fx-translate-y: 0.5px;-fx-translate-x: -3px; -fx-background-color: " + trackColor);
            this.trackColor = trackColor;
            seek((int)(getValue() * 100 / getMax()));
        });

        valueProperty().addListener((ov, old, val) -> {
            int x = (int)(val.doubleValue() * 100 / getMax());
            seek(x);
        });
    }

    void seek(int x) {
        String style = String.format("-fx-background-color: linear-gradient(to right,"+ this.trackColor + "," + this.trackColor +" %d%%, " + this.backgroundColor + " %d%%);",
                x , x);
        var track = getTrack();

        if (track != null) {
            track.setStyle(style);
        }
        else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    getTrack().setStyle(style);
                }
            }, 1000);
        }
    }

    Node getTrack() {
        return lookup(".track");
    }
}

class PlaybackBar extends ColorSlider {
    ChangeListener<Duration> currentTimeChangeListener = (ob, old, val) -> {
        NowPlayingBar.time.setText(String.format("%02d:%02d", (int)val.toMinutes(), ((int)val.toSeconds()) % 60));
        removeSliderValueListener();
        setValue(val.toMillis());
        addSliderValueListener();
    };
    ChangeListener<Number> sliderValueChangeListener = (ob, old, val) -> {
        removeCurrentTimeListener();
        Player.setCurrentTime(getValue());
        addCurrentTimeListener();
    };

    PlaybackBar() {
        super("#b3b3b3", "#535353", 0, 1.0, 0);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                lookup(".thumb").setStyle("-fx-scale-y: 0.5;-fx-translate-y: 0.5px;-fx-translate-x: -3px; -fx-background-color: #b3b3b3");
            }
        }, 1000);

    }

    void addAllListener() {
        addCurrentTimeListener();
        addSliderValueListener();
    }
    void removeAllListener() {
        removeCurrentTimeListener();
        removeSliderValueListener();
    }
    void addCurrentTimeListener() {
        Player.mediaPlayer.currentTimeProperty().addListener(currentTimeChangeListener);
    }

    void removeCurrentTimeListener() {
        Player.mediaPlayer.currentTimeProperty().removeListener(currentTimeChangeListener);
    }
    void addSliderValueListener() {
        valueProperty().addListener(sliderValueChangeListener);
    }
    void removeSliderValueListener() {
        valueProperty().removeListener(sliderValueChangeListener);
    }
}

class VolumeBar extends HBox {
    Button mute = new MuteButton();
    ColorSlider slider = new ColorSlider("#b3b3b3", "#535353", 0, 1.0, 0);
    VolumeBar() {
        getChildren().addAll(mute, slider);
        setAlignment(Pos.CENTER_RIGHT);
        slider.setValue(0.8);
        getStyleClass().add("volume-bar");
        slider.valueProperty().addListener((ov, old, val) -> Player.setVolume(val.doubleValue()));
        mute.setOnMouseClicked(e -> {
            if (Player.mediaPlayer != null) {
                if (Player.mediaPlayer.isMute()) {
                    Player.mediaPlayer.setMute(false);
                    mute.setGraphic(new AudioIcon());
                }
                else {
                    Player.mediaPlayer.setMute(true);
                    mute.setGraphic(new MuteIcon());
                }
            }
        });
//        mute.setOnMouseEntered(e -> {
//            mute.lookup(".icon").setStyle("-fx-background-color: #ffffff");
//        });
//        mute.setOnMouseExited(e -> mute.lookup(".icon").setStyle(""));

    }
}
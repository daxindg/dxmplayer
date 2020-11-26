package dxmplayer;


import animatefx.util.ParallelAnimationFX;
import dxmplayer.icons.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import animatefx.animation.*;

public class Layout extends BorderPane {
    static LeftPane leftPane;
    static RightPane rightPane;
    static MainView mainView ;
    static NowPlayingBar nowPlayingBar;
    static WindowManageBar windowManageBar;
    Layout() {
        leftPane = new LeftPane();
        rightPane = new RightPane();
        nowPlayingBar = new NowPlayingBar();
        windowManageBar = new WindowManageBar();

        mainView = new MainView();

        setTop(windowManageBar);
        setCenter(mainView);
        setBottom(nowPlayingBar);
        getStyleClass().add("root");
    }

    static class MainView extends SplitPane {
        MainView() {
            setStyle("-fx-background-color: transparent");
            getItems().addAll(leftPane, rightPane);
        }
    }
}

class WindowManageBar extends HBox {
    Button maximize = new Button();
    Button minimize = new Button();
    Button close = new Button();
    Double initialX;
    Double initialY;
    {

        maximize.setGraphic(new MaximizeIcon());
        minimize.setGraphic(new MinimizeIcon());
        close.setGraphic(new CloseIcon());

        maximize.setOnMouseClicked(e -> {
            if (App.mainStage.isMaximized()) {
                App.mainStage.setMaximized(false);
                maximize.setGraphic(new MaximizeIcon());
            }else {
                App.mainStage.hide();
                App.mainStage.setMaximized(true);
                App.mainStage.show();
                maximize.setGraphic(new RestoreIcon());
            }
        });
        minimize.setOnMouseClicked(e -> App.mainStage.setIconified(true));
        close.setOnMouseClicked(e -> App.mainStage.close());

        setOnMousePressed(me->{
                if (me.getButton() != MouseButton.MIDDLE) {
                    initialX = me.getSceneX();
                    initialY = me.getSceneY();
                }
        });

        setOnMouseDragged(me->{
                if (me.getButton() != MouseButton.MIDDLE) {
                    getScene().getWindow().setX(me.getScreenX() - initialX);
                    getScene().getWindow().setY(me.getScreenY() - initialY);
                }
        });
        setAlignment(Pos.CENTER_RIGHT);
        getChildren().addAll(minimize, maximize, close);
        setStyle("-fx-background-color: #141414");
    }
}

class LeftPane extends VBox {
    Button logo = new Button();
    PlaylistListWrapper playlistList = new PlaylistListWrapper();
    ImageView imageView = new ImageView();
    StackPane nowCover = new StackPane();
    LeftPane() {
        getStyleClass().add("left-pane");

        logo.setStyle("-fx-background-color: transparent; -fx-border-style: none");
        logo.setGraphic(new Logo());
        imageView.setPreserveRatio(true);
        imageView.fitHeightProperty().bind(widthProperty().subtract(5));


        nowCover.getChildren().addAll(imageView);
        getChildren().addAll(logo, playlistList, nowCover);
        VBox.setVgrow(playlistList, Priority.ALWAYS);
        setAlignment(Pos.TOP_CENTER);
        setSpacing(24);
        setPadding(new Insets(20,0,0,0));
    }
}

class RightPane extends StackPane {
    VBox playlistView = new VBox();
    PlayListWrapper playlist = new PlayListWrapper();

    LyricsView lyricsView = new LyricsView();
    HBox header = new HBox();
    Label playlistName = new Label("");

    Node showing = playlistView;

    RightPane() {

        playlistName.setStyle("-fx-padding: 0 0 7 0;-fx-font-size: 27px; -fx-text-fill: #adadad;-fx-border-color: #2b2b2b; -fx-border-style: hidden hidden solid hidden; -fx-border-width: 2px ; -fx-font-weight: 800");

        header.getChildren().add(playlistName);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 0, 20));
        header.setStyle("-fx-min-height: 140px;-fx-max-height: 140px");

        setupPlaylist();
        playlistView.getStyleClass().add("right-pane");
        playlistView.setAlignment(Pos.CENTER);
        playlistView.getChildren().addAll(header, playlist);
        VBox.setVgrow(playlist, Priority.ALWAYS);

        getStyleClass().add("right-pane");
        setAlignment(Pos.CENTER);
        getChildren().addAll(lyricsView, playlistView);
    }

    void setupPlaylist() {
        var out = new SlideOutRight(showing);
        var in  = new SlideInLeft(playlistView);

        playlistView.setVisible(true);
        out.setOnFinished(e -> lyricsView.setVisible(false));

        var seq = new ParallelAnimationFX(out, in);

        seq.play();

        setPadding(new Insets(10, 0, 10, 0));
        showing = playlistView;
    }

    void setupLyricsView() {
        if (Player.mediaPlayer == null) {
            return;
        }

        LyricsView.lyrics.loadPlaying();

        lyricsView.setVisible(true);

        var out = new SlideOutRight(showing);
        var in  = new SlideInLeft(lyricsView);
        out.setOnFinished(e -> playlistView.setVisible(false));
        var seq = new ParallelAnimationFX(out, in);

        seq.play();

        showing = lyricsView;
    }
}
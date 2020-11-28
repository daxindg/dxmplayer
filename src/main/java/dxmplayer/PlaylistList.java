package dxmplayer;


import animatefx.animation.*;
import dxmplayer.icons.AddFolderIcon;
import dxmplayer.icons.AddIcon;
import dxmplayer.icons.SoundIcon;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

import static dxmplayer.App.mainStage;


class PlaylistListView extends VBox {

    static ObservableList<PlaylistList.PlaylistItem> items = FXCollections.observableArrayList(PlaylistList.PlaylistItem.extractor());
    static PlaylistList playlistList = new PlaylistList(items);
    HBox header = new HBox();
    Button btnNewPlayList = new Button();
    static IntegerProperty indexOfSelected = new SimpleIntegerProperty(0);
    Label playlistLabel = new Label("PLAYLISTS");

    {

        playlistLabel.setStyle("-fx-text-fill: #b3b3b3");
        btnNewPlayList.setGraphic(new AddIcon());

        header.setSpacing(27);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(playlistLabel, btnNewPlayList);
        header.setPadding(new Insets(0, 0, 6, 15));

        header.setStyle("-fx-border-style: hidden hidden solid hidden; -fx-border-color: #282828; -fx-border-width: 2px");
        items.add(new PlaylistList.PlaylistItem("Default Playlist"));
        playlistList.getSelectionModel().select(0);


        Player.setup(items.get(0));

        indexOfSelected.bind(playlistList.getSelectionModel().selectedIndexProperty());


        btnNewPlayList.setOnMouseClicked(e -> items.add(new PlaylistList.PlaylistItem("Default Playlist " + items.size())));

        indexOfSelected.addListener((ov, old, val) -> Layout.rightPane.playlist.playlist.setItems(items.get(val.intValue()).playlist));


        getChildren().addAll(header, playlistList);
        setSpacing(6);
    }

    static ObservableList<SongItem> getSelectedPlaylist() {
        return getSelectedItem().playlist;
    }

    static PlaylistList.PlaylistItem getSelectedItem() {
        return items.get(indexOfSelected.get());
    }

    static void appendToPlaylist(List<String> list, ObservableList<SongItem> playlist) {
        if (items.isEmpty()) return;
        var newItems = list.stream().filter(el -> {
            try {
                new Media(el);
                return true;
            }catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());
        for (var uri : newItems) {
            NowPlayingBar.play.setDisable(false);
            var mdp = new MediaPlayer(new Media(uri));

            mdp.setOnReady(() -> {
                var newItem = new SongItem(uri,FXCollections.observableMap(mdp.getMedia().getMetadata()), new Duration(mdp.getTotalDuration().toMillis()));
                playlist.add(newItem);
                mdp.dispose();
            });
        }
    }
}

class PlaylistList extends ListView<PlaylistList.PlaylistItem> {
    PlaylistList(ObservableList<PlaylistItem> list) {
        setCellFactory(x -> new PlaylistListRow());
        setItems(list);
    }
    static class PlaylistItem {
        StringProperty name;
        ObservableList<SongItem>  playlist = FXCollections.observableArrayList(SongItem.extractor());
        BooleanProperty playing = new SimpleBooleanProperty(false);
        PlaylistItem(String name) {
            this.name = new SimpleStringProperty(name);
        }

        boolean isPlaying () {
            return playing.get();
        }

        public static Callback<PlaylistItem, Observable[]> extractor() {
            return param -> new Observable[]{param.name, param.playing};
        }
    }

    static class PlaylistListRow extends ListCell<PlaylistItem> {
        private final HBox container = new HBox();
        private final TextField name = new TextField("Default Playlist");
        private final ChooseFileButton btnChooseFile = new ChooseFileButton();
        private final SoundIcon playingIcon = new SoundIcon();

        MenuItem remove = new MenuItem("Remove");
        ContextMenu contextMenu =  new ContextMenu(remove);

        AnimationFX animation = new Swing(playingIcon);
        PlaylistListRow() {
            setAlignment(Pos.CENTER);
            setStyle("-fx-background-radius: 0;");
            setContextMenu(contextMenu);

            remove.setOnAction(e -> {
                if (Player.playListItem == getItem()) return;
                PlaylistListView.items.remove(getItem());
            });

            playingIcon.setVisible(false);

            container.getChildren().addAll(name, playingIcon, btnChooseFile);
            container.setAlignment(Pos.CENTER_LEFT);
            name.setContextMenu(contextMenu);
            name.setEditable(false);
            name.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1) {
                    PlaylistListView.playlistList.getSelectionModel().select(getItem());
                    Layout.rightPane.playlistName.setText(getItem().name.get());
                }
                if (e.getClickCount() == 2) {
                    name.setEditable(true);
                }
            });
            name.focusedProperty().addListener((ob, old, val) -> {
                if (!val) {
                    if (getItem() == null) return;
                    getItem().name.set(name.getText());
                    name.setEditable(false);
                }
            });

            HBox.setHgrow(name, Priority.ALWAYS);

            setGraphic(container);
        }

        void playAnimation() {
            playingIcon.setVisible(true);
            animation.setCycleCount(Timeline.INDEFINITE);
            animation.setSpeed(0.4);
            animation.play();
        }

        void stopAnimation() {
            animation.stop();
            playingIcon.setVisible(false);
        }

        @Override
        protected void updateItem(PlaylistItem item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                name.setText(item.name.get());
                btnChooseFile.setOnMouseClicked(e -> PlaylistListView.appendToPlaylist(btnChooseFile.getFiles(), item.playlist));

                if (item.isPlaying() && Player.mediaPlayer != null && Player.mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    playAnimation();
                }

                else stopAnimation();
                Layout.rightPane.playlistName.setText(PlaylistListView.getSelectedItem().name.get());
                setGraphic(container);
            } else setGraphic(null);
        }
    }
}

class ChooseFileButton extends Button {
    {
        setGraphic(new AddFolderIcon());
        setPickOnBounds(true);
        setStyle("-fx-border-style: none");
    }

    List<String> getFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        var files = fileChooser.showOpenMultipleDialog(mainStage);
        if (files == null) return List.of();
        return files.stream().map(el -> el.toURI().toASCIIString()).collect(Collectors.toList());
    }
}
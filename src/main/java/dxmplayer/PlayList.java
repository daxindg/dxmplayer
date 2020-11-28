package dxmplayer;

import dxmplayer.icons.EqualiserAnimated;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.Arrays;


class PlaylistView extends VBox {
    PlayList playlist = new PlayList();
    Label sharp = new Label("#");
    Label title = new Label("TITLE");
    Label album = new Label("ALBUM");
    Label duration = new Label();
    HBox header = new HBox();
    PlaylistView() {
        var durationIcon = new Region();
        durationIcon.getStyleClass().add("duration-icon");
        this.duration.setGraphic(durationIcon);

        getChildren().addAll(header,playlist);

        var sharp = new HBox(this.sharp);
        var title = new HBox(this.title);
        var album = new HBox(this.album);
        var duration = new HBox(this.duration);

        header.getChildren().addAll(sharp, title, album, duration);
        header.setSpacing(70);
        header.getStyleClass().add("playlist-header");

        VBox.setVgrow(playlist, Priority.ALWAYS);
        sharp.setStyle("-fx-max-width: 3em; -fx-min-width: 3em;");
        duration.setStyle("-fx-min-width: 4em; -fx-max-width: 4em;");

        sharp.setAlignment(Pos.CENTER);
        duration.setAlignment(Pos.CENTER_LEFT);

        for (var x : Arrays.asList(title, album)) {
            HBox.setHgrow(x, Priority.ALWAYS);
            x.prefWidthProperty().bind(prefWidthProperty().subtract(112).divide(3));
            x.setAlignment(Pos.CENTER_LEFT);
        }
    }
}

class PlayList extends ListView<SongItem> {

    PlayList() {
        setSelectionModel(new NoSelectionModel<>());
        setCellFactory(x -> new PlayListRow());
        setItems(Player.getPlaylist());
        getStyleClass().add("playlist");
    }

}

class SongItem {
    ObservableMap<String, Object> meta;
    String fileUri;
    Duration totalTime;
    BooleanProperty playing = new SimpleBooleanProperty(false);
    SongItem(String uri, ObservableMap<String, Object> meta, Duration totalTime) {
        this.totalTime = totalTime;
        this.meta = meta;
        this.fileUri = uri;
    }

    int getId() {return  PlaylistListView.getSelectedPlaylist().indexOf(this) + 1;}

    public static Callback<SongItem, Observable[]> extractor() {
        return param -> new Observable[]{param.meta, param.playing};
    }
}

class PlayListRow extends ListCell<SongItem> {
    private final HBox container = new HBox();
    private TitleSection titleSection = new TitleSection(FXCollections.emptyObservableMap());
    private final Label album = new Label("<No Album>");
    private final Label time = new Label("<Unknown>");
    private final Label id = new Label("-1");
    HBox albumBox = new HBox(album);
    HBox timeBox = new HBox(time);
    HBox idBox = new HBox(id);

    MenuItem remove = new MenuItem("Remove");
    ContextMenu contextMenu =  new ContextMenu(remove);

    PlayListRow() {

        idBox.setStyle("-fx-max-width: 2em; -fx-min-width: 2em");
        idBox.setAlignment(Pos.CENTER);
        timeBox.setStyle("-fx-min-width: 3em; -fx-max-width: 3em");
        timeBox.setAlignment(Pos.CENTER);

        container.getChildren().addAll(idBox, titleSection, albumBox, timeBox);
        container.setStyle("-fx-min-height: 60px; -fx-padding: 10px");
        setContextMenu(contextMenu);
        remove.setOnAction(e -> PlaylistListView.getSelectedPlaylist().remove(getItem()));

        for (var x : Arrays.asList(titleSection, albumBox)) {
            HBox.setHgrow(x, Priority.ALWAYS);
            x.prefWidthProperty().bind(prefWidthProperty().subtract(80).divide(3));
            x.setAlignment(Pos.CENTER_LEFT);
        }

        container.setSpacing(10);
        container.setOnMouseClicked(e-> {
            if (e.getClickCount() == 2) {
                if (Player.getPlaylist() != PlaylistListView.getSelectedPlaylist()) {
                    if (Player.getPlayListItem() != null) {
                        Player.getPlayListItem().playing.set(false);
                    }
                    Player.setup(PlaylistListView.getSelectedItem());
                }
                Player.play(Integer.parseInt(id.getText()) - 1);
            }
        });
    }



    @Override
    protected void updateItem(SongItem item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            container.getChildren().removeAll(idBox, titleSection, albumBox, timeBox);
            titleSection = new TitleSection(item.meta);
            HBox.setHgrow(titleSection, Priority.ALWAYS);
            titleSection.prefWidthProperty().bind(prefWidthProperty().subtract(80).divide(3));
            titleSection.setAlignment(Pos.CENTER_LEFT);
            update(item.meta);


            id.setText(item.getId() + "");
            time.setText(String.format("%02d:%02d", (int)item.totalTime.toMinutes(), ((int)item.totalTime.toSeconds()) % 60));
            if (item.playing.get() && !idBox.getChildren().contains(EqualiserAnimated.icon)) {
                idBox.getChildren().remove(id);
                idBox.getChildren().add(EqualiserAnimated.icon);

//                System.out.println("O_o " + id.getText());
            }
            else if (!item.playing.get() && !idBox.getChildren().contains(id)) {
                idBox.getChildren().remove(EqualiserAnimated.icon);
                idBox.getChildren().add(id);

//                System.out.println("T_T " + id.getText());
            }
            if (item.playing.get()) {
                titleSection.title.getStyleClass().clear();
                titleSection.title.getStyleClass().addAll("playing", "label", "title");
            }
            else {
                titleSection.title.getStyleClass().clear();
                titleSection.title.getStyleClass().addAll("label", "title");
            }
            container.getChildren().addAll(idBox, titleSection, albumBox, timeBox);
            setGraphic(container);

        } else setGraphic(null);
    }

    void update(ObservableMap<String, Object> meta) {
        album.setText((String)meta.getOrDefault("album", "<No Album>"));
    }

    static class TitleSection extends HBox {
        ImageView img;
        Label title, artist;
        VBox vbox;
        TitleSection(@org.jetbrains.annotations.NotNull ObservableMap<String, Object> meta) {
            setSpacing(10);
            img = new ImageView();

            img.setPreserveRatio(true);
            img.setFitWidth(50);

            title = new Label((String)meta.getOrDefault("title", "<No Title>"));
            artist = new Label((String) meta.getOrDefault("artist", "<No Artists>"));
            if (meta.containsKey("image")) {
                img.setImage((Image)meta.get("image"));
            }
            title.getStyleClass().add("title");

            vbox = new VBox(title, artist);
            vbox.setAlignment(Pos.CENTER_LEFT);
            vbox.setSpacing(5);
            getChildren().addAll(img, vbox);
        }

    }
}

class NoSelectionModel<T> extends MultipleSelectionModel<T> {

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public void selectIndices(int index, int... indices) {
    }

    @Override
    public void selectAll() {
    }

    @Override
    public void selectFirst() {
    }

    @Override
    public void selectLast() {
    }

    @Override
    public void clearAndSelect(int index) {
    }

    @Override
    public void select(int index) {
    }

    @Override
    public void select(T obj) {
    }

    @Override
    public void clearSelection(int index) {
    }

    @Override
    public void clearSelection() {
    }

    @Override
    public boolean isSelected(int index) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void selectPrevious() {
    }

    @Override
    public void selectNext() {
    }
}


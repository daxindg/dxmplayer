package dxmplayer;


import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class LyricsView extends HBox {
    static Lyrics lyrics = new Lyrics();
    static LyricsList lyricsList = new LyricsList(lyrics.getLyrics());
    static Timer timer = null;
    static ChangeListener<Duration> currentTimeListener = (ov, old, val) -> {
        for (var item : lyrics.getLyrics()) {
            if (val.toMillis() > item.start.toMillis() && val.toMillis() < item.end.toMillis()) {
                if (!item.getPlaying()) {
                    LyricsView.lyricsList.scrollTo(Math.max(lyrics.getLyrics().indexOf(item) - 3, 0));
                    LyricsView.lyricsList.getSelectionModel().select(item);
                    item.setPlaying(true);
                }
            }
            else item.setPlaying(false);
        }
    };
    {
        getChildren().add(lyricsList);
        setAlignment(Pos.CENTER);
        setOnScroll(e -> {
            if (timer != null) timer.cancel();
            removeListener();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    addListener();
                }
            }, 1000);
        });
    }

    static void addListener() {
        if (Player.mediaPlayer == null) return;
        Player.mediaPlayer.currentTimeProperty().addListener(currentTimeListener);
    }

    static void removeListener() {
        if (Player.mediaPlayer == null) return;
        Player.mediaPlayer.currentTimeProperty().removeListener(currentTimeListener);
    }

}

class Lyrics {
    private final ObservableList<LyricLine> lyrics = FXCollections.observableArrayList(LyricLine.extractor());

    void clear() {
        lyrics.clear();
    }
    void load(File file) {
        clear();
        try (Scanner scan = new Scanner(new FileInputStream(file), StandardCharsets.UTF_8)) {
            ArrayList<Pair<Double, String>> tmp = new ArrayList<>();

            while (scan.hasNextLine()) {
                var line = scan.nextLine();
                if (line.length() < 10 || !line.contains("[") || !line.contains("]") || !line.contains(":") || !line.contains("."))
                    continue;

                try {
                    int e = line.indexOf("]");
                    String time = line.substring(1, e);
                    time = time.replace('.', ':');
                    var x = time.split(":");
                    double res = 0;
                    res += Double.parseDouble(x[0]) * 60000;
                    res += Double.parseDouble(x[1]) * 1000;
                    if (e == 9) res += Double.parseDouble(x[2]) * 10;
                    else if (e == 10) res += Double.parseDouble(x[2]);
                    tmp.add(new Pair<>(res, line.substring(e + 1)));
                } catch (Exception ignored) {
                }
            }

            if (!tmp.isEmpty()) {
                var pre = tmp.get(tmp.size() - 1).getKey();
                lyrics.add(new LyricLine(tmp.get(tmp.size() - 1).getValue(), new Duration(pre), new Duration(pre * 2)));
                for (int i = tmp.size() - 2; i >= 0; i--) {
                    var t = tmp.get(i).getKey();
                    lyrics.add(new LyricLine(tmp.get(i).getValue(), new Duration(t), new Duration(pre)));
                    pre = t;
                }
                Collections.reverse(lyrics);
            }
        } catch (FileNotFoundException e) {
            lyrics.addAll(
                    new LyricLine("No lrc file found", new Duration(0), new Duration(0)),
                    new LyricLine("Please put <music filename>.lrc in same directory", new Duration(0), new Duration(0))
            );
        }
    }

    void loadPlaying() {
        if (Player.mediaPlayer == null) return;
        ArrayList<String> x = Arrays.stream(Player.playing.fileUri.split("\\.")).collect(Collectors.toCollection(ArrayList::new));
        x.remove(x.size() - 1);
        x.add(".lrc");
        File file = new File(URI.create(String.join("", x)));
        load(file);
    }

    ObservableList<LyricLine> getLyrics() {
        return lyrics;
    }
}

class LyricsList extends ListView<LyricLine> {
    LyricsList(ObservableList<LyricLine> list) {
        setCellFactory(x -> new LyricsListRow());
        setItems(list);
        getStyleClass().add("lrc-list");
    }


    static class LyricsListRow extends ListCell<LyricLine> {
        private final HBox container = new HBox();
        private final Label content = new Label("");
        LyricsListRow() {
            container.getChildren().add(content);
            container.setAlignment(Pos.CENTER);
            container.setOnMouseClicked(e -> {
                Player.mediaPlayer.seek(getItem().start);
//                getItem().setPlaying(true);
            });

            setAlignment(Pos.CENTER);
            setStyle("-fx-background-radius: 0;");
            setGraphic(container);
        }



        @Override
        protected void updateItem(LyricLine item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                content.setText(item.content.get());
                if (item.getPlaying()) {
                    content.setStyle("-fx-text-fill: #21b054");
                }
                else content.setStyle("");
                setGraphic(container);
            } else setGraphic(null);
        }
    }
}

class LyricLine {
    StringProperty content;
    Duration start, end;
    BooleanProperty playing = new SimpleBooleanProperty(false);
    LyricLine(String content, Duration start, Duration end) {
        this.content = new SimpleStringProperty(content);
        this.start = start;
        this.end = end;
    }
    void setPlaying(boolean val) {
        playing.set(val);
    }

    boolean getPlaying () {
        return playing.get();
    }



    public static Callback<LyricLine, Observable[]> extractor() {
        return param -> new Observable[]{param.content, param.playing};
    }
}


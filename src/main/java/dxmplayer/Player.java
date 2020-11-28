package dxmplayer;


import dxmplayer.icons.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.media.*;
import javafx.util.Duration;

import java.util.Random;

public class Player {
  static Media media;
  static MediaPlayer mediaPlayer;
  static int repeat = 1;
  static boolean shuffle = false;
  static PlaylistList.PlaylistItem playListItem;
  static SongItem playing;
  static IntegerProperty now = new SimpleIntegerProperty(0);
  static double volume = 1;

  static ObservableList<SongItem> getPlaylist() {
    return playListItem.playlist;
  }

  static PlaylistList.PlaylistItem getPlayListItem() {
    return playListItem;
  }
  static void setPlayListItem(PlaylistList.PlaylistItem playListItem) {
    Player.playListItem = playListItem;
  }

  static void setup(PlaylistList.PlaylistItem playList) {

    if (playListItem != null && !getPlaylist().isEmpty()) {
      getPlaylist().get(now.get()).playing.set(false);
      NowPlayingBar.removeListener();
    }
    if (mediaPlayer != null) {
      mediaPlayer.dispose();
      NowPlayingBar.playbackBar.removeAllListener();
    }

    media = null;
    mediaPlayer = null;
    setPlayListItem(playList);

    now = new SimpleIntegerProperty(0);
    NowPlayingBar.addListener();
  }

  static void play(int x) {
    if (getPlaylist().isEmpty()) return;
    x = Math.max(x, 0);
    int n = getPlaylist().size();
    x %= n;

    if (repeat == 0) {
      NowPlayingBar.prev.setDisable(x == 0);
      NowPlayingBar.next.setDisable(x == n - 1);
    }
    else {
      NowPlayingBar.next.setDisable(false);
      NowPlayingBar.prev.setDisable(false);
    }


    if (mediaPlayer != null) {
      mediaPlayer.setOnEndOfMedia(null);
      mediaPlayer.stop();
      mediaPlayer.dispose();
//      <previous play end hook

      NowPlayingBar.playbackBar.removeAllListener();
      LyricsView.removeListener();
//       previous play end hook>
      getPlaylist().get(now.get()).playing.set(false);
    }

    now.setValue(x);
    playing = getPlaylist().get(x);
    media = new Media(getPlaylist().get(x).fileUri);
    mediaPlayer = new MediaPlayer(media);
    mediaPlayer.setVolume(volume);
    mediaPlayer.setOnReady(()->{
      var nx  = mediaPlayer.getTotalDuration();
//      <play start hook
      NowPlayingBar.playbackBar.setMax(nx.toMillis());
      NowPlayingBar.playbackBar.addAllListener();
      LyricsView.lyrics.loadPlaying();
      LyricsView.addListener();
//       play start hook>
      NowPlayingBar.totalTime.setText(String.format("%02d:%02d", (int)nx.toMinutes(), ((int)nx.toSeconds()) % 60));

    });

//    mediaPlayer.setOnStopped(()->NowPlayingBar.playbackBar.removeAllListener());

    init();
    mediaPlayer.play();
    getPlaylist().get(now.get()).playing.set(true);
  }


  static void pause() {
    if (mediaPlayer != null) {
      mediaPlayer.pause();
      getPlaylist().get(now.get()).playing.set(false);
    }
  }

  static void resume() {
    if (mediaPlayer != null) {
      mediaPlayer.play();
      getPlaylist().get(now.get()).playing.set(true);
    }
    else {
      play( now.get());
    }
  }

  static void next() {
    play(now.get() + 1);
  }
  static void prev() {
    play(now.get() + getPlaylist().size() - 1);
  }

  static void changePlayState() {
    if (mediaPlayer == null || mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
      resume();
    }
    else if (mediaPlayer != null) pause();
  }

  static void init() {
    if (mediaPlayer != null) {
      mediaPlayer.setOnEndOfMedia(() -> {
        int next = now.get();
        if (repeat == 0 && now.get() == getPlaylist().size() - 1) {
          changePlayState();
          return;
        }
        if (repeat != 2) next++;
        if (repeat != 2 && shuffle) next = new Random().nextInt();
        play(next);
      });
      mediaPlayer.setOnPaused(()->{
        NowPlayingBar.play.setGraphic(new PlayIcon());
        getPlayListItem().playing.set(false);
      });
      mediaPlayer.setOnPlaying(()-> {
        NowPlayingBar.play.setGraphic(new PauseIcon());

        getPlayListItem().playing.set(true);
      });
    }
  }

  static void setVolume (double val) {
    volume = val;
    if (mediaPlayer != null) mediaPlayer.setVolume(val);
  }

  static void setCurrentTime(double millis) {
    if (mediaPlayer == null) return;
    mediaPlayer.seek(new Duration(millis));
  }
}

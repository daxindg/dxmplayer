module dxmplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.jetbrains.annotations;
    requires javafx.base;
    requires AnimateFX;
    opens dxmplayer to javafx.graphics;
}
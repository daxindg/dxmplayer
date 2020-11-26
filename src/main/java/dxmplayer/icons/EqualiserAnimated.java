package dxmplayer.icons;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class EqualiserAnimated {
    static final Image img = new Image(EqualiserAnimated.class.getResource("/img/equaliser-animated.gif").toString());
    public static final ImageView icon = new ImageView(img);
    static {
        icon.setStyle("-fx-min-width: 24px; -fx-max-width: 24px; -fx-min-height: 24px; -fx-max-height: 24px");
    }

}

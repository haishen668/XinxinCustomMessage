package ltd.dreamcraft.xinxincustommessage.objects;

import java.awt.*;

public class CustomText {
    public final String id;

    public final String text;

    public final Font font;

    public final int x;

    public final int z;

    public CustomText(String id, String text, Font font, int x, int z) {
        this.id = id;
        this.text = text;
        this.font = font;
        this.x = x;
        this.z = z;
    }
}


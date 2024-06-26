package ltd.dreamcraft.xinxincustommessage.objects;

import java.awt.*;

public class CustomText {
    public final String id;

    public final String text;

    public final Font font;

    public final int x;

    public final int z;
    public final boolean center;

    public CustomText(String id, String text, Font font, int x, int z, boolean center) {
        this.id = id;
        this.text = text;
        this.font = font;
        this.x = x;
        this.z = z;
        this.center = center;
    }
}


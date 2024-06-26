package ltd.dreamcraft.xinxincustommessage.objects;

public class SubImage {
    public final String id;

    public final String path;

    public final int width;

    public final int height;

    public final int x;

    public final int z;
    public final boolean center;

    public SubImage(String id, String path, int width, int height, int x, int z, boolean center) {
        this.id = id;
        this.path = path;
        this.width = width;
        this.height = height;
        this.x = x;
        this.z = z;
        this.center = center;
    }
}

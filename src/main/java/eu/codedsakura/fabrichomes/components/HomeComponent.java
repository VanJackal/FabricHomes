package eu.codedsakura.fabrichomes.components;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.NoSuchElementException;

import static eu.codedsakura.mods.TextUtils.valueRepr;

public class HomeComponent implements INamedDirectionalPointComponent {
    private double x, y, z;
    private float pitch, yaw;
    private String name;
    private Identifier dim;

    public HomeComponent(double x, double y, double z, float pitch, float yaw, Identifier dim, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.name = name;
        this.dim = dim;
    }

    public HomeComponent(Vec3d pos, float pitch, float yaw, Identifier dim, String name) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.name = name;
        this.dim = dim;
    }

    public static HomeComponent readFromNbt(NbtCompound tag) throws NoSuchElementException {
        return new HomeComponent(
                tag.getDouble("x").get(),
                tag.getDouble("y").get(),
                tag.getDouble("z").get(),
                tag.getFloat("pitch").get(),
                tag.getFloat("yaw").get(),
                Identifier.tryParse(tag.getString("dim").get()),
                tag.getString("name").get()
        );
    }

    public void writeToNbt(NbtCompound tag) {
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("pitch", pitch);
        tag.putFloat("yaw", yaw);
        tag.putString("name", name);
        tag.putString("dim", dim.toString());
    }

    @Override public double getX()  { return x; }
    @Override public double getY()  { return y; }
    @Override public double geyZ()  { return z; }
    @Override public float getPitch()  { return pitch; }
    @Override public float getYaw()    { return yaw;   }
    @Override public String getName()   { return name;  }
    @Override public Vec3d getCoords()  { return new Vec3d(x, y, z); }
    @Override public Identifier getDimID() { return dim; }

    @Override
    public MutableText toText(MinecraftServer server) {
        return Text.translatable("%s\n%s; %s; %s\n%s; %s\n%s",
                valueRepr("Name", name),
                valueRepr("X", x), valueRepr("Y", y), valueRepr("Z", z),
                valueRepr("Yaw", yaw), valueRepr("Pitch", pitch),
                valueRepr("In", dim.toString()));
    }
}

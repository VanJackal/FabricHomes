package eu.codedsakura.fabrichomes.components;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

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

    public HomeComponent(Vec3 pos, float pitch, float yaw, Identifier dim, String name) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.name = name;
        this.dim = dim;
    }

    public static HomeComponent readFromNbt(ValueInput tag) throws NoSuchElementException {
        return new HomeComponent(
                tag.getDoubleOr("x",0),
                tag.getDoubleOr("y",0),
                tag.getDoubleOr("z",0),
                tag.getFloatOr("pitch",0),
                tag.getFloatOr("yaw",0),
                Identifier.tryParse(tag.getStringOr("dim", "minecraft:overworld")),
                tag.getStringOr("name","riplmao")
        );
    }

    public void writeToNbt(ValueOutput tag) {
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
    @Override public Vec3 getCoords()  { return new Vec3(x, y, z); }
    @Override public Identifier getDimID() { return dim; }

    @Override
    public MutableComponent toText(MinecraftServer server) {
        return Component.translatable("%s\n%s; %s; %s\n%s; %s\n%s",
                valueRepr("Name", name),
                valueRepr("X", x), valueRepr("Y", y), valueRepr("Z", z),
                valueRepr("Yaw", yaw), valueRepr("Pitch", pitch),
                valueRepr("In", dim.toString()));
    }
}

package eu.codedsakura.fabrichomes.components;

import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public interface INamedDirectionalPointComponent {
    double getX();
    double getY();
    double geyZ();
    float getPitch();
    float getYaw();
    String getName();
    Vec3 getCoords();
    Identifier getDimID();
    MutableComponent toText(MinecraftServer server);
}

package eu.codedsakura.mods;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Timer;
import java.util.TimerTask;

public class TeleportUtils {
    public static void genericTeleport(boolean bossBar, double standStillTime, ServerPlayerEntity who, Runnable onCounterDone) {
        final double[] counter = {standStillTime};
        who.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 10, 5));

        final ServerPlayerEntity[] whoFinal = {who};
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (counter[0] == 0) {
                    whoFinal[0].sendMessage(Text.literal("Teleporting!").formatted(Formatting.LIGHT_PURPLE), true);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            whoFinal[0].networkHandler.sendPacket(new ClearTitleS2CPacket(true));
                        }
                    }, 500);
                    timer.cancel();
                    return;
                }


                whoFinal[0].networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Please stand still...")
                        .formatted(Formatting.RED, Formatting.ITALIC)));
                whoFinal[0].networkHandler.sendPacket(new TitleS2CPacket(Text.literal("Teleporting!")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)));
            }
        }, 0, 250);
    }
}

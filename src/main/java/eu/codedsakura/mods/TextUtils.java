package eu.codedsakura.mods;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public class TextUtils {
    public static MutableComponent valueRepr(String name, MutableComponent value) {
        if (value.getStyle().getColor() == null)
            return Component.literal(name + ": ").withStyle(ChatFormatting.RESET).append(value.copy().withStyle(ChatFormatting.GOLD));
        return Component.literal(name + ": ").withStyle(ChatFormatting.RESET).append(value);
    }
    public static MutableComponent valueRepr(String name, String value) {
        return valueRepr(name, Component.literal(value).withStyle(ChatFormatting.GOLD));
    }
    public static MutableComponent valueRepr(String name, double value) {
        return valueRepr(name, String.format("%.2f", value));
    }
    public static MutableComponent valueRepr(String name, float value) {
        return valueRepr(name, String.format("%.2f", value));
    }

    public static MutableComponent join(List<MutableComponent> values, MutableComponent joiner) {
        MutableComponent out = Component.empty();
        for (int i = 0; i < values.size(); i++) {
            out.append(values.get(i));
            if (i < values.size() - 1) out.append(joiner);
        }
        return out;
    }
}

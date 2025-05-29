package eu.codedsakura.fabrichomes.components;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class HomeDataComponent implements IHomeDataComponent {
    private final List<HomeComponent> homes = new ArrayList<>();
    private int maxHomes;

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        try {
            homes.clear();
            tag.getList("homes").get().forEach(v -> homes.add(HomeComponent.readFromNbt((NbtCompound) v)));
            maxHomes = tag.getInt("maxHomes").get();
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
            System.out.println("failed to read home data");
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList homeTag = new NbtList();
        homes.forEach(v -> {
            NbtCompound ct = new NbtCompound();
            v.writeToNbt(ct);
            homeTag.add(ct);
        });
        tag.put("homes", homeTag);
        tag.putInt("maxHomes", maxHomes);
    }

    @Override public List<HomeComponent> getHomes() { return homes; }
    @Override public int getMaxHomes() { return maxHomes; }

    @Override
    public boolean addHome(HomeComponent home) {
        if (homes.stream().anyMatch(v -> v.getName().equalsIgnoreCase(home.getName()))) return false;
        return homes.add(home);
    }

    @Override
    public boolean removeHome(String name) {
        if (homes.stream().noneMatch(v -> v.getName().equalsIgnoreCase(name))) return false;
        return homes.removeIf(v -> v.getName().equalsIgnoreCase(name));
    }
}

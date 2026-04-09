package eu.codedsakura.fabrichomes.components;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class HomeDataComponent implements IHomeDataComponent {
    private final List<HomeComponent> homes = new ArrayList<>();
    private int maxHomes;

    @Override
    public void readData(ValueInput readView) {
        try {
            homes.clear();
            readView.childrenListOrEmpty("homes").forEach(v -> homes.add(HomeComponent.readFromNbt(v)));
            maxHomes = readView.getIntOr("maxHomes", 1);
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
            System.out.println("failed to read home data");
        }
    }

    @Override
    public void writeData(ValueOutput writeView) {
        writeView.discard("homes");
        var listView = writeView.childrenList("homes");
        homes.forEach(v -> {
            v.writeToNbt(listView.addChild());
        });
        writeView.putInt("maxHomes", maxHomes);
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

package eu.codedsakura.fabrichomes.components;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class HomeDataComponent implements IHomeDataComponent {
    private final List<HomeComponent> homes = new ArrayList<>();
    private int maxHomes;

    @Override
    public void readData(ReadView readView) {
        try {
            homes.clear();
            readView.getListReadView("homes").forEach(v -> homes.add(HomeComponent.readFromNbt(v)));
            maxHomes = readView.getInt("maxHomes", 1);
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
            System.out.println("failed to read home data");
        }
    }

    @Override
    public void writeData(WriteView writeView) {
        writeView.remove("homes");
        var listView = writeView.getList("homes");
        homes.forEach(v -> {
            v.writeToNbt(listView.add());
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

package com.gmail.val59000mc.scenarios;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import com.gmail.val59000mc.UhcCore;

import org.bukkit.Bukkit;

public class DogNameGenerator {
    private List<String> names;
    private Set<String> madeNames;

    public DogNameGenerator(String fileName) {

        names = new ArrayList<>();
        madeNames = new HashSet<>();
        try {
            Scanner s = new Scanner(new File(UhcCore.getPlugin().getDataFolder(), fileName));
            while (s.hasNextLine()) { names.add(s.nextLine()); }
            s.close();
        } catch (FileNotFoundException f) {
            names.add("Spot");
        }
    }

    public String newName() { return names.get((int) (Math.random() * names.size())); }

    public String uniqueName() {
        while (true) {
            if (madeNames.size() == names.size()) return "Paul" + Math.random();
            String name = newName();
            if (Bukkit.getPlayer(name) != null) continue;
            if (madeNames.contains(name)) continue;
            madeNames.add(name);
            return name;
        }
    }
}
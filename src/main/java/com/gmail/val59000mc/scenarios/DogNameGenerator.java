package com.gmail.val59000mc.scenarios;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import com.gmail.val59000mc.UhcCore;

public class DogNameGenerator {
    private List<String> names;

    public DogNameGenerator() {
        
        names = new ArrayList<>();
        try {
            Scanner s = new Scanner(new File(UhcCore.getPlugin().getDataFolder(), "doggyNames.txt"));
            while (s.hasNextLine()){
                names.add(s.nextLine());
            }
            s.close();
        } catch(FileNotFoundException f) {
            names.add("Spot");
        }
    }

    public String newName() {
        return names.get((int)(Math.random()*names.size()));
    }
}
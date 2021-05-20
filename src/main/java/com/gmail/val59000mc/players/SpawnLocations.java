package com.gmail.val59000mc.players;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.gmail.val59000mc.game.GameManager;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class SpawnLocations {

    private static Queue<Location> randLocs;

    private static Set<Material> nonoBlocks;

    public SpawnLocations() {

        nonoBlocks = new HashSet<>();
        nonoBlocks.add(Material.LAVA);
        nonoBlocks.add(Material.WATER);
        nonoBlocks.add(Material.MAGMA_BLOCK);
        nonoBlocks.add(Material.CACTUS);
        nonoBlocks.add(Material.BARRIER);
        nonoBlocks.add(Material.GLOWSTONE);
        nonoBlocks.add(Material.FIRE);
        nonoBlocks.add(Material.STONE_BRICKS);
        nonoBlocks.add(Material.STONE_BRICK_STAIRS);
        nonoBlocks.add(Material.STONE_BRICK_SLAB);
        nonoBlocks.add(Material.CRACKED_STONE_BRICKS);
        nonoBlocks.add(Material.STONE_BRICK_WALL);
    }

    public static Queue<Location> getStoredLocs() { return randLocs; }

    public static void createSpawnLocations(int num) {
        GameManager gm = GameManager.getGameManager();
        World world = Bukkit.getWorld(gm.getConfiguration().getOverworldUuid());
        randLocs = getRandomSafeLocations(world, num, 400 / Math.sqrt(num));
        gm.finishLoad("GENERATESPAWNPOINTS");
    }

    public static Location newRandomLocation(World world, boolean top) {
        return newRandomLocation(world, Math.min(world.getWorldBorder().getSize() / 2.,
                GameManager.getGameManager().getConfiguration().getBorderStartSize()), top);
    }

    public static Location newRandomLocation(World world, double range, boolean top) {
        Random r = new Random();
        WorldBorder wb = world.getWorldBorder();
        double x = range * (r.nextDouble() * 2 - 1) + wb.getCenter().getX();
        double z = range * (r.nextDouble() * 2 - 1) + wb.getCenter().getZ();
        return new Location(world, x, top ? 256 : Math.random() * 128, z);
    }

    private static Location getGroundLocation(Location loc) {
        Location setLoc = loc.clone();

        double dist = Double.MAX_VALUE;

        while (setLoc.getY() < 256) {
            setLoc.setY(1);
            setLoc.setX((int) (setLoc.getX()) + 0.5);
            setLoc.setZ((int) (setLoc.getZ()) + 0.5);
            Material below = setLoc.clone().add(0, -1, 0).getBlock().getType();
            Material at = setLoc.getBlock().getType();
            Material up1 = setLoc.clone().add(0, 1, 0).getBlock().getType();
            Material up2 = setLoc.clone().add(0, 2, 0).getBlock().getType();
            Material up3 = setLoc.clone().add(0, 3, 0).getBlock().getType();

            while (!below.isSolid() || at == Material.AIR || up1 == Material.AIR || up2 == Material.AIR
                    || up3 == Material.AIR) {
                setLoc.add(0, 1, 0);
                below = at;
                at = up1;
                up1 = up2;
                up2 = up3;
                up3 = setLoc.clone().add(0, 3, 0).getBlock().getType();
            }
            double newDist = setLoc.distanceSquared(loc);
            if (newDist >= dist) return setLoc;
            dist = newDist;
        }
        return setLoc;
    }

    public static Queue<Location> getRandomSafeLocations(World world, int num, double minDist) {
        return getRandomSafeLocations(world, num, minDist, 20);
    }

    public static Queue<Location> getRandomSafeLocations(World world, int num, double minDist, int bailout) {
        List<Location> checkedLocations = new LinkedList<>();

        for (int i = 0; i < num; i++) {
            Location randomLoc = findRandomSafeLocation(world);

            boolean verified = false;
            for (int j = 0; j < bailout && !verified; j++) {
                verified = true;
                for (Location l : checkedLocations) {
                    if (l.distanceSquared(randomLoc) < minDist * minDist) {
                        verified = false;
                        // Bukkit.getLogger().info("[UhcCore] Location " + randomLoc.toString() + " was
                        // within " + minDist
                        // + " of another team");
                        randomLoc = findRandomSafeLocation(world);
                        break;
                    }
                }
            }

            if (!verified) Bukkit.getLogger().info("[UhcCore] Location " + randomLoc.toString() + " was within "
                    + minDist + " blocks of another team, but the bailout was reached.");
            else Bukkit.getLogger().info("[UhcCore] Spawn location " + (i + 1) + "/" + num + " found.");
            checkedLocations.add(randomLoc);
        }

        return new LinkedList<>(checkedLocations);
    }

    public static Location findRandomSafeLocation(World world) { return findRandomSafeLocation(world, 20); }

    public static Location findRandomSafeLocation(World world, int bailout) {
        boolean ontop = world.getEnvironment() != World.Environment.NETHER;
        for (int i = 0; i < bailout; i++) {
            Location loc = verifySafe(newRandomLocation(world, ontop));
            if (loc != null) return loc;
        }
        Bukkit.getLogger().info("[UHCCore] Bailout reached, returning random location");
        return getGroundLocation(newRandomLocation(world, ontop));
    }

    public static Location verifySafe(Location loc) {
        boolean nether = loc.getWorld().getEnvironment() == World.Environment.NETHER;
        Location ground = getGroundLocation(loc);

        ground.setPitch(0);
        ground.setYaw(0);

        if (nether && ground.getBlockY() > 120) return null;

        Material material = ground.clone().add(0, -1, 0).getBlock().getType();
        while (material == Material.BARRIER) {
            ground = getBelow(ground);
            if (ground == null) return null;
            material = ground.clone().add(0, -1, 0).getBlock().getType();
        }
        if (!material.isSolid() || nonoBlocks.contains(material)
                || !GameManager.getGameManager().getWorldBorder().isWithinBorder(ground))
            return null;
        return ground;
    }

    private static Location getBelow(Location loc) {
        int y = -2;
        while (loc.getBlockY() + y > 0 && loc.getWorld()
                .getBlockAt(loc.getBlockX(), loc.getBlockY() + y, loc.getBlockZ()).getType() == Material.AIR) {
            if (loc.getBlockY() + y <= 0) return null;
            y--;
        }
        return loc.clone().add(0, y + 1, 0);
    }

    @Nullable
    public static Location findSafeLocationAround(Location loc, int range) {
        Location betterLocation = verifySafe(loc);
        if (betterLocation != null) return betterLocation;

        Location testLoc = loc.clone();

        int i = 1;
        Vector dir = new Vector(0, 0, 1);
        // dear god I hope it never gets to full map size
        while (i < range * 2) {
            for (int j = 0; j < i; j++) {
                betterLocation = verifySafe(testLoc);
                if (betterLocation != null) return betterLocation;
                testLoc.add(dir);
            }

            dir = new Vector(dir.getZ(), 0, -dir.getX());

            for (int j = 0; j < i; j++) {
                betterLocation = verifySafe(testLoc);
                if (betterLocation != null) return betterLocation;
                testLoc.add(dir);
            }

            dir = new Vector(dir.getZ(), 0, -dir.getX());
            i++;
        }

        // Bukkit.getLogger().info("[UhcCore] Could not find any safe spawn spot near "
        // + loc.toString() + " within "
        // + range + " blocks!");

        return null;
    }

}

package com.gmail.val59000mc.utils;

import org.bukkit.block.BlockFace;

import java.util.Arrays;
import java.util.Random;

public class RandomUtils {
	private final static Random r = new Random();

	public static int randomInteger(int min, int max) {
		int realMin = Math.min(min, max);
		int realMax = Math.max(min, max);
		int exclusiveSize = realMax - realMin;
		return r.nextInt(exclusiveSize + 1) + min;
	}

	public static BlockFace randomAdjacentFace() {
		BlockFace[] faces = new BlockFace[] { BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.WEST,
				BlockFace.NORTH, BlockFace.SOUTH };
		return faces[randomInteger(0, faces.length - 1)];
	}

	public static String randomTextColor() { return '\u00a7' + Integer.toHexString(r.nextInt(16)); }

	public static String randomTextColor(String... omit) {
		String s = randomTextColor();
		// this is dumb but its easier than making a new list
		while (Arrays.asList(omit).contains(s)) s = randomTextColor();
		return s;
	}

}

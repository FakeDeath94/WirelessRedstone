package net.licks92.wirelessredstone.signs;

import net.licks92.wirelessredstone.Utils;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.block.data.type.Sign;
import org.bukkit.Material;
import org.bukkit.block.data.type.Sign.Side;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

@SerializableAs("WirelessScreen")
public class WirelessScreen extends WirelessPoint implements ConfigurationSerializable {

    public WirelessScreen(int x, int y, int z, String world, boolean isWallSign, BlockFace direction, String owner) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.isWallSign = isWallSign;
        this.direction = direction;
        this.owner = owner;
    }

    public WirelessScreen(Map<String, Object> map) {
        owner = (String) map.get("owner");
        world = (String) map.get("world");
        isWallSign = (Boolean) map.get("isWallSign");
        x = (Integer) map.get("x");
        y = (Integer) map.get("y");
        z = (Integer) map.get("z");

        try {
            direction = BlockFace.valueOf(map.get("direction").toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                int directionInt = Integer.parseInt(map.get("direction").toString());
                direction = Utils.getBlockFace(false, directionInt); // In the past normal signs and wall signs where saved under one direction
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public void turnOn() {
        updateSign(true);
    }

    public void turnOff() {
        updateSign(false);
    }

    public void updateSign(boolean isChannelOn) {
        if (getLocation() == null)
            return;

        getLocation().getWorld().loadChunk(getLocation().getChunk());

        if (!(getLocation().getBlock().getState() instanceof Sign)) {
            return;
        }

        String str = isChannelOn ? ChatColor.GREEN + "ACTIVE" : ChatColor.RED + "INACTIVE";

        Sign signState = (Sign) getLocation().getBlock().getState();
        org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign) signState.getBlockData();

        // Check if the sign is attached to a wall or standing
        if (signData instanceof WallSign) {
            // Wall signs have only one side that can be written on
            SignSide signSide = signState.getSide(Side.FRONT);
            signSide.setLine(2, str);
        } else {
            // Standing signs can have multiple sides
            SignSide frontSide = signState.getSide(Side.FRONT);
            frontSide.setLine(2, str);
        }

        signState.update();
    }


    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("direction", getDirection().name().toUpperCase());
        map.put("isWallSign", isWallSign());
        map.put("owner", getOwner());
        map.put("world", getWorld());
        map.put("x", getX());
        map.put("y", getY());
        map.put("z", getZ());
        return map;
    }

    @Override
    public String toString() {
        return "WirelessScreen{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", owner='" + owner + '\'' +
                ", world='" + world + '\'' +
                ", direction=" + direction +
                ", isWallSign=" + isWallSign +
                '}';
    }
}

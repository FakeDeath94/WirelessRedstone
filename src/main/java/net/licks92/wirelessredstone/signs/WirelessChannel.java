package net.licks92.wirelessredstone.signs;

import net.licks92.wirelessredstone.ConfigManager;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@SerializableAs("WirelessChannel")
public class WirelessChannel implements ConfigurationSerializable {

    private int id;
    private String name;
    private boolean active;
    private boolean locked;

    private List<String> owners = new ArrayList<>();
    private List<WirelessTransmitter> transmitters = new ArrayList<>();
    private List<WirelessReceiver> receivers = new ArrayList<>();
    private List<WirelessScreen> screens = new ArrayList<>();

    public WirelessChannel(String name) {
        this.name = name;
        this.active = false;
        this.locked = false;
    }

    public WirelessChannel(String name, boolean locked) {
        this.name = name;
        this.active = false;
        this.locked = locked;
    }

    public WirelessChannel(String name, List<String> owners) {
        this.name = name;
        this.owners = owners;
        this.active = false;
        this.locked = false;
    }

    public WirelessChannel(String name, List<String> owners, boolean locked) {
        this.name = name;
        this.owners = owners;
        this.active = false;
        this.locked = locked;
    }

    public WirelessChannel(Map<String, Object> map) {
        this.id = getAsType(map.get("id"), Integer.class);
        this.name = getAsType(map.get("name"), String.class);
        this.active = (Boolean) map.getOrDefault("active", false);
        this.owners = getAsTypeList(map.get("owners"), String.class);
        this.receivers = getAsTypeList(map.get("receivers"), WirelessReceiver.class);
        this.transmitters = getAsTypeList(map.get("transmitters"), WirelessTransmitter.class);
        this.screens = getAsTypeList(map.get("screens"), WirelessScreen.class);
        this.locked = getAsType(map.get("locked"), Boolean.class, false);

        convertOwnersToUuid();
    }

    @SuppressWarnings("unchecked")
    private <T> T getAsType(Object obj, Class<T> type) {
        if (type.isInstance(obj)) {
            return (T) obj;
        }
        throw new ClassCastException("Failed to cast to " + type.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private <T> T getAsType(Object obj, Class<T> type, T defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (type.isInstance(obj)) {
            return (T) obj;
        }
        throw new ClassCastException("Failed to cast to " + type.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getAsTypeList(Object obj, Class<T> type) {
        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            if (list.isEmpty() || type.isInstance(list.get(0))) {
                return (List<T>) list;
            }
        }
        throw new ClassCastException("Failed to cast to List<" + type.getSimpleName() + ">");
    }

    public void turnOn() {
        turnOn(0);
    }

    public void turnOn(int time) {
        WirelessRedstone.getWRLogger().debug("Channel#turnOn() WirelessChannel{" +
                "name='" + name + '\'' +
                ", active=" + active +
                "}");

        if (isLocked()) {
            WirelessRedstone.getWRLogger().debug("Channel " + name + " didn't turn on because locked.");
            return;
        }

        if (time > 0 && time < 50) {
            throw new IllegalArgumentException("Time must be at least 50ms.");
        }

        if (active) {
            return;
        }

        active = true;

        getReceivers().forEach(receiver -> receiver.turnOn(name));
        getScreens().forEach(WirelessScreen::turnOn);

        WirelessRedstone.getStorage().updateSwitchState(this);

        if (time >= 50) {
            Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(),
                    () -> turnOff(null, true),
                    time / 50);
        }
    }

    public void turnOff(Location loc) {
        turnOff(loc, false);
    }

    public void turnOff(Location loc, boolean force) {
        if (isLocked()) {
            WirelessRedstone.getWRLogger().debug("Channel " + name + " didn't turn off because locked.");
            return;
        }

        if (!active) {
            return;
        }

        boolean canTurnOff = true;
        if (ConfigManager.getConfig().useORLogic() && !force) {
            for (WirelessTransmitter transmitter : getTransmitters()) {
                if (loc != null) {
                    if (Utils.sameLocation(loc, transmitter.getLocation())) {
                        continue;
                    }
                }

                if (transmitter.isPowered()) {
                    canTurnOff = false;
                    break;
                }
            }
        }

        WirelessRedstone.getWRLogger().debug("Channel#turnOff() WirelessChannel{" +
                "name='" + name + '\'' +
                ", active=" + active +
                ", canTurnOff=" + canTurnOff +
                "}");

        if (!canTurnOff) {
            active = true;
            return;
        }

        active = false;

        getReceivers().forEach(receiver -> receiver.turnOff(name));
        getScreens().forEach(WirelessScreen::turnOff);
    }

    public void addWirelessPoint(WirelessPoint wirelessPoint) {
        if (wirelessPoint instanceof WirelessTransmitter) {
            if (!transmitters.contains(wirelessPoint)) {
                transmitters.add((WirelessTransmitter) wirelessPoint);
            }
        } else if (wirelessPoint instanceof WirelessScreen) {
            if (!screens.contains(wirelessPoint)) {
                screens.add((WirelessScreen) wirelessPoint);
            }
        } else if (wirelessPoint instanceof WirelessReceiver) {
            if (!receivers.contains(wirelessPoint)) {
                receivers.add((WirelessReceiver) wirelessPoint);
            }
        }

        //TODO: Maybe add owner from wirelesspoint to list of owners
    }

    public void removeWirelessPoint(WirelessPoint wirelessPoint) {
        if (wirelessPoint instanceof WirelessTransmitter) {
            transmitters.remove(wirelessPoint);
        } else if (wirelessPoint instanceof WirelessScreen) {
            screens.remove(wirelessPoint);
        } else if (wirelessPoint instanceof WirelessReceiver) {
            receivers.remove(wirelessPoint);
        }

        //TODO: Maybe remove owner from wirelesspoint to list of owners
    }

    public void addOwner(String uuid) {
        if (!owners.contains(uuid))
            owners.add(uuid);
    }

    public void removeOwner(String uuid) {
        owners.remove(uuid);
    }

    public void convertOwnersToUuid() {
        List<String> updatedOwners = new ArrayList<>();
        for (String owner : owners) {
            if (!owner.contains("-")) {
                UUID uuid = getUUIDFromName(owner);
                if (uuid != null) {
                    updatedOwners.add(uuid.toString());
                }
            } else {
                updatedOwners.add(owner);
            }
        }
        owners = updatedOwners;
    }

    private UUID getUUIDFromName(String name) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                if (scanner.hasNext()) {
                    String response = scanner.next();
                    String id = response.split("\"id\":\"")[1].split("\"")[0];
                    return UUID.fromString(
                        id.replaceFirst(
                            "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                            "$1-$2-$3-$4-$5"
                        )
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }   
     
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLocked() {
        return locked;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public List<WirelessTransmitter> getTransmitters() {
        return transmitters;
    }

    public void setTransmitters(List<WirelessTransmitter> transmitters) {
        this.transmitters = transmitters;
    }

    public List<WirelessReceiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<WirelessReceiver> receivers) {
        this.receivers = receivers;
    }

    public List<WirelessScreen> getScreens() {
        return screens;
    }

    public void setScreens(List<WirelessScreen> screens) {
        this.screens = screens;
    }

    public boolean isActive() {
        return active;
    }

    public List<WirelessPoint> getSigns() {
        List<WirelessPoint> signs = new ArrayList<>();
        signs.addAll(getTransmitters());
        signs.addAll(getReceivers());
        signs.addAll(getScreens());
        return signs;
    }

    public boolean isEmpty() {
        return getSigns().isEmpty();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", getId());
        map.put("name", getName());
        map.put("active", isActive());
        map.put("owners", getOwners());
        map.put("receivers", getReceivers());
        map.put("transmitters", getTransmitters());
        map.put("screens", getScreens());
        map.put("locked", isLocked());
        return map;
    }

    @Override
    public String toString() {
        return "WirelessChannel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                ", locked=" + locked +
                ", owners=" + owners +
                ", transmitters=" + transmitters +
                ", receivers=" + receivers +
                ", screens=" + screens +
                '}';
    }
}
package net.licks92.WirelessRedstone.Channel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.scheduler.BukkitTask;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name = "wirelesschannels")
@SerializableAs("WirelessChannel")
public class WirelessChannel implements ConfigurationSerializable, Serializable {
	private static final long serialVersionUID = -3322590857684087871L;
	@Id
	private int id;
	@NotNull
	private String name;
	@NotNull
	private boolean locked;

	private List<String> owners = new LinkedList<String>();
	private List<WirelessTransmitter> transmitters = new LinkedList<WirelessTransmitter>();
	private List<WirelessReceiver> receivers = new LinkedList<WirelessReceiver>();
	private List<WirelessScreen> screens = new LinkedList<WirelessScreen>();

	public WirelessChannel(final String name) {
		this.setName(name);
	}

	@SuppressWarnings("unchecked")
	public WirelessChannel(final Map<String, Object> map) {
		this.setId((Integer) map.get("id"));
		this.setName((String) map.get("name"));
		this.setOwners((List<String>) map.get("owners"));
		this.setReceivers((List<WirelessReceiver>) map.get("receivers"));
		this.setTransmitters((List<WirelessTransmitter>) map
				.get("transmitters"));
		this.setScreens((List<WirelessScreen>) map.get("screens"));
		try {
			this.setLocked((Boolean) map.get("locked"));
		} catch (NullPointerException ex) {

		}
	}

	/**
	 * This method is almost the same as turnOn(), except that the channel will
	 * be on for a temporary time only!
	 *
	 * @param time
	 *            - Time spent until the channel turns off in ms.
	 */
	public void turnOn(final int time) {
		if (isLocked()) {
			WirelessRedstone.getWRLogger().debug(
					"Channel " + name + " didn't turn on because locked.");
			return;
		}
		int timeInTicks = time * 20; // It's the time in ticks, where the time
										// variable is supposed to be the time
										// in ticks.
		turnOn();
		Bukkit.getScheduler().runTaskLater(
				Bukkit.getPluginManager().getPlugin("WirelessRedstone"),
				new Runnable() {
					@Override
					public void run() {
						turnOff();
					}

				}, timeInTicks);
	}

	/**
	 * Simply turns on the wireless channel, means that all the receivers and
	 * screens will turn on.
	 */
	public void turnOn() {
		if (isLocked()) {
			WirelessRedstone.getWRLogger().debug(
					"Channel " + name + " didn't turn on because locked.");
			return;
		}
		// Turning on the receivers ONLY if the channel isn't active.
		try {
			// Change receivers
			for (WirelessReceiver receiver : receivers) {
				receiver.turnOn(getName());
			}

			// Turning on screens
			for (WirelessScreen screen : screens) {
				screen.turnOn();
			}
		} catch (RuntimeException e) {
			WirelessRedstone
					.getWRLogger()
					.severe("Error while turning on the receivers of channel "
							+ name
							+ ". Please turn the debug mode on to get more informations.");
			if (WirelessRedstone.config.getDebugMode())
				e.printStackTrace();
		}
		if(!WirelessRedstone.getInstance().activeChannels.contains(getName())){
			WirelessRedstone.getInstance().activeChannels.add(getName());
		}
	}

	/**
	 * Simply turns off the channel : all the receivers and screens turn off.
	 */
	public void turnOff() {
		try {
			// Change receivers
			for (WirelessReceiver receiver : getReceivers()) {
				receiver.turnOff(getName());
			}

			// Change screens
			for (WirelessScreen screen : screens) {
				screen.turnOff();
			}
		} catch (RuntimeException e) {
			WirelessRedstone
					.getWRLogger()
					.severe("Error while updating redstone onBlockRedstoneChange for Screens , turn on the Debug Mode to get more informations.");
			if (WirelessRedstone.config.getDebugMode())
				e.printStackTrace();
		}
		if(WirelessRedstone.getInstance().activeChannels.contains(getName())){
			WirelessRedstone.getInstance().activeChannels.remove(getName());
		}
	}

	public void startClock(final BukkitTask task) {
		WirelessRedstone.getInstance().clockTasks.put(task.getTaskId(),
				getName());
		WirelessRedstone.getWRLogger().debug(
				"Added clock task " + task.getTaskId()
						+ " to te list for circuit " + getName());
	}

	public void stopClock() {
		ArrayList<Integer> remove = new ArrayList<Integer>();
		for (Map.Entry<Integer, String> task : WirelessRedstone.getInstance().clockTasks
				.entrySet()) {
			if (!task.getValue().equalsIgnoreCase(getName())) {
				continue;
			}
			Bukkit.getScheduler().cancelTask(task.getKey());
			remove.add(task.getKey());
			WirelessRedstone.getWRLogger().debug("Stopped clock task " + task);
		}
		for (Integer i : remove) {
			WirelessRedstone.getInstance().clockTasks.remove(i);
		}
		remove.clear();
	}

	public void toggle(final Block block) {
		Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(),
				new Runnable() {
					@Override
					public void run() {
						if (block.isBlockPowered()
								|| block.isBlockIndirectlyPowered()) {
							if(WirelessRedstone.getInstance().activeChannels.contains(getName())){
								return;
							}
							turnOn();
						} else if (!block.isBlockPowered()
								|| !block.isBlockIndirectlyPowered()) {
							turnOff();
						}
					}
				}, 1L);
	}

	/**
	 * @return true if one of the transmitters is active, false if they are all
	 *         off.
	 */
	public boolean isActive() {
		for (WirelessTransmitter t : getTransmitters()) {
			Location loc = new Location(Bukkit.getWorld(t.getWorld()),
					t.getX(), t.getY(), t.getZ());
			Block block = loc.getBlock();
			if (block.getState() instanceof Sign) {
				if (block.isBlockIndirectlyPowered()
						|| block.isBlockIndirectlyPowered()) {
					return true;
				}
			}

			ArrayList<BlockFace> possibleBlockface = new ArrayList<BlockFace>();
			possibleBlockface.add(BlockFace.NORTH);
			possibleBlockface.add(BlockFace.EAST);
			possibleBlockface.add(BlockFace.SOUTH);
			possibleBlockface.add(BlockFace.WEST);
			possibleBlockface.add(BlockFace.UP);
			possibleBlockface.add(BlockFace.DOWN);

			for (BlockFace blockFace : possibleBlockface) {
				if (block.getRelative(blockFace).isBlockIndirectlyPowered()
						|| block.getRelative(blockFace).isBlockIndirectlyPowered()) {
					return true;
				}
			}
		}
		return false;
	}

	public void removeReceiverAt(final Location loc) {
		for (WirelessReceiver receiver : receivers) {
			if (receiver.getX() == loc.getBlockX()
					&& receiver.getZ() == loc.getBlockZ()
					&& receiver.getY() == loc.getBlockY()) {
				receivers.remove(receiver);
				return;
			}
		}
	}

	public void removeTransmitterAt(final Location loc) {
		for (WirelessTransmitter transmitter : transmitters) {
			if (transmitter.getX() == loc.getBlockX()
					&& transmitter.getZ() == loc.getBlockZ()
					&& transmitter.getY() == loc.getBlockY()) {
				transmitters.remove(transmitter);
				return;
			}
		}
	}

	public void removeScreenAt(final Location loc) {
		for (WirelessScreen screen : screens) {
			if (screen.getX() == loc.getBlockX()
					&& screen.getZ() == loc.getBlockZ()
					&& screen.getY() == loc.getBlockY()) {
				screens.remove(screen);
				return;
			}
		}
	}

	public boolean removeOwner(final String username) {
		boolean ret = this.owners.remove(username);

		return ret;
	}

	public void addTransmitter(final WirelessTransmitter transmitter) {
		if (transmitters == null)
			transmitters = new ArrayList<WirelessTransmitter>();

		transmitters.add(transmitter);
	}

	public void addReceiver(final WirelessReceiver receiver) {
		if (receivers == null)
			receivers = new ArrayList<WirelessReceiver>();

		receivers.add(receiver);
	}

	public void addScreen(final WirelessScreen screen) {
		if (screens == null)
			screens = new LinkedList<WirelessScreen>();

		screens.add(screen);
	}

	public void addOwner(final String username) {
		if (this.owners == null)
			this.owners = new LinkedList<String>();

		if (!this.owners.contains(username))
			this.owners.add(username);
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setLocked(final boolean value) {
		this.locked = value;
	}

	public void setOwners(final List<String> owners) {
		this.owners = owners;
	}

	public void setTransmitters(final List<WirelessTransmitter> transmitters) {
		if (transmitters != null)
			this.transmitters = transmitters;
		else
			this.transmitters = new LinkedList<WirelessTransmitter>();
	}

	public void setReceivers(final List<WirelessReceiver> receivers) {
		if (receivers != null)
			this.receivers = receivers;
		else
			this.receivers = new LinkedList<WirelessReceiver>();
	}

	public void setScreens(final List<WirelessScreen> screens) {
		if (screens != null)
			this.screens = screens;
		else
			this.screens = new LinkedList<WirelessScreen>();
	}

	public String getName() {
		return this.name;
	}

	public boolean isLocked() {
		return this.locked;
	}

	public List<WirelessTransmitter> getTransmitters() {
		try {
			return this.transmitters;
		} catch (NullPointerException ex) {
			return new LinkedList<WirelessTransmitter>();
		}
	}

	public List<WirelessReceiver> getReceivers() {
		try {
			return this.receivers;
		} catch (NullPointerException ex) {
			return new LinkedList<WirelessReceiver>();
		}
	}

	public List<WirelessScreen> getScreens() {
		try {
			return this.screens;
		} catch (NullPointerException ex) {
			return new LinkedList<WirelessScreen>();
		}
	}

	public List<String> getOwners() {
		try {
			return this.owners;
		} catch (NullPointerException ex) {
			return new LinkedList<String>();
		}
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", getId());
		map.put("name", getName());
		map.put("owners", getOwners());
		map.put("receivers", getReceivers());
		map.put("transmitters", getTransmitters());
		map.put("screens", getScreens());
		map.put("locked", isLocked());
		return map;
	}
}

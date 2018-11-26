package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Commands.WirelessCommandTabCompletion;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Add owner to WirlessChannel", usage = "<channel> <playername>", aliases = {"addowner"},
        tabCompletion = {WirelessCommandTabCompletion.CHANNEL, WirelessCommandTabCompletion.PLAYER},
        permission = "addOwner", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminAddOwner extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        String channelName = args[0];
        String playerName = args[1];

        if (!hasAccessToChannel(sender, channelName)) {
            Utils.sendFeedback(WirelessRedstone.getStrings().permissionChannelAccess, sender, true);
            return;
        }

        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);
        if (channel == null) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelNotFound, sender, true);
            return;
        }

        if (channel.getOwners().contains(playerName)) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelAlreadyOwner, sender, true);
            return;
        }

        channel.addOwner(playerName);
        WirelessRedstone.getStorage().updateChannel(channelName, channel);

        WirelessRedstone.getWRLogger().info("Channel " + channelName + " has been updated. Player " + playerName + " has been added to the owner list.");
        Utils.sendFeedback(WirelessRedstone.getStrings().channelOwnerAdded.replaceAll("%%PLAYERNAME", playerName), sender, false);
    }
}

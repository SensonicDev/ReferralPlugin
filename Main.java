package me.sensonic.referrals;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


public class Main extends JavaPlugin {

    private File referralsFile = new File(getDataFolder() + "//referrals.yml");
    private FileConfiguration referralsYAML = YamlConfiguration.loadConfiguration(referralsFile);

    private static HashMap<String, String> referrals = new HashMap<>();
    private static HashMap<String, String> awaitingConfirmList = new HashMap<>();
    private static String hFN = "\u00a72\u00a7lHUNT\u00a76\u00a7lREFERRALS \u00a77";

    @Override
    public void onEnable() {

        if (!referralsFile.exists()) saveResource("referrals.yml", true);
        referralsYAML.options().copyDefaults(true);

        //loads referral list
        loadReferrals();

        //loads config
        this.getConfig().options().copyDefaults(true);
        saveConfig();

        //enabled message
        Bukkit.getConsoleSender().sendMessage(" ");
        Bukkit.getConsoleSender().sendMessage(hFN + "enabled! " +
                "\u00a78| \u00a74Developers: \u00a79Sensonic \u00a77& \u00a79DevAnonymous \u00a78| \u00a77Version \u00a74" + this.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(" ");
    }

    @Override
    public void onDisable() {

        // saves the referral list for future use
        saveReferrals();

        //disabled message
        Bukkit.getConsoleSender().sendMessage(" ");
        Bukkit.getConsoleSender().sendMessage(hFN + "disabled! " +
                "\u00a78| \u00a74Developers: \u00a79Sensonic \u00a77& \u00a79DevAnonymous \u00a78| \u00a77Version \u00a74" + this.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(" ");
    }


    //defines commands and checks if used
    @Override
    public boolean onCommand(CommandSender sender ,Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("refer")) {
            //sender cannot be the console
            if (!(sender instanceof Player)) {
                sender.sendMessage(hFN + "The console cannot perform this command!");
                return false;
            }

            Player p = (Player) sender;

            // gives info about usage if command is used without args
            if (args.length == 0) {
                p.sendMessage(hFN + "Please use \u00a72/refer <playername> \u00a77or \u00a72/refer confirm <playername> \u00a77to refer others and receive a reward.");

                return true;
            }

            //if there is 1 argument, checks if player is valid, than refers them.
            if (args.length == 1) {
                if (Bukkit.getPlayer(args[0]) != null) {

                    if (Bukkit.getPlayer(args[0]) == sender) {
                        p.sendMessage(hFN + "You cannot refer yourself. Nice try!");
                        return false;
                    }
                    if (referrals.get(p.getName()) != null) {
                        if (referrals.get(p.getName()).compareTo(args[0]) == 0) {
                            p.sendMessage(hFN + "You cannot refer the player who has already referred you.");
                            return false;
                        }
                    }

                    Player t = Bukkit.getPlayer(args[0]);
                    String pS = p.getName();
                    String tS = args[0];

                    // checks if the target player has already been referred, and cancels the referral if true.
                    if (referrals.containsKey(tS)) {
                        p.sendMessage(hFN + "That player has already been referred. A player can only be referred once.");
                        return true;
                    }

                    if (!(awaitingConfirmList.containsKey(tS) && awaitingConfirmList.get(tS).compareTo(pS) == 0)) {
                        //successfully sent referral request, so puts them in the awaiting confirmation list
                        awaitingConfirmList.put(tS, pS);
                        p.sendMessage(hFN + "You have sent a referral request to \u00a72" + tS);
                        t.sendTitle(hFN, "\u00a77You have been referred by \u00a72" + pS + "\u00a77!", 20, 60, 20);
                        t.sendMessage(hFN + "You have been referred by \u00a72" + pS + "\u00a77!");
                        t.sendMessage(hFN + "Please use \u00a72/refer confirm " + pS + " \u00a77to confirm you were referred by this player.");
                    } else {
                        p.sendMessage(hFN + "You have already sent a referral request to this player, please await confirmation.");
                    }




                } else { //incorrect usage, checks if player meant to do the confirm command but failed
                    if (args[0].compareTo("confirm") == 0) {
                        p.sendMessage(hFN + "Please use \u00a72/refer confirm <playername>");
                        return true;
                    } else {
                        //incorrect usage and no use of confirm, so wrong player was searched for
                        p.sendMessage(hFN + "We can't seem to find the player you were looking for.");
                    }
                }
                return true;
            }

            if (args.length == 2) {
                if (args[0].compareTo("confirm") == 0) {
                    if (Bukkit.getPlayer(args[1]) != null) {
                        Player t = Bukkit.getPlayer(args[1]);
                        String pS = p.getName();
                        String tS = args[1];

                        // checks if the player has a referral awaiting confirmation
                        if (awaitingConfirmList.containsKey(pS)) {

                            if (awaitingConfirmList.get(pS).compareTo(tS) == 0) {

                                loadReferrals();
                                referrals.put(pS, tS);
                                saveReferrals();

                                awaitingConfirmList.remove(pS, tS);

                                PlayerInventory invP = p.getInventory();
                                PlayerInventory invT = t.getInventory();

                                ItemStack reward = new ItemStack(Material.valueOf(getConfig().getString("Reward Item").toUpperCase()));

                                ItemMeta rewardMeta = reward.getItemMeta();
                                rewardMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("Reward Item Name")));
                                rewardMeta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("Reward Item Description"))));

                                reward.setItemMeta(rewardMeta);
                                invP.addItem(reward);
                                invT.addItem(reward);

                                try {
                                    t.playSound(t.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                } catch (Exception s) {
                                    if(p.isOp()) {
                                        p.sendMessage(hFN + "Sound is misconfigured. Please ask dev for help.");
                                    }

                                }

                                p.sendMessage(hFN + "You have been successfully referred by \u00a72" + tS + "\u00a77!");
                                t.sendMessage(hFN + "You have successfully referred \u00a72" + pS + "\u00a77!");
                                Bukkit.broadcastMessage(hFN + "\u00a76" + t.getName() + " \u00a77has referred \u00a76" + p.getName() + "\u00a77 to \u00a72Hunt\u00a76Network \u00a77and both received a reward!");
                                p.sendMessage(hFN + "You have been gifted a special reward. Have fun!");
                                t.sendMessage(hFN + "You have been gifted a special reward. Have fun!");

                                return true;

                            }
                            p.sendMessage(hFN + "Are you sure that's the player who referred you?");
                            return true;
                        }

                        p.sendMessage(hFN + "You have no referrals awaiting confirmation!");
                        return true;
                    } else {
                        p.sendMessage(hFN + "We couldn't find the player you were looking for.");
                        return true;
                    }
                } else {
                    p.sendMessage(hFN + "Please use \u00a72/refer <playername> \u00a77or \u00a72/refer confirm <playername>");
                    return true;
                }
            } else {
                p.sendMessage(hFN + "Incorrect usage! Please use \u00a72/refer <playername>");
                return true;
            }

        }

        return super.onCommand(sender, cmd, label, args);
    }



    private void saveReferrals() {
        for(String referral : referrals.keySet()) {
            String value = referrals.get(referral);
            referralsYAML.set("referrals."+referral, value);
        }
        try {
            referralsYAML.save(referralsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadReferrals() {
        referrals = new HashMap<>(); //reset the credits
        if(referralsYAML.getConfigurationSection("referrals") != null ) {
            //the referrals have been saved before, lets load them
            Set<String> set = referralsYAML.getConfigurationSection("referrals").getKeys(false);
            for(String referral : set) {
                String value = referralsYAML.getString(referral);
                referrals.put(referral, value);
            }
        }
    }

}

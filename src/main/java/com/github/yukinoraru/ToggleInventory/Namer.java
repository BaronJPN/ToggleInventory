package com.github.yukinoraru.ToggleInventory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Class, that allows setting and getting name and lore of item.
 * Based on PrettyScaryLib.Namer
 */
public class Namer {

    /** craft stack. */
    private static CraftItemStack					craftStack;

    /** item stack. */
    private static net.minecraft.server.ItemStack	itemStack;

    /**
     * Sets name.
     *
     * @param item item
     * @param name name
     * @return item stack
     */
    public static ItemStack setName(ItemStack item, String name) {
        if (item instanceof CraftItemStack) {
            craftStack = (CraftItemStack) item;
            Namer.itemStack = craftStack.getHandle();
        }
        else if (item instanceof ItemStack) {
            craftStack = new CraftItemStack(item);
            Namer.itemStack = craftStack.getHandle();
        }
        NBTTagCompound tag = itemStack.tag;
        if (tag == null) {
            tag = new NBTTagCompound();
            tag.setCompound("display", new NBTTagCompound());
            itemStack.tag = tag;
        }

        tag = itemStack.tag.getCompound("display");
        //tag.setString("Name", ChatColor.RESET + name);
        tag.setString("Name", name);
        itemStack.tag.setCompound("display", tag);
        return craftStack;
    }

    public static ItemStack setNameInURLEncoded(ItemStack item, String encoded_name) throws UnsupportedEncodingException {
        String name = URLDecoder.decode(encoded_name, "UTF-8");
        return setName(item, name);
    }

    public static ItemStack setLoreInURLEncoded(ItemStack item, String... encoded_lores) throws UnsupportedEncodingException {
        String []lores = new String[encoded_lores.length];
        for(int m = 0; m < lores.length; m++){
            lores[m] = URLDecoder.decode(encoded_lores[m], "UTF-8");
        }
        return setLore(item, lores);
    }

    public static String getNameInURLEncoded(ItemStack item) throws UnsupportedEncodingException {
        String name = getName(item);
        return (name != null) ? URLEncoder.encode(name, "UTF-8") : "";
    }

    public static String[] getLoreInURLEncoded(ItemStack item) throws UnsupportedEncodingException {
        String []lores = getLore(item);
        String []encoded_lores = new String[lores.length];
        for(int m = 0; m < lores.length; m++){
            encoded_lores[m] = URLEncoder.encode(lores[m], "UTF-8");
        }
        return encoded_lores;
    }

    /**
     * Gets name.
     *
     * @param item item
     * @return name
     */
    public static String getName(ItemStack item) {
        if (item instanceof CraftItemStack) {
            craftStack = (CraftItemStack) item;
            Namer.itemStack = craftStack.getHandle();
        }
        else if (item instanceof ItemStack) {
            craftStack = new CraftItemStack(item);
            Namer.itemStack = craftStack.getHandle();
        }
        if (itemStack == null) {
            return null;
        }
        NBTTagCompound tag = itemStack.tag;
        if (tag == null) {
            return null;
        }
        tag = itemStack.tag.getCompound("display");
        return tag.getString("Name");
    }

    /**
     * Sets lore.
     *
     * @param item item
     * @param lore lore
     * @return item stack
     */
    public static ItemStack setLore(ItemStack item, String... lore) {
        if (item instanceof CraftItemStack) {
            craftStack = (CraftItemStack) item;
            Namer.itemStack = craftStack.getHandle();
        }
        else if (item instanceof ItemStack) {
            craftStack = new CraftItemStack(item);
            Namer.itemStack = craftStack.getHandle();
        }
        NBTTagCompound tag = itemStack.tag;
        if (tag == null) {
            tag = new NBTTagCompound();
            tag.setCompound("display", new NBTTagCompound());
            itemStack.tag = tag;
        }
        tag = itemStack.tag.getCompound("display");
        NBTTagList list = new NBTTagList();
        for (String l : lore) {
            //list.add(new NBTTagString("", ChatColor.RESET + l));
            list.add(new NBTTagString("", l));
        }
        tag.set("Lore", list);
        itemStack.tag.setCompound("display", tag);
        return craftStack;
    }

    /**
     * Adds lore.
     *
     * @param item item
     * @param lore lore
     * @return item stack
     */
    public static ItemStack addLore(ItemStack item, String lore) {
        if (item instanceof CraftItemStack) {
            craftStack = (CraftItemStack) item;
            Namer.itemStack = craftStack.getHandle();
        }
        else if (item instanceof ItemStack) {
            craftStack = new CraftItemStack(item);
            Namer.itemStack = craftStack.getHandle();
        }
        NBTTagCompound tag = itemStack.tag;
        if (tag == null) {
            tag = new NBTTagCompound();
            tag.setCompound("display", new NBTTagCompound());
            tag.getCompound("display").set("Lore", new NBTTagList());
            itemStack.tag = tag;
        }

        tag = itemStack.tag.getCompound("display");
        NBTTagList list = tag.getList("Lore");
        list.add(new NBTTagString("", ChatColor.RESET + lore));
        tag.set("Lore", list);
        itemStack.tag.setCompound("display", tag);
        return craftStack;
    }

    /**
     * Gets lore.
     *
     * @param item item
     * @return lore
     */
    public static String[] getLore(ItemStack item) {
        if (item instanceof CraftItemStack) {
            craftStack = (CraftItemStack) item;
            Namer.itemStack = craftStack.getHandle();
        }
        else if (item instanceof ItemStack) {
            craftStack = new CraftItemStack(item);
            Namer.itemStack = craftStack.getHandle();
        }
        NBTTagCompound tag = itemStack.tag;
        if (tag == null) {
            tag = new NBTTagCompound();
            tag.setCompound("display", new NBTTagCompound());
            tag.getCompound("display").set("Lore", new NBTTagList());
            itemStack.tag = tag;
        }
        tag = itemStack.tag;
        NBTTagList list = tag.getCompound("display").getList("Lore");
        String[] lores = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            lores[i] = list.get(i).toString();
        }
        return lores;
    }
}
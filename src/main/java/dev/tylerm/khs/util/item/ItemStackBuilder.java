/*
 *
 *   AstralFlow - The plugin enriches bukkit servers
 *   Copyright (C) 2022 The Inlined Lambdas and Contributors
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *   USA
 */

package dev.tylerm.khs.util.item;

import dev.tylerm.khs.item.CustomItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class ItemStackBuilder {
    private Material material = Material.AIR;
    private List<String> lore = new ArrayList<>();
    private int customModelId;
    private String displayName;
    private List<ItemFlag> itemFlags = new ArrayList<>();
    private int customItemId=-1;

    private boolean unbreakable;

    public ItemStackBuilder unbreakable() {
        this.unbreakable = true;
        return this;
    }

    public ItemStackBuilder material(Material material) {
        requireNonNull(material, "Material cannot be null");
        if(material.isAir()){
            throw new IllegalArgumentException("Material cannot be AIR");
        }
        this.material = material;
        return this;
    }

    public ItemStackBuilder lore(String... strings) {
        requireNonNull(strings, "Lore cannot be null");
        for (String string : strings) {
            lore.add(string.replaceAll("&", ChatColor.COLOR_CHAR + ""));
        }
        return this;
    }

    public ItemStackBuilder customItemKey(int key){
        customItemId = key;;
        return this;
    }

    public ItemStackBuilder customModelId(int customModelId) {
        if(customModelId < 0){
            throw new IllegalArgumentException("Custom model id cannot be negative");
        }
        this.customModelId = customModelId;
        return this;
    }

    public ItemStackBuilder displayName(String displayName) {
        requireNonNull(displayName, "Display name cannot be null");
        this.displayName = displayName.replaceAll("&", ChatColor.COLOR_CHAR + "");
        return this;
    }

    public ItemStackBuilder itemFlags(ItemFlag... flags) {
        requireNonNull(flags, "Flags cannot be null");
        itemFlags.addAll(Arrays.asList(flags));
        return this;
    }


    public ItemStack build() {
        if (material == Material.AIR) {
            throw new IllegalArgumentException("Material cannot be AIR");
        }
        var item = new ItemStack(material);
        var meta = item.getItemMeta();
        if (customModelId != 0 || displayName != null || !lore.isEmpty() || unbreakable || !itemFlags.isEmpty() || customItemId != 0) {
            meta.setCustomModelData(customModelId);
            meta.setLore(lore);
            meta.setDisplayName(displayName);
            meta.setUnbreakable(unbreakable);
            meta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
        }
        meta.getPersistentDataContainer().set(CustomItems.CUSTOM_ITEM, PersistentDataType.INTEGER, customItemId);
        item.setItemMeta(meta);
        return item;
    }
}
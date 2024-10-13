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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Util for working with ItemStacks.
 */
@ApiStatus.AvailableSince("0.1.0")
public final class ItemStacks {
    /**
     * Create an itemstack by a builder pattern.
     *
     * @param material The material of the itemstack.
     * @return The itemstack.
     */
    public static ItemStackBuilder builder(Material material) {
        return new ItemStackBuilder().material(material);
    }

    /**
     * A quicker way to create sample itemstacks.
     *
     * @param material material of the item
     * @param name     display name of the item
     * @param lores    lore of the item
     * @return itemstack
     */
    @NotNull
    public static ItemStack of(Material material, String name, String... lores) {
        requireNonNull(material);
        if (material.isAir()) throw new IllegalArgumentException("Material cannot be air");
        requireNonNull(name);
        return builder(material).displayName(name).lore(lores == null ? new String[0] : lores).build();
    }

    public static ItemStack consumeOrNull(ItemStack itemStack, int amount) {
        if (itemStack.getAmount() < amount) {
            return null;
        }
        if (itemStack.getAmount() == amount) {
            return new ItemStack(Material.AIR);
        }
        var item = itemStack.clone();
        item.setAmount(item.getAmount() - amount);
        return item;
    }
}
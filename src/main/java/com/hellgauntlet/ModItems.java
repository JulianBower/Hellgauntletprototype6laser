package com.hellgauntlet;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HellGauntletMod.MODID);

    // 10000 durability (also makes it stack to 1), fire resistant like netherite
    public static final DeferredItem<Item> GAUNTLET = ITEMS.register("gauntlet_of_hell",
            () -> new GauntletItem(new Item.Properties()
                    .durability(10000)
                    .fireResistant()
                    .attributes(GauntletItem.createAttributes())));

    // Only used as the model of the flying black orb. Not in any creative tab.
    public static final DeferredItem<Item> VOID_ORB = ITEMS.register("void_orb",
            () -> new Item(new Item.Properties()));
}

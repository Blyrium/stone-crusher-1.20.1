package net.blyrium.stonecrusher.screen;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.client.gui.screen.recipebook.AbstractFurnaceRecipeBookScreen;
import net.minecraft.item.Item;

import java.util.Set;

public class CrusherRecipeBookComponent extends AbstractFurnaceRecipeBookScreen {

    @Override
    protected Set<Item> getAllowedFuels() {
        return AbstractFurnaceBlockEntity.createFuelTimeMap().keySet();
    }
}

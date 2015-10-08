package oortcloud.modulardrills.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import oortcloud.modulardrills.items.ItemModularDrill;
import oortcloud.modulardrills.items.ModItems;

public class ModularRecipe implements IRecipe {

	private final ItemStack recipeOutput = new ItemStack(ModItems.drill);

	@Override
	public boolean matches(InventoryCrafting inventory, World world) {
		ItemStack drill = getDrill(inventory);
		ItemStack component = getComponent(inventory);
		if (drill != null && component != null) {
			if (component.getItem() == ModItems.drilltip) {
				if (ItemModularDrill.getNBTTipTier(drill) + 1 == component.getItemDamage()) {
					return true;
				}
			}
			if (component.getItem() == ModItems.drillmotor) {
				if (ItemModularDrill.getNBTMotorTier(drill) + 1 == component.getItemDamage()) {
					return true;
				}
			}
			if (component.getItem() == ModItems.drillbattery) {
				if (ItemModularDrill.getNBTBatteryTier(drill) + 1 == component.getItemDamage()) {
					return true;
				}
			}
		}

		return false;
	}

	private ItemStack getDrill(InventoryCrafting inventory) {
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				ItemStack itemstack = inventory.getStackInRowAndColumn(j, i);

				if (itemstack != null) {
					if (itemstack.getItem() == ModItems.drill)
						return itemstack;
				}
			}
		}
		return null;
	}

	private ItemStack getComponent(InventoryCrafting inventory) {
		int cnt = 0;
		ItemStack component = null;
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				ItemStack itemstack = inventory.getStackInRowAndColumn(j, i);

				if (itemstack != null) {
					if (isComponent(itemstack)) {
						component = itemstack;
						cnt++;
					}
				}
			}
		}

		if (cnt == 1)
			return component;
		return null;
	}

	private boolean isComponent(ItemStack stack) {
		Item item = stack.getItem();
		return item == ModItems.drilltip || item == ModItems.drillmotor || item == ModItems.drillbattery;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory) {
		ItemStack drill = getDrill(inventory).copy();
		ItemStack component = getComponent(inventory);
		if (component.getItem() == ModItems.drilltip) {
			ItemModularDrill.setNBTTipTier(drill, component.getItemDamage());
		}
		if (component.getItem() == ModItems.drillmotor) {
			ItemModularDrill.setNBTMotorTier(drill, component.getItemDamage());
		}
		if (component.getItem() == ModItems.drillbattery) {
			ItemModularDrill.setNBTBatteryTier(drill, component.getItemDamage());
		}
		return drill;
	}

	@Override
	public int getRecipeSize() {
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return recipeOutput;
	}

}

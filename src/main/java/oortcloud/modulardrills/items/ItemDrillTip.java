package oortcloud.modulardrills.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import oortcloud.modulardrills.ModularDrills;
import oortcloud.modulardrills.libs.References;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemDrillTip extends Item {

	private IIcon[] icons;

	public ItemDrillTip() {
		super();
		setCreativeTab(ModularDrills.tabModulardrills);
		setUnlocalizedName(References.RESOURCESPREFIX + "drilltip");
		setHasSubtypes(true);
		ModItems.register(this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icons = new IIcon[6];

		for (int i = 0; i < icons.length; i++) {
			icons[i] = iconRegister.registerIcon(ModItems.getUnwrappedUnlocalizedName(super.getUnlocalizedName()) + "_" + i);
		}

		this.itemIcon = icons[0];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack)+"_"+stack.getItemDamage();
	}
	
	@Override
	public IIcon getIconFromDamage(int damage) {
		if (damage < icons.length)
			return icons[damage];
		return itemIcon;
	}
	
	@Override
	public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List list) {
		list.add(new ItemStack(this,1,0));
		list.add(new ItemStack(this,1,1));
		list.add(new ItemStack(this,1,2));
		list.add(new ItemStack(this,1,3));
		list.add(new ItemStack(this,1,4));
		list.add(new ItemStack(this,1,5));
	}
}

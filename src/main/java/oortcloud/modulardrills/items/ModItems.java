package oortcloud.modulardrills.items;

import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModItems {

	public static Item drill;
	public static Item drilltip;
	public static Item drillmotor;
	public static Item drillbattery;
	
	public static void init() {
		drill = new ItemModularDrill();
		drilltip = new ItemDrillTip();
		drillmotor = new ItemDrillMotor();
		drillbattery = new ItemDrillBattery();
	}
	
	public static String getUnwrappedUnlocalizedName(String unlocalizedName)
	{
		return unlocalizedName.substring(unlocalizedName.indexOf(".") + 1);
	}
	
	public static String getName(String unlocalizedName)
	{
		return unlocalizedName.substring(unlocalizedName.indexOf(":") + 1);
	}
	
	public static void register(Item item) {
		GameRegistry.registerItem(item, getName(item.getUnlocalizedName()));
	}
}

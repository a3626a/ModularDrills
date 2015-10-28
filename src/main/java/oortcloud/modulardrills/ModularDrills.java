package oortcloud.modulardrills;

import ic2.api.item.IC2Items;
import ic2.core.Ic2Items;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import oortcloud.modulardrills.core.proxy.CommonProxy;
import oortcloud.modulardrills.crafting.ModularRecipe;
import oortcloud.modulardrills.items.ModItems;
import oortcloud.modulardrills.libs.References;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = References.MODID, name = References.MODNAME, version = References.VERSION)
public class ModularDrills {
	@Mod.Instance
	public static ModularDrills instance;

	@SidedProxy(clientSide = References.CLIENTPROXYLOCATION, serverSide = References.COMMONPROXYLOCATION)
	public static CommonProxy proxy;

	public static CreativeTabs tabModulardrills = new CreativeTabs("tabModularDrills") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return Ic2Items.miningDrill.getItem();
		}
	};
	
	public static Logger logger;

	@Mod.EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		GameRegistry.addRecipe(new ModularRecipe());
	}

	@Mod.EventHandler
	public static void Init(FMLInitializationEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ModItems.init();
		
		/*
		for (String i : OreDictionary.getOreNames()) {
			System.out.print(i + " : ");
			for (ItemStack j :OreDictionary.getOres(i)) {
				System.out.print(j.getDisplayName() + ", ");
			}
			System.out.println();
		}
		*/
		
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.drill), " a "," b "," c ", 'a', ModItems.drilltip, 'b', ModItems.drillbattery,'c', ModItems.drillmotor);
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillbattery,1,0), " a ", "aba", " a " , 'a' , "plateTin", 'b', new ItemStack(IC2Items.getItem("chargedReBattery").getItem(),1,OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillbattery,1,1), "aaa", "bbb", "aaa" , 'a' , "plateTin", 'b', new ItemStack(IC2Items.getItem("chargedReBattery").getItem(),1,OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillbattery,1,2), " a ", "aba", " a " , 'a' , "plateBronze", 'b', new ItemStack(IC2Items.getItem("advBattery").getItem(),1,OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillbattery,1,3), "aaa", "bbb", "aaa" , 'a' , "plateBronze", 'b', new ItemStack(IC2Items.getItem("advBattery").getItem(),1,OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillbattery,1,4), " a ", "aba", " a " , 'a' , "plateSteel", 'b', new ItemStack(IC2Items.getItem("energyCrystal").getItem(),1,OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillbattery,1,5), "aaa", "bbb", "aaa" , 'a' , "plateSteel", 'b', new ItemStack(IC2Items.getItem("energyCrystal").getItem(),1,OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillbattery,1,6), " a ", "aba", " a " , 'a' , Ic2Items.iridiumPlate, 'b', new ItemStack(IC2Items.getItem("lapotronCrystal").getItem(),1,OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillbattery,1,7), "aaa", "bbb", "aaa" , 'a' , Ic2Items.iridiumPlate, 'b', new ItemStack(IC2Items.getItem("lapotronCrystal").getItem(),1,OreDictionary.WILDCARD_VALUE)));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drilltip,1,0), " a ", "aba", "aca" , 'a' , "plateIron", 'b', "ingotIron", 'c', "blockIron"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drilltip,1,1), " a ", "aba", "aba" , 'a' , "plateBronze", 'b', "blockBronze"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drilltip,1,2), " c ", "aba", "aba" , 'a' , "plateSteel", 'b', "blockSteel", 'c', "plateDenseSteel"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drilltip,1,3), " a ", "aba", "aba" , 'a' , "gemDiamond", 'b', Ic2Items.coalChunk));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drilltip,1,4), " a ", "aaa", "aba" , 'a' , Ic2Items.iridiumOre, 'b', Items.nether_star));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drilltip,1,5), " a ", "aba", "aba" , 'a' , Ic2Items.iridiumPlate, 'b', Items.nether_star));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillmotor,1,0), "aaa", "aba", "aaa" , 'a' , Ic2Items.copperCableItem, 'b', Ic2Items.elemotor));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillmotor,1,1), " a ", "aba", " a " , 'a' , new ItemStack(ModItems.drillmotor,1,0), 'b', "blockCopper"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillmotor,1,2), "aaa", "aba", "aaa" , 'a' , Ic2Items.goldCableItem, 'b', Ic2Items.elemotor));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillmotor,1,3), " a ", "aba", " a " , 'a' , new ItemStack(ModItems.drillmotor,1,2), 'b', "blockGold"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillmotor,1,4), "aaa", "aba", "aaa" , 'a' , Ic2Items.carbonFiber, 'b', Ic2Items.elemotor));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillmotor,1,5), " a ", "aba", " a " , 'a' , new ItemStack(ModItems.drillmotor,1,4), 'b', Ic2Items.coalChunk));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillmotor,1,6), " a ", "aba", " a " , 'a' , Ic2Items.iridiumOre, 'b', Ic2Items.elemotor));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.drillmotor,1,7), " a ", "aba", " a " , 'a' , new ItemStack(ModItems.drillmotor,1,6), 'b', Ic2Items.iridiumPlate));

	}

	@Mod.EventHandler
	public static void postInit(FMLPostInitializationEvent event) {
	}
}


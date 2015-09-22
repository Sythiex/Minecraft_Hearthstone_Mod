package com.sythiex.hearthstonemod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = HearthstoneMod.MODID, version = HearthstoneMod.VERSION)
public class HearthstoneMod
{
	public static final String MODID = "hearthstonemod";
    public static final String VERSION = "0.1.2";
    
    public static boolean difficulty;
	
    public static Item hearthstone;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());
    	config.load();
    	
    	difficulty = config.getBoolean("Easy Recipe", config.CATEGORY_GENERAL, false, "Change to true to use lapis instead of diamonds in the Hearthstone recipe");
    	
    	config.save();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	hearthstone = new ItemHearthstone();
    	GameRegistry.registerItem(hearthstone, MODID + "_hearthstone");
    	
    	if(difficulty)
    	{
    		ItemStack stoneStack = new ItemStack(Blocks.stone);
    		ItemStack clockStack = new ItemStack(Items.clock);
    		ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
    		GameRegistry.addRecipe(new ItemStack(hearthstone),
    			"SLS",
    			"LCL",
    			"SLS",
    			'S', stoneStack, 'C', clockStack, 'L', lapisStack);
    	}
    	else
    	{
    		ItemStack stoneStack = new ItemStack(Blocks.stone);
    		ItemStack clockStack = new ItemStack(Items.clock);
    		ItemStack diamondStack = new ItemStack(Items.diamond);
    		GameRegistry.addRecipe(new ItemStack(hearthstone),
    			"SDS",
    			"DCD",
    			"SDS",
    			'S', stoneStack, 'C', clockStack, 'D', diamondStack);
    	}
    }
}
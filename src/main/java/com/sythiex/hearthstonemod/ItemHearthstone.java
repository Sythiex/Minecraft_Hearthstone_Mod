package com.sythiex.hearthstonemod;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class ItemHearthstone extends Item
{
	public static int maxCooldown = 36000; // 30min
	public static int maxCastTime = 200; // 10sec
	
	private boolean castFlag = false; // used to stop casting
	
	private double prevX = 0;
	private double prevY = 0;
	private double prevZ = 0;
	
	public TargetedEntitySound channelSound;
	
	public ItemHearthstone()
	{
		super();
		setUnlocalizedName("hearthstone");
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.tabTools);
		setTextureName(HearthstoneMod.MODID + ":hearthstone");
	}
	
	@Override
	public void onUpdate(ItemStack itemStack, World world, Entity entity, int p_77663_4_, boolean p_77663_5_)
	{
		if(!world.isRemote)
		{
			if(itemStack.stackTagCompound != null)
			{
				int cooldown = itemStack.stackTagCompound.getInteger("cooldown");
				if(cooldown > 0)
				{
					cooldown--;
					itemStack.stackTagCompound.setInteger("cooldown", cooldown);
				}
			}
			else
			{
				itemStack.stackTagCompound = new NBTTagCompound();
				itemStack.stackTagCompound.setInteger("cooldown", 0);
				itemStack.stackTagCompound.setInteger("castTime", 0);
				itemStack.stackTagCompound.setInteger("bedX", 0);
				itemStack.stackTagCompound.setInteger("bedY", 0);
				itemStack.stackTagCompound.setInteger("bedZ", 0);
				itemStack.stackTagCompound.setInteger("bedDimension", 0);
				itemStack.stackTagCompound.setBoolean("locationSet", false);
				itemStack.stackTagCompound.setBoolean("isCasting", false);
				// itemStack.stackTagCompound.setInteger("distance", 0);
			}
			
			if(itemStack.stackTagCompound.getBoolean("isCasting") && entity instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer) entity;
				int castTime = itemStack.stackTagCompound.getInteger("castTime") + 1;
				itemStack.stackTagCompound.setInteger("castTime", castTime);
				
				if(player.ticksExisted % 5 == 0)
				{
					HearthstoneMod.proxy.generateChannelParticles(player);
				}
				
				double diffX = Math.abs(prevX - player.posX);
				double diffY = Math.abs(prevY - player.posY);
				double diffZ = Math.abs(prevZ - player.posZ);
				// if player moves cancel cast
				if(((diffX > 0.05 || diffY > 0.05 || diffZ > 0.05) && prevX != 0) || castFlag)
				{
					Minecraft.getMinecraft().getSoundHandler().stopSound(channelSound);
					itemStack.stackTagCompound.setInteger("castTime", 0);
					itemStack.stackTagCompound.setBoolean("isCasting", false);
					player.addChatMessage(new ChatComponentTranslation("msg.hearthstoneCastCanceled.txt"));
					
					if(castFlag)
					{
						castFlag = false;
					}
				}
				
				// initiate tp after casting
				if(itemStack.stackTagCompound.getInteger("castTime") >= maxCastTime)
				{
					Minecraft.getMinecraft().getSoundHandler().stopSound(channelSound);
					itemStack.stackTagCompound.setInteger("castTime", 0);
					itemStack.stackTagCompound.setBoolean("isCasting", false);
					
					world.playSoundEffect(prevX, prevY, prevZ, "hearthstonemod:hearthstoneCast", 1, 1);
					
					int dimension = itemStack.stackTagCompound.getInteger("bedDimension");
					// if player is not in same dimension as bed, travel to that dimension
					if(dimension != player.dimension)
					{
						player.travelToDimension(dimension);
					}
					
					int bedX = itemStack.stackTagCompound.getInteger("bedX");
					int bedY = itemStack.stackTagCompound.getInteger("bedY");
					int bedZ = itemStack.stackTagCompound.getInteger("bedZ");
					
					// checks if bed is still there
					if(player.worldObj.getBlock(bedX, bedY, bedZ).isBed(player.worldObj, bedX, bedY, bedZ, player))
					{
						Material material1 = player.worldObj.getBlock(bedX - 1, bedY, bedZ).getMaterial();
						Material material2 = player.worldObj.getBlock(bedX - 1, bedY + 1, bedZ).getMaterial();
						
						Material material3 = player.worldObj.getBlock(bedX + 1, bedY, bedZ).getMaterial();
						Material material4 = player.worldObj.getBlock(bedX + 1, bedY + 1, bedZ).getMaterial();
						
						Material material5 = player.worldObj.getBlock(bedX, bedY, bedZ - 1).getMaterial();
						Material material6 = player.worldObj.getBlock(bedX, bedY + 1, bedZ - 1).getMaterial();
						
						Material material7 = player.worldObj.getBlock(bedX, bedY, bedZ + 1).getMaterial();
						Material material8 = player.worldObj.getBlock(bedX, bedY + 1, bedZ + 1).getMaterial();
						
						// finds open space around bed and tps player
						if(!material1.isSolid() && !material1.isLiquid() && !material2.isSolid() && !material2.isLiquid())
						{
							player.setPositionAndUpdate(bedX - 1 + 0.5, bedY, bedZ + 0.5);
						}
						else if(!material3.isSolid() && !material3.isLiquid() && !material4.isSolid() && !material4.isLiquid())
						{
							player.setPositionAndUpdate(bedX + 1 + 0.5, bedY, bedZ + 0.5);
						}
						else if(!material5.isSolid() && !material5.isLiquid() && !material6.isSolid() && !material6.isLiquid())
						{
							player.setPositionAndUpdate(bedX + 0.5, bedY, bedZ - 1 + 0.5);
						}
						else if(!material7.isSolid() && !material7.isLiquid() && !material8.isSolid() && !material8.isLiquid())
						{
							player.setPositionAndUpdate(bedX + 0.5, bedY, bedZ + 1 + 0.5);
						}
						// defaults to tp player on top of bed
						else
						{
							player.setPositionAndUpdate(bedX + 0.5, bedY + 1, bedZ + 0.5);
						}
						
						world.playSoundEffect(entity.posX, entity.posY, entity.posZ, "hearthstonemod:hearthstoneImpact", 1, 1);
						itemStack.stackTagCompound.setInteger("cooldown", maxCooldown); // sets hearthstone on
																						// cooldown
					}
					// tps player to where bed was, then breaks link
					else
					{
						player.setPositionAndUpdate(bedX + 0.5, bedY + 1, bedZ + 0.5);
						world.playSoundEffect(entity.posX, entity.posY, entity.posZ, "hearthstonemod:hearthstoneImpact", 1, 1);
						itemStack.stackTagCompound.setInteger("cooldown", maxCooldown); // sets hearthstone on
																						// cooldown
						itemStack.stackTagCompound.setBoolean("locationSet", false);
						// informs player of broken link
						player.addChatMessage(new ChatComponentTranslation("msg.hearthstoneMissingBed.txt"));
					}
				}
			}
			
			prevX = entity.posX;
			prevY = entity.posY;
			prevZ = entity.posZ;
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		if(!world.isRemote)
		{
			// not sneaking
			if(!player.isSneaking())
			{
				// location is set
				if(itemStack.stackTagCompound.getBoolean("locationSet"))
				{
					int cooldown = itemStack.stackTagCompound.getInteger("cooldown");
					// off cooldown
					if(cooldown <= 0)
					{
						// if player is not casting, start casting
						if(!itemStack.stackTagCompound.getBoolean("isCasting"))
						{
							itemStack.stackTagCompound.setBoolean("isCasting", true);
							channelSound = new TargetedEntitySound(player, new ResourceLocation("hearthstonemod:hearthstoneChannel"));
							Minecraft.getMinecraft().getSoundHandler().playSound(channelSound);
						}
					}
					// on cooldown
					else
					{
						player.addChatMessage(new ChatComponentTranslation("msg.hearthstoneOnCooldown.txt"));
					}
				}
				// location is not set
				else
				{
					player.addChatMessage(new ChatComponentTranslation("msg.hearthstoneNoBed.txt"));
				}
			}
		}
		return itemStack;
	}
	
	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int metadata, float sideX, float sideY, float sideZ)
	{
		if(!world.isRemote)
		{
			// sneaking and hearthstone is not linked
			if(player.isSneaking() && !itemStack.stackTagCompound.getBoolean("locationSet"))
			{
				// checks if block right clicked is bed, then links hearthstone
				if(world.getBlock(x, y, z).isBed(world, x, y, z, player))
				{
					itemStack.stackTagCompound.setInteger("bedX", x);
					itemStack.stackTagCompound.setInteger("bedY", y);
					itemStack.stackTagCompound.setInteger("bedZ", z);
					itemStack.stackTagCompound.setInteger("bedDimension", player.dimension);
					itemStack.stackTagCompound.setBoolean("locationSet", true);
					player.addChatMessage(new ChatComponentTranslation("msg.hearthstoneLinked.txt"));
				}
			}
			return true;
		}
		else
			return false;
	}
	
	public void stopCasting()
	{
		this.castFlag = true;
	}
	
	public boolean showDurabilityBar(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound != null)
		{
			return itemStack.stackTagCompound.getInteger("cooldown") > 0 || itemStack.stackTagCompound.getInteger("castTime") > 0;
		}
		else
			return false;
	}
	
	public double getDurabilityForDisplay(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound.getInteger("cooldown") > 0)
			return (double) itemStack.stackTagCompound.getInteger("cooldown") / (double) maxCooldown;
		else
			return (double) 1 - (itemStack.stackTagCompound.getInteger("castTime") / (double) maxCastTime);
	}
	
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4)
	{
		if(itemStack.stackTagCompound != null)
		{
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(3);
			int cooldown = itemStack.stackTagCompound.getInteger("cooldown");
			float minutesExact, secondsExact;
			int minutes, seconds;
			minutesExact = cooldown / 1200;
			minutes = (int) minutesExact;
			secondsExact = cooldown / 20;
			seconds = (int) (secondsExact - (minutes * 60));
			list.add("Cooldown: " + minutes + " minutes " + seconds + " seconds");
			// list.add("Distance teleported: " + itemStack.stackTagCompound.getInteger("distance"));
			/*
			 * sprinting adds .1 exhaustion per meter, jumping adds .2 exhaustion
			 * assuming the player jumps often while traveling, every 5m traveled adds ~.7 exhaustion, or .14 exhaustion per
			 * meter
			 * every 4.0 exhaustion subtracts 1 point of saturation, so every 28.57m consumes 1 saturation (4.0/.14)
			 * steak adds 12.8 saturation, so traveling 365.71m consumes a steak's worth of saturation (28.57*12.8)
			 * distance traveled * (1/365.71) gives the number of steaks that would be used to travel that distance
			 */
			// list.add("Steaks saved: " + df.format(itemStack.stackTagCompound.getInteger("distance") * .002734));
		}
	}
}

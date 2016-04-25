package com.sythiex.hearthstonemod;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;

public class TargetedEntitySound extends MovingSound
{
	private final Entity entity;
	private static int maxPlayTime = ItemHearthstone.maxCastTime; // 10sec
	private int count = 0;
	
	public TargetedEntitySound(Entity entity, ResourceLocation resourceLocation)
	{
		super(resourceLocation);
		this.entity = entity;
		this.xPosF = (float) entity.posX;
		this.yPosF = (float) entity.posY;
		this.zPosF = (float) entity.posZ;
		this.repeat = true;
		this.volume = 1.0F;
		this.repeatDelay = 0;
	}
	
	@Override
	public void update()
	{
		count++;
		if(count > maxPlayTime)
		{
			this.donePlaying = true;
		}
	}
	
	@Override
	public boolean isDonePlaying()
	{
		return this.donePlaying;
	}
}
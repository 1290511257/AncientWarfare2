/**
 * Copyright 2012 John Cummens (aka Shadowmage, Shadowmage4513)
 * This software is distributed under the terms of the GNU General Public License.
 * Please see COPYING for precise license information.
 * <p>
 * This file is part of Ancient Warfare.
 * <p>
 * Ancient Warfare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Ancient Warfare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Ancient Warfare.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

public class AmmoNapalmShot extends Ammo {

	public AmmoNapalmShot(int weight) {
		super("ammo_napalm_shot_" + weight);
		this.ammoWeight = weight;
		this.entityDamage = weight;
		this.vehicleDamage = weight;
		float scaleFactor = weight + 45.f;
		this.renderScale = (weight / scaleFactor) * 2;
		this.configName = "napalm_shot_" + weight;
		this.modelTexture = new ResourceLocation(AncientWarfareCore.modID, "textures/model/vehicle/ammo/ammo_stone_shot.png");
		this.isFlaming = true;
	}

	@Override
	public void onImpactWorld(World world, float x, float y, float z, MissileBase missile, RayTraceResult hit) {
		int bx = MathHelper.floor(x);
		int by = MathHelper.floor(y);
		int bz = MathHelper.floor(z);
		setBlockToLava(world, bx, by, bz, 5);
		double dx = missile.motionX;
		double dz = missile.motionZ;
		if (Math.abs(dx) > Math.abs(dz)) {
			dz = 0;
		} else {
			dx = 0;
		}
		dx = dx < 0 ? -1 : dx > 0 ? 1 : dx;
		dz = dz < 0 ? -1 : dz > 0 ? 1 : dz;
		if (ammoWeight >= 15)//set the 'forward' block to lava as well
		{
			setBlockToLava(world, bx + (int) dx, by, bz + (int) dz, 5);
		}
		if (ammoWeight >= 30)//set the 'rear' block to lava as well
		{
			setBlockToLava(world, bx - (int) dx, by, bz - (int) dz, 5);
		}
		if (ammoWeight >= 45) {
			if (dx == 0)//have already done Z's
			{
				setBlockToLava(world, bx + 1, by, bz, 5);
				setBlockToLava(world, bx - 1, by, bz, 5);
			} else {
				setBlockToLava(world, bx, by, bz + 1, 5);
				setBlockToLava(world, bx, by, bz - 1, 5);
			}
		}
	}

	@Override
	public void onImpactEntity(World world, Entity ent, float x, float y, float z, MissileBase missile) {
		if (!world.isRemote) {
			ent.attackEntityFrom(DamageType.causeEntityMissileDamage(missile.shooterLiving, true, false), this.getEntityDamage());
			ent.setFire(3);
			onImpactWorld(world, x, (float) ent.posY, z, missile, null);
		}
	}

}

package net.shadowmage.ancientwarfare.core.proxy;

import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

@SideOnly(Side.CLIENT)
public class ClientProxyBase extends CommonProxyBase {

	private Set<IClientRegistrar> clientRegistrars = Sets.newHashSet();

	public ClientProxyBase() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		for (IClientRegistrar registrar : clientRegistrars) {
			registrar.registerClient();
		}
	}

	@Override
	public void addClientRegistrar(IClientRegistrar registrar) {
		clientRegistrars.add(registrar);
	}

	@Override
	public void preInit() {
		super.preInit();

	}

	@Override
	public void init() {
		super.init();

	}

	@Override
	public final EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().player;
	}
}

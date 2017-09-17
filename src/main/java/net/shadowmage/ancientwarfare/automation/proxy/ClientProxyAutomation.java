package net.shadowmage.ancientwarfare.automation.proxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.shadowmage.ancientwarfare.automation.KeyHandler;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.automation.gui.GuiChunkLoaderDeluxe;
import net.shadowmage.ancientwarfare.automation.gui.GuiMailboxInventory;
import net.shadowmage.ancientwarfare.automation.gui.GuiTorqueGeneratorSterling;
import net.shadowmage.ancientwarfare.automation.gui.GuiWarehouseControl;
import net.shadowmage.ancientwarfare.automation.gui.GuiWarehouseCraftingStation;
import net.shadowmage.ancientwarfare.automation.gui.GuiWarehouseInterface;
import net.shadowmage.ancientwarfare.automation.gui.GuiWarehouseStockViewer;
import net.shadowmage.ancientwarfare.automation.gui.GuiWarehouseStorage;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteAnimalControl;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteAnimalFarm;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteAutoCrafting;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteBoundsAdjust;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteCropFarm;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteFishControl;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteFishFarm;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteInventorySideSelection;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteMushroomFarm;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteQuarry;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteReedFarm;
import net.shadowmage.ancientwarfare.automation.gui.GuiWorksiteTreeFarm;
import net.shadowmage.ancientwarfare.automation.model.ModelAutoCraftingStation;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileAutoCrafting;
import net.shadowmage.ancientwarfare.core.config.ConfigManager;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.proxy.ClientProxyBase;
import net.shadowmage.ancientwarfare.core.render.TileCraftingTableRender;

import java.util.ArrayList;
import java.util.List;

public class ClientProxyAutomation extends ClientProxyBase {

    @Override
    public void registerClient() {
        super.registerClient();

        registerClientOptions();
        MinecraftForge.EVENT_BUS.register(new KeyHandler(Minecraft.getMinecraft()));

        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_INVENTORY_SIDE_ADJUST, GuiWorksiteInventorySideSelection.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_ANIMAL_CONTROL, GuiWorksiteAnimalControl.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_AUTO_CRAFT, GuiWorksiteAutoCrafting.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_FISH_CONTROL, GuiWorksiteFishControl.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_MAILBOX_INVENTORY, GuiMailboxInventory.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WAREHOUSE_CONTROL, GuiWarehouseControl.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WAREHOUSE_STORAGE, GuiWarehouseStorage.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WAREHOUSE_OUTPUT, GuiWarehouseInterface.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WAREHOUSE_CRAFTING, GuiWarehouseCraftingStation.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_QUARRY, GuiWorksiteQuarry.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_TREE_FARM, GuiWorksiteTreeFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_CROP_FARM, GuiWorksiteCropFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_MUSHROOM_FARM, GuiWorksiteMushroomFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_ANIMAL_FARM, GuiWorksiteAnimalFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_REED_FARM, GuiWorksiteReedFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_FISH_FARM, GuiWorksiteFishFarm.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_TORQUE_GENERATOR_STERLING, GuiTorqueGeneratorSterling.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_CHUNK_LOADER_DELUXE, GuiChunkLoaderDeluxe.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WAREHOUSE_STOCK, GuiWarehouseStockViewer.class);
        NetworkHandler.registerGui(NetworkHandler.GUI_WORKSITE_BOUNDS, GuiWorksiteBoundsAdjust.class);


        //ClientRegistry.bindTileEntitySpecialRenderer(TileWorksiteBase.class, new RenderTileWorksite());
        //ClientRegistry.bindTileEntitySpecialRenderer(TileWarehouseBase.class, new RenderTileWorksite());
        //ClientRegistry.bindTileEntitySpecialRenderer(TileWarehouseStockViewer.class, new RenderTileWarehouseStockViewer());


        TileCraftingTableRender tctr = new TileCraftingTableRender(new ModelAutoCraftingStation(), "textures/model/automation/tile_auto_crafting.png");
        ClientRegistry.bindTileEntitySpecialRenderer(TileAutoCrafting.class, tctr);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.worksiteAutoCrafting), tctr);

        //********************************************CONDUIT / TRANSPORT RENDERS***************************************************************//

        //RenderTileTorqueTransport conduitRender = new RenderTileTorqueTransport(new ResourceLocation("ancientwarfare", "textures/model/automation/torque_conduit_light.png"), new ResourceLocation("ancientwarfare", "textures/model/automation/torque_conduit_medium.png"), new ResourceLocation("ancientwarfare", "textures/model/automation/torque_conduit_heavy.png"));
        //ClientRegistry.bindTileEntitySpecialRenderer(TileTorqueSidedCell.class, conduitRender);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.torqueConduit), conduitRender);

        //RenderTileTorqueTransport distributorRender = new RenderTileTorqueTransport(new ResourceLocation("ancientwarfare", "textures/model/automation/torque_distributor_light.png"), new ResourceLocation("ancientwarfare", "textures/model/automation/torque_distributor_medium.png"), new ResourceLocation("ancientwarfare", "textures/model/automation/torque_distributor_heavy.png"));
        //ClientRegistry.bindTileEntitySpecialRenderer(TileDistributor.class, distributorRender);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.torqueDistributor), distributorRender);

        //RenderTileTorqueShaft shaftRender = new RenderTileTorqueShaft(new ResourceLocation("ancientwarfare", "textures/model/automation/torque_shaft_light.png"), new ResourceLocation("ancientwarfare", "textures/model/automation/torque_shaft_medium.png"), new ResourceLocation("ancientwarfare", "textures/model/automation/torque_shaft_heavy.png"));
        //ClientRegistry.bindTileEntitySpecialRenderer(TileTorqueShaft.class, shaftRender);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.torqueShaft), shaftRender);


        //********************************************STORAGE RENDERS***************************************************************//
        //RenderTileTorqueFlywheelController storageControlRender = new RenderTileTorqueFlywheelController();
        //ClientRegistry.bindTileEntitySpecialRenderer(TileFlywheelControl.class, storageControlRender);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.flywheel), storageControlRender);

        //RenderTileFlywheelStorage storageRender = new RenderTileFlywheelStorage();
        //ClientRegistry.bindTileEntitySpecialRenderer(TileFlywheelStorage.class, storageRender);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.flywheelStorage), storageRender);

        //********************************************GENERATOR RENDERS***************************************************************//
        //RenderSterlingEngine sterlingRender = new RenderSterlingEngine();
        //ClientRegistry.bindTileEntitySpecialRenderer(TileSterlingEngine.class, sterlingRender);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.torqueGeneratorSterling), sterlingRender);

        //RenderTileWaterwheel waterwheelRender = new RenderTileWaterwheel();
        //ClientRegistry.bindTileEntitySpecialRenderer(TileWaterwheel.class, waterwheelRender);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.torqueGeneratorWaterwheel), waterwheelRender);

        //RenderTileHandEngine handGeneratorRender = new RenderTileHandEngine();
        //ClientRegistry.bindTileEntitySpecialRenderer(TileHandGenerator.class, handGeneratorRender);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.handCrankedEngine), handGeneratorRender);

        //RenderWindmillControl windmillControlRender = new RenderWindmillControl();
        //ClientRegistry.bindTileEntitySpecialRenderer(TileWindmillController.class, windmillControlRender);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.windmillControl), windmillControlRender);

        //RenderWindmillBlades bladeRender = new RenderWindmillBlades();
        //ClientRegistry.bindTileEntitySpecialRenderer(TileWindmillBlade.class, bladeRender);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AWAutomationBlockLoader.windmillBlade), bladeRender);
    }

    private void registerClientOptions() {
        ConfigManager.registerConfigCategory(new AutomationCategory("awconfig.automation_config"));
    }

    public static final class AutomationCategory extends DummyCategoryElement {
        public static final ConfigElement renderWorkBounds = new ConfigElement(AWAutomationStatics.renderWorkBounds);

        public AutomationCategory(String name) {
            super(name, name, getElementList());
        }

        private static List<IConfigElement> getElementList() {
            ArrayList<IConfigElement> list = new ArrayList<>();
            list.add(renderWorkBounds);
            return list;
        }
    }


}

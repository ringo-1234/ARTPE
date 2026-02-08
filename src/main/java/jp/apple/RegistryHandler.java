package jp.apple;

import jp.apple.block.BlockTrainPlacer;
import jp.apple.item.ItemArtpeTrain;
import jp.apple.tileentity.TileEntityTrainPlacer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import jp.apple.ARTPECore;

@Mod.EventBusSubscriber(modid = ARTPECore.MODID)
public class RegistryHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ARTPECore.trainPlacerBlock = new BlockTrainPlacer();
        event.getRegistry().register(ARTPECore.trainPlacerBlock);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(ARTPECore.trainPlacerBlock)
                .setRegistryName(ARTPECore.trainPlacerBlock.getRegistryName()));

        
        ARTPECore.itemArtpeTrain = new ItemArtpeTrain();
        event.getRegistry().register(ARTPECore.itemArtpeTrain);
    }

    @SubscribeEvent
    public static void registerModels(net.minecraftforge.client.event.ModelRegistryEvent event) {
        net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(ARTPECore.trainPlacerBlock),
                0,
                new net.minecraft.client.renderer.block.model.ModelResourceLocation(
                        ARTPECore.trainPlacerBlock.getRegistryName(), "inventory"));


        net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(
                ARTPECore.itemArtpeTrain,
                0,
                new net.minecraft.client.renderer.block.model.ModelResourceLocation(
                        ARTPECore.itemArtpeTrain.getRegistryName(), "inventory"));
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityTrainPlacer.class, "artpe:tile_train_placer");
    }
}
package jp.apple;

import jp.apple.block.BlockTrainPlacer;
import jp.apple.network.PacketFinishEditing;
import jp.apple.network.PacketPreloadModels;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = ARTPECore.MODID, name = ARTPECore.NAME, version = ARTPECore.VERSION, dependencies = "required-after:rtm")
public class ARTPECore {
    public static final String MODID = "artpe";
    public static final String NAME = "ARTPE Train Extension";
    public static final String VERSION = "1.0";
    public static SimpleNetworkWrapper network;

    @Mod.Instance(MODID)
    public static ARTPECore instance;

    public static final boolean COMPAT_MODE = false;

    public static Block trainPlacerBlock;
    public static Item itemArtpeTrain;

    public static final CreativeTabs tabARTPE = new CreativeTabs("artpe_tab") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(trainPlacerBlock);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ARTPEGuiHandler());
        RegistryHandler.registerTileEntities();

        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        
        network.registerMessage(PacketFinishEditing.Handler.class, PacketFinishEditing.class, 1, Side.SERVER);

        MinecraftForge.EVENT_BUS.register(this);
        

        if (event.getSide() == Side.CLIENT) {
            jp.apple.SoundGuard.register();
            network.registerMessage(PacketPreloadModels.Handler.class, PacketPreloadModels.class, 2, Side.CLIENT);
        }
    }
}
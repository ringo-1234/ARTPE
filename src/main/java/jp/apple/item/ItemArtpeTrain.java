package jp.apple.item;

import jp.apple.ARTPECore;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.EntityTrainDieselCar;
import jp.ngt.rtm.entity.train.util.Formation;
import jp.ngt.rtm.entity.train.util.FormationEntry;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import jp.ngt.rtm.item.ItemTrain;
import jp.ngt.rtm.item.ItemTrain.TrainSet;
import jp.ngt.rtm.modelpack.state.ResourceState;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.util.RailMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ItemArtpeTrain extends Item {
    private static final AtomicLong lastId = new AtomicLong(0);

    public ItemArtpeTrain() {
        super();
        this.setUnlocalizedName("artpe_train");
        this.setRegistryName("artpe_train");
        this.setCreativeTab(ARTPECore.tabARTPE);
        this.setMaxStackSize(1);
    }

    private long getUniqueId() {
        long now = System.currentTimeMillis();
        while (true) {
            long last = lastId.get();
            long next = Math.max(now, last + 1);
            if (lastId.compareAndSet(last, next)) {
                return next;
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (world.isRemote) return EnumActionResult.SUCCESS;

        RailMap rm0 = TileEntityLargeRailBase.getRailMapFromCoordinates(world, player, pos.getX(), pos.getY(), pos.getZ());
        if (rm0 == null) return EnumActionResult.PASS;

        int SPLIT = 128;
        int spIndex = rm0.getNearlestPoint(SPLIT, (double)pos.getX() + 0.5D, (double)pos.getZ() + 0.5D);
        float yw0 = NGTMath.wrapAngle(rm0.getRailYaw(SPLIT, spIndex));
        float yaw = EntityBogie.fixBogieYaw(-player.rotationYaw, yw0);
        float pitch = EntityBogie.fixBogiePitch(rm0.getRailPitch(SPLIT, spIndex), yw0, yaw);
        double posX = rm0.getRailPos(SPLIT, spIndex)[1];
        double posY = rm0.getRailHeight(SPLIT, spIndex);
        double posZ = rm0.getRailPos(SPLIT, spIndex)[0];

        List<TrainSet> trainSets = ItemTrain.getFormationFromItem(itemStack);
        if (trainSets.isEmpty()) return EnumActionResult.FAIL;

        long formationId = getUniqueId();
        Formation formation = new Formation(formationId, trainSets.size());
        net.minecraft.nbt.NBTTagList tagList = itemStack.getTagCompound().getTagList("formations", 10);

        for (int i = 0; i < trainSets.size(); i++) {
            TrainSet set = trainSets.get(i);
            NBTTagCompound trainNbt = tagList.getCompoundTagAt(i);
            int entryDir = trainNbt.hasKey("dir") ? trainNbt.getInteger("dir") : 0;

            Vec3 vec = PooledVec3.create(set.posX, set.posY, set.posZ).rotateAroundY(yaw);
            EntityTrainBase train = new EntityTrainDieselCar(world, "");

            
            float spawnYaw = yaw + set.yaw + (entryDir == 1 ? 180.0F : 0.0F);
            train.setPositionAndRotation(posX + vec.getX(), posY + vec.getY(), posZ + vec.getZ(), spawnYaw, pitch + set.pitch);

            train.getResourceState().setResourceName(set.modelName);
            train.setTrainStateData_NoSync(TrainStateType.Role, (byte)1);
            train.setNotch(-8);
            train.setSpeed(0.0F);

            
            train.setTrainDirection_NoSync((byte)entryDir);

            world.spawnEntity(train);

            if (train.getBogie(0) != null) train.getBogie(0).isActivated = false;
            if (train.getBogie(1) != null) train.getBogie(1).isActivated = false;

            
            FormationEntry entry = new FormationEntry(train, i, entryDir);
            formation.entries[i] = entry;
            train.setFormation(formation);

            train.updateResourceState();
        }

        try {
            java.lang.reflect.Method realloc = Formation.class.getDeclaredMethod("reallocation");
            realloc.setAccessible(true);
            realloc.invoke(formation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!player.capabilities.isCreativeMode) itemStack.shrink(1);
        return EnumActionResult.SUCCESS;
    }
}
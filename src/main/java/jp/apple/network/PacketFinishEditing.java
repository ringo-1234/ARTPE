package jp.apple.network;

import io.netty.buffer.ByteBuf;
import jp.ngt.rtm.modelpack.state.ResourceState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import jp.apple.ARTPECore;
import java.util.ArrayList;
import java.util.List;

public class PacketFinishEditing implements IMessage {
    private List<String> trainModels;
    private List<Integer> trainDirs;

    public PacketFinishEditing() {}

    public PacketFinishEditing(List<String> models, List<Integer> dirs) {
        this.trainModels = models;
        this.trainDirs = dirs;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        this.trainModels = new ArrayList<>();
        this.trainDirs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.trainModels.add(ByteBufUtils.readUTF8String(buf));
            this.trainDirs.add(buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.trainModels.size());
        for (int i = 0; i < this.trainModels.size(); i++) {
            ByteBufUtils.writeUTF8String(buf, this.trainModels.get(i));
            buf.writeInt(this.trainDirs.get(i));
        }
    }

    public static class Handler implements IMessageHandler<PacketFinishEditing, IMessage> {
        @Override
        public IMessage onMessage(PacketFinishEditing message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player == null) return null;

            player.getServerWorld().addScheduledTask(() -> {
                try {
                    
                    ItemStack resultStack = new ItemStack(ARTPECore.itemArtpeTrain, 1, 0);

                    NBTTagCompound rootTag = new NBTTagCompound();
                    NBTTagList formationListNBT = new NBTTagList();
                    String firstModel = null;

                    for (int i = 0; i < message.trainModels.size(); i++) {
                        String modelId = message.trainModels.get(i);
                        int dir = message.trainDirs.get(i);
                        if (modelId == null || modelId.isEmpty() || "未選択".equals(modelId)) continue;
                        if (firstModel == null) firstModel = modelId;

                        NBTTagCompound trainTag = new NBTTagCompound();
                        trainTag.setString("model", modelId);
                        trainTag.setInteger("index", i);
                        trainTag.setInteger("dir", dir);
                        trainTag.setFloat("pos_z", -20.0f * i);
                        trainTag.setFloat("pos_x", 0.0f);
                        trainTag.setFloat("pos_y", 0.0f);
                        trainTag.setFloat("yaw", 0.0f);
                        trainTag.setFloat("pitch", 0.0f);
                        formationListNBT.appendTag(trainTag);
                    }

                    if (firstModel == null) return;

                    rootTag.setTag("formations", formationListNBT);
                    ResourceState state = new ResourceState(jp.ngt.rtm.RTMResource.TRAIN_EC, null);
                    state.setResourceName(firstModel);
                    rootTag.setTag("State", state.writeToNBT());

                    resultStack.setTagCompound(rootTag);
                    player.inventory.addItemStackToInventory(resultStack);
                    player.inventoryContainer.detectAndSendChanges();

                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });
            return null;
        }
    }
}
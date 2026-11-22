package com.brandon3055.draconicevolution.blocks.tileentity;

import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.lib.datamanager.DataFlags;
import com.brandon3055.brandonscore.lib.datamanager.ManagedPos;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.draconicevolution.blocks.Portal;
import com.brandon3055.draconicevolution.init.DEContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Created by brandon3055 on 16/07/2016.
 */
public class TilePortal extends TileBCore {
    private final ManagedPos controllerPos = register(new ManagedPos("controller_pos", (BlockPos) null, DataFlags.SAVE_NBT_SYNC_TILE));

    private boolean hidden = false;
    private Player player = null;

    public TilePortal(BlockPos pos, BlockState state) {
        super(DEContent.TILE_PORTAL.get(), pos, state);
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos.set(worldPosition.subtract(controllerPos));
    }

    protected BlockPos getControllerPos() {
        return controllerPos.get() == null ? BlockPos.ZERO : worldPosition.subtract(controllerPos.get());
    }

    public TileDislocatorReceptacle getController() {
        BlockEntity tile = level.getBlockEntity(getControllerPos());
        return tile instanceof TileDislocatorReceptacle ? (TileDislocatorReceptacle) tile : null;
    }

    public boolean isPortalActive() {
        TileDislocatorReceptacle controller = getController();
        return controller != null && controller.isActive();
    }

    // Client Stiff

    @Override
    public void tick() {
        if (hidden && player != null) {
            BlockState state = getBlockState();
            if (!state.is(DEContent.PORTAL.get())) {
                hidden = false;
                player = null;
                level.setBlock(getBlockPos(), state.setValue(Portal.VISIBLE, true), 0, 0);
                return;
            }
            Direction.Axis axis = state.getValue(Portal.AXIS);
            Vector3 vec = Vector3.fromTileCenter(this).subtract(Vector3.fromEntity(player));
            double dist = Math.abs(axis == Direction.Axis.X ? vec.x : axis == Direction.Axis.Y ? vec.y : vec.z);
            if (dist > 1.5) {
                hidden = false;
                player = null;
                level.setBlock(getBlockPos(), state.setValue(Portal.VISIBLE, true), 0, 0);
            }
            return;
        }
        // port-26.1.2: клиентские партиклы вынесены в DEClientHooks (dist-clean)
        com.brandon3055.draconicevolution.client.DEClientHooks.portalParticles(this);
    }

    public void clientArrived(Player player) {
        BlockState state = getBlockState();
        if (state.is(DEContent.PORTAL.get()) && level != null) {
            level.setBlock(getBlockPos(), state.setValue(Portal.VISIBLE, false), 0, 0);
            hidden = true;
            this.player = player;
        }
    }
}

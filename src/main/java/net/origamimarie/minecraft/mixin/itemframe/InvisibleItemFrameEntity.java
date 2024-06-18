package net.origamimarie.minecraft.mixin.itemframe;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;


// Note to future Marie:  you may not need this
// You might be able to override something like getBoundingBox() to interact with visibility





// Mostly lifted from decompiled code provided by FernFlower decompiler
public class InvisibleItemFrameEntity extends ItemFrameEntity {

    public InvisibleItemFrameEntity(ItemFrameEntity original) {
        super((EntityType<? extends ItemFrameEntity>) original.getType(),
                original.getWorld(),
                original.getBlockPos(),
                ((AbstractDecorationEntityAccessor) original).getFacing());
    }

    public InvisibleItemFrameEntity(EntityType<? extends InvisibleItemFrameEntity> entityType, World world) {
        super(entityType, world);
    }

    public InvisibleItemFrameEntity(World world, BlockPos pos, Direction facing) {
        super(world, pos, facing);
    }

    public InvisibleItemFrameEntity(EntityType<? extends InvisibleItemFrameEntity> type, World world, BlockPos pos, Direction facing) {
        super(type, world, pos, facing);
    }

}

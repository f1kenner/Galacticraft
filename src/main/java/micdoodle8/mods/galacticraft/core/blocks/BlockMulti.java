package micdoodle8.mods.galacticraft.core.blocks;

import micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock;
import micdoodle8.mods.galacticraft.core.tile.TileEntityMulti;
import micdoodle8.mods.galacticraft.planets.mars.blocks.BlockMachineMars;
import micdoodle8.mods.galacticraft.planets.mars.blocks.MarsBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class BlockMulti extends BlockContainer implements IPartialSealableBlock, ITileEntityProvider
{
    public static final PropertyEnum MULTI_TYPE = PropertyEnum.create("type", EnumBlockMultiType.class);
    public static final PropertyInteger RENDER_TYPE = PropertyInteger.create("renderType", 0, 7);

    public enum EnumBlockMultiType implements IStringSerializable
    {
        SOLAR_PANEL_0(0, "solar"),
        SPACE_STATION_BASE(1, "ss_base"),
        ROCKET_PAD(2, "rocket_pad"),
        NASA_WORKBENCH(3, "nasa_workbench"),
        SOLAR_PANEL_1(4, "solar_panel"),
        CRYO_CHAMBER(5, "cryo_chamber"),
        BUGGY_FUEL_PAD(6, "buggy_pad"),
        MINER_BASE(7, "miner_base");

        private final int meta;
        private final String name;

        private EnumBlockMultiType(int meta, String name)
        {
            this.meta = meta;
            this.name = name;
        }

        public int getMeta()
        {
            return this.meta;
        }

        public static EnumBlockMultiType byMetadata(int meta)
        {
            return values()[meta];
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    public BlockMulti(String assetName)
    {
        super(GCBlocks.machine);
        this.setHardness(1.0F);
        this.setStepSound(Block.soundTypeMetal);
        this.setUnlocalizedName(assetName);
        this.setResistance(1000000000000000.0F);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        int meta = getMetaFromState(worldIn.getBlockState(pos));

        if (meta == 2 || meta == 6)
        {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
        }
        else if (meta == 0 || meta == 4)
        {
            this.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, worldIn.getBlockState(pos.up()).getBlock() == this ? 1.0F : 0.6F, 0.7F);
        }
        else
        {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity)
    {
        int meta = getMetaFromState(state);

        if (meta == 2 || meta == 6)
        {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
            super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        }
        else if (meta == 0 || meta == 4)
        {
            this.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, worldIn.getBlockState(pos.up()).getBlock() == this ? 1.0F : 0.6F, 0.7F);
            super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        }
        /*else if (meta == 7)
        {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.38F, 1.0F);
            super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
        }*/
        else
        {
            super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        this.setBlockBoundsBasedOnState(worldIn, pos);
        return super.getCollisionBoundingBox(worldIn, pos, state);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos)
    {
        this.setBlockBoundsBasedOnState(worldIn, pos);
        return super.getSelectedBoundingBox(worldIn, pos);
    }

    @Override
    public boolean canDropFromExplosion(Explosion par1Explosion)
    {
        return false;
    }

    public void makeFakeBlock(World worldObj, BlockPos pos, BlockPos mainBlock, int meta)
    {
        worldObj.setBlockState(pos, GCBlocks.fakeBlock.getStateFromMeta(meta), 3);
        worldObj.getTileEntity(pos).setWorldObj(worldObj);
        ((TileEntityMulti) worldObj.getTileEntity(pos)).setMainBlock(mainBlock);
    }

    @Override
    public float getBlockHardness(World worldIn, BlockPos pos)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileEntityMulti)
        {
            BlockPos mainBlockPosition = ((TileEntityMulti) tileEntity).mainBlockPosition;

            if (mainBlockPosition != null)
            {
                return worldIn.getBlockState(mainBlockPosition).getBlock().getBlockHardness(worldIn, pos);
            }
        }

        return this.blockHardness;
    }

    @Override
    public boolean isSealed(World worldIn, BlockPos pos, EnumFacing direction)
    {
        int metadata = getMetaFromState(worldIn.getBlockState(pos));

        //Landing pad and refueling pad
        if (metadata == 2 || metadata == 6)
        {
            return direction == EnumFacing.DOWN;
        }

        //Basic solar panel fixed top
        if (metadata == 4)
        {
            return direction == EnumFacing.UP;
        }

        return false;
    }

    /*@Override
    public Block setBlockTextureName(String name)
    {
        this.textureName = name;
        return this;
    }*/

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileEntityMulti)
        {
            ((TileEntityMulti) tileEntity).onBlockRemoval();
        }

        super.breakBlock(worldIn, pos, state);
    }

    /**
     * Called when the block is right clicked by the player. This modified
     * version detects electric items and wrench actions on your machine block.
     * Do not override this function. Use machineActivated instead! (It does the
     * same thing)
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntityMulti tileEntity = (TileEntityMulti) worldIn.getTileEntity(pos);
        return tileEntity.onBlockActivated(worldIn, pos, playerIn);
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    @Override
    public int getRenderType()
    {
        return 3;
    }

    @Override
    public boolean isFullCube()
    {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileEntityMulti();
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityMulti)
        {
            BlockPos mainBlockPosition = ((TileEntityMulti) tileEntity).mainBlockPosition;

            if (mainBlockPosition != null)
	        {
	            Block mainBlockID = world.getBlockState(mainBlockPosition).getBlock();
	
	            if (Blocks.air != mainBlockID)
	            {
	                return mainBlockID.getPickBlock(target, world, mainBlockPosition);
	            }
	        }
        }

        return null;
    }

    @Override
    public EnumFacing getBedDirection(IBlockAccess world, BlockPos pos)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityMulti)
        {
            BlockPos mainBlockPosition = ((TileEntityMulti) tileEntity).mainBlockPosition;

	        if (mainBlockPosition != null)
	        {
	            return world.getBlockState(mainBlockPosition).getBlock().getBedDirection(world, mainBlockPosition);
	        }
        }

        return EnumFacing.UP; // TODO
    }

    @Override
    public boolean isBed(IBlockAccess world, BlockPos pos, Entity player)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityMulti)
        {
            BlockPos mainBlockPosition = ((TileEntityMulti) tileEntity).mainBlockPosition;

            if (mainBlockPosition != null)
	        {
	            return world.getBlockState(mainBlockPosition).getBlock().isBed(world, mainBlockPosition, player);
	        }
        }

        return super.isBed(world, pos, player);
    }

    @Override
    public void setBedOccupied(IBlockAccess world, BlockPos pos, EntityPlayer player, boolean occupied)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        BlockPos mainBlockPosition = ((TileEntityMulti) tileEntity).mainBlockPosition;

        if (mainBlockPosition != null)
        {
            world.getBlockState(mainBlockPosition).getBlock().setBedOccupied(world, mainBlockPosition, player, occupied);
        }
        else
        {
            super.setBedOccupied(world, pos, player, occupied);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer)
    {
        TileEntity tileEntity = worldObj.getTileEntity(target.getBlockPos());

        if (tileEntity instanceof TileEntityMulti)
        {
            BlockPos mainBlockPosition = ((TileEntityMulti) tileEntity).mainBlockPosition;

            if (mainBlockPosition != null)
            {
                effectRenderer.addBlockHitEffects(mainBlockPosition, target);
            }
        }

        return super.addHitEffects(worldObj, target, effectRenderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, net.minecraft.client.particle.EffectRenderer effectRenderer)
    {
        return super.addDestroyEffects(world, pos, effectRenderer);
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(MULTI_TYPE, EnumBlockMultiType.byMetadata(meta));
    }

    public int getMetaFromState(IBlockState state)
    {
        return ((EnumBlockMultiType)state.getValue(MULTI_TYPE)).getMeta();
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, MULTI_TYPE, RENDER_TYPE);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        EnumBlockMultiType type = (EnumBlockMultiType)state.getValue(MULTI_TYPE);
        int renderType = 0;

        switch (type)
        {
        case CRYO_CHAMBER:
            IBlockState stateAbove = worldIn.getBlockState(pos.up());
            TileEntityMulti tile = (TileEntityMulti) worldIn.getTileEntity(pos);
            if (stateAbove.getBlock() == this && (stateAbove.getValue(MULTI_TYPE)) == EnumBlockMultiType.CRYO_CHAMBER)
            {
                renderType = 0;
            }
            else
            {
                renderType = 4;
            }
            if (tile != null && tile.mainBlockPosition != null)
            {
                IBlockState stateMain = worldIn.getBlockState(tile.mainBlockPosition);
                if (stateMain.getBlock() == MarsBlocks.machine && stateMain.getValue(BlockMachineMars.TYPE) == BlockMachineMars.EnumMachineType.CRYOGENIC_CHAMBER)
                {
                    EnumFacing dir = stateMain.getValue(BlockMachineMars.FACING);
                    renderType += dir.getHorizontalIndex();
                }
            }
            break;
        default:
            break;
        }

        return state.withProperty(RENDER_TYPE, renderType);
    }
}

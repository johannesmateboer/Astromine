/*
 * MIT License
 *
 * Copyright (c) 2020 Chainmail Studios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.chainmailstudios.astromine.transportations.common.block;

import com.github.chainmailstudios.astromine.common.utilities.VoxelShapeUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import com.github.chainmailstudios.astromine.common.utilities.RotationUtilities;
import com.github.chainmailstudios.astromine.transportations.common.block.property.ConveyorProperties;

public class CatwalkStairsBlock extends HorizontalFacingBlock implements Waterloggable {
	private static final VoxelShape FIRST_STEP = VoxelShapes.cuboid(0, 0, (12F / 16F), 1, (3F / 16F), 1);
	private static final VoxelShape SECOND_STEP = VoxelShapes.cuboid(0, 0, (8F / 16F), 1, (7F / 16F), (12F / 16F));
	private static final VoxelShape THIRD_STEP = VoxelShapes.cuboid(0, 0, (4F / 16F), 1, (11F / 16F), (8F / 16F));
	private static final VoxelShape FOURTH_STEP = VoxelShapes.cuboid(0, 0, 0, 1, (15F / 16F), (4F / 16F));

	private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[6];

	public CatwalkStairsBlock(Settings settings) {
		super(settings);

		this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(ConveyorProperties.LEFT, false).with(ConveyorProperties.RIGHT, false).with(Properties.WATERLOGGED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, ConveyorProperties.LEFT, ConveyorProperties.RIGHT, Properties.WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) ? Fluids.WATER.getDefaultState() : super.getFluidState(state);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return this.getDefaultState().with(FACING, context.getPlayer().isSneaking() ? context.getPlayerFacing().getOpposite() : context.getPlayerFacing()).with(Properties.WATERLOGGED, context.getWorld().getBlockState(context.getBlockPos()).getBlock() == Blocks.WATER);
	}

	public boolean isAdjacentBlockOfMyType(WorldAccess world, BlockPos position, Direction direction) {
		BlockPos newPosition = position.offset(direction);

		BlockState blockState = world.getBlockState(newPosition);

		Block block = (null == blockState) ? null : blockState.getBlock();

		return this == block;
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		return state.with(ConveyorProperties.RIGHT, this.isAdjacentBlockOfMyType(world, pos, state.get(FACING).rotateYClockwise())).with(ConveyorProperties.LEFT, this.isAdjacentBlockOfMyType(world, pos, state.get(FACING).rotateYCounterclockwise()));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext entityContext) {
		Direction facing = state.get(FACING);

		if (SHAPE_CACHE[facing.getId()] == null) {
			SHAPE_CACHE[facing.getId()] = VoxelShapes.union(VoxelShapeUtilities.rotate(facing, FIRST_STEP), VoxelShapeUtilities.rotate(facing, SECOND_STEP), VoxelShapeUtilities.rotate(facing, THIRD_STEP), VoxelShapeUtilities.rotate(facing, FOURTH_STEP));
		}

		return SHAPE_CACHE[facing.getId()];
	}
}

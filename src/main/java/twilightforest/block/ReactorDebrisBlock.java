package twilightforest.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.entity.ReactorDebrisBlockEntity;

public class ReactorDebrisBlock extends BaseEntityBlock {

	public VoxelShape SHAPE;
	public static final MapCodec<ReactorDebrisBlock> CODEC = simpleCodec(ReactorDebrisBlock::new);

	public ReactorDebrisBlock(Properties properties) {
		super(properties);
		SHAPE = Shapes.empty();
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (level.getBlockEntity(pos) instanceof ReactorDebrisBlockEntity reactorDebrisBlockEntity)
			return reactorDebrisBlockEntity.SHAPE;
		return ReactorDebrisBlockEntity.calculateVoxelShape();
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		//schedule this block to be removed 3 seconds after placement if not removed before then
		if(!level.isClientSide()) {
			ReactorDebrisBlockEntity blockEntity = (ReactorDebrisBlockEntity) level.getBlockEntity(pos);
			blockEntity.randomizeDimensions();
			blockEntity.randomizeTextures();
		}
//		level.scheduleTick(pos, this, 60);;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getBlock() == this)
			level.destroyBlock(pos, false);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new ReactorDebrisBlockEntity(blockPos, blockState);
	}
}

package twilightforest.structures.minotaurmaze;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.structure.StructureComponent;
import twilightforest.block.BlockTFMazestone;
import twilightforest.block.TFBlocks;

import static twilightforest.block.enums.MazestoneVariant.BRICK;
import static twilightforest.block.enums.MazestoneVariant.CRACKED;
import static twilightforest.block.enums.MazestoneVariant.MOSSY;

public class StructureTFMazeStones extends StructureComponent.BlockSelector {

	@Override
	public void selectBlocks(Random par1Random, int par2, int par3, int par4, boolean wall) {
        if (!wall)
        {
            this.blockstate = Blocks.AIR.getDefaultState();
        }
        else
        {
            this.blockstate = TFBlocks.mazestone.getDefaultState();
            float rf = par1Random.nextFloat();

            if (rf < 0.2F)
            {
                this.blockstate = blockstate.withProperty(BlockTFMazestone.VARIANT, MOSSY);
            }
            else if (rf < 0.5F)
            {
                this.blockstate = blockstate.withProperty(BlockTFMazestone.VARIANT, CRACKED);
            }
            else
            {
                this.blockstate = blockstate.withProperty(BlockTFMazestone.VARIANT, BRICK);
            }
        }
	}

}

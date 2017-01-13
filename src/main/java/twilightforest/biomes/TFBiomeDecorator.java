package twilightforest.biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.world.TFGenCanopyMushroom;
import twilightforest.world.TFGenCanopyTree;
import twilightforest.world.TFGenFallenHollowLog;
import twilightforest.world.TFGenFallenSmallLog;
import twilightforest.world.TFGenFoundation;
import twilightforest.world.TFGenGroveRuins;
import twilightforest.world.TFGenTorchBerries;
import twilightforest.world.TFGenHollowStump;
import twilightforest.world.TFGenHollowTree;
import twilightforest.world.TFGenMangroveTree;
import twilightforest.world.TFGenMonolith;
import twilightforest.world.TFGenMyceliumBlob;
import twilightforest.world.TFGenOutsideStalagmite;
import twilightforest.world.TFGenPlantRoots;
import twilightforest.world.TFGenStoneCircle;
import twilightforest.world.TFGenWell;
import twilightforest.world.TFGenWitchHut;
import twilightforest.world.TFGenWoodRoots;
import twilightforest.world.TFGenerator;
import twilightforest.world.TFTreeGenerator;

public class TFBiomeDecorator extends BiomeDecorator {
	
	TFGenCanopyTree canopyTreeGen = new TFGenCanopyTree();
	TFTreeGenerator alternateCanopyGen = new TFGenCanopyMushroom();
	private TFGenHollowTree hollowTreeGen = new TFGenHollowTree();
	private TFGenMyceliumBlob myceliumBlobGen = new TFGenMyceliumBlob(5);
	private WorldGenLakes extraLakeGen = new WorldGenLakes(Blocks.WATER);
	private WorldGenLakes extraLavaPoolGen = new WorldGenLakes(Blocks.LAVA);
	private TFGenMangroveTree mangroveTreeGen = new TFGenMangroveTree();

	private TFGenPlantRoots plantRootGen = new TFGenPlantRoots();
	private TFGenWoodRoots woodRootGen = new TFGenWoodRoots();
	private WorldGenLiquids caveWaterGen = new WorldGenLiquids(Blocks.FLOWING_WATER);
	private TFGenTorchBerries torchBerryGen = new TFGenTorchBerries();

    public float canopyPerChunk = TwilightForestMod.canopyCoverage;
    public float alternateCanopyChance = 0;
    public int myceliumPerChunk = 0;
    public int mangrovesPerChunk = 0;
    public int lakesPerChunk = 0;
    public float lavaPoolChance = 0;
    
    private static final List<RuinEntry> ruinList = new ArrayList<RuinEntry>();
    static {
    	// make list of ruins
    	ruinList.add(new RuinEntry(new TFGenStoneCircle(), 10));
    	ruinList.add(new RuinEntry(new TFGenWell(), 10));
    	ruinList.add(new RuinEntry(new TFGenWitchHut(), 5));
    	ruinList.add(new RuinEntry(new TFGenOutsideStalagmite(), 12));
    	ruinList.add(new RuinEntry(new TFGenFoundation(), 10));
    	ruinList.add(new RuinEntry(new TFGenMonolith(), 10));
    	ruinList.add(new RuinEntry(new TFGenGroveRuins(), 5));
    	ruinList.add(new RuinEntry(new TFGenHollowStump(), 12));
    	ruinList.add(new RuinEntry(new TFGenFallenHollowLog(), 10));
    	ruinList.add(new RuinEntry(new TFGenFallenSmallLog(), 10));
    }
    
    private static class RuinEntry extends WeightedRandom.Item
    {
        public final TFGenerator generator;
        public RuinEntry(TFGenerator generator, int weight)
        {
            super(weight);
            this.generator = generator;
        }
    }

	@Override
    public void decorate(World world, Random rand, Biome biome, BlockPos pos)
    {

    	// check for features
    	TFFeature nearFeature = TFFeature.getNearestFeature(pos.getX() >> 4, pos.getZ() >> 4, world);

    	if (!nearFeature.areChunkDecorationsEnabled) {
        	// no normal decorations here, these parts supply their own decorations.
            decorateUnderground(world, rand, pos);
            decorateOnlyOres(world, rand, pos);
    	} else {
//    		// hollow trees!
//	    	if (rand.nextInt(24) == 0) {
//		        int rx = mapX + rand.nextInt(16) + 8;
//		        int rz = mapZ + rand.nextInt(16) + 8;
//		        int ry = world.getHeightValue(rx, rz);
//	    		hollowTreeGen.generate(world, rand, rx, ry, rz);
//	    	}
	    	
	    	// regular decorations
	    	super.decorate(world, rand, biome, pos);
    	}
    }
    
	@Override
    protected void genDecorations(Biome biome, World world, Random randomGenerator)
    {
    	// random features!
        if(randomGenerator.nextInt(6) == 0)
        {
            int rx = chunkPos.getX() + randomGenerator.nextInt(16) + 8;
            int rz = chunkPos.getZ() + randomGenerator.nextInt(16) + 8;
            TFGenerator rf = randomFeature(randomGenerator);
            if (rf.generate(world, randomGenerator, world.getHeight(new BlockPos(rx, 0, rz))))
            {
//            	System.out.println(rf + " success at " + rx + ", " + ry + ", " + rz);
            }
        }

		// add canopy trees
    	int nc = (int)canopyPerChunk + ((randomGenerator.nextFloat() < (canopyPerChunk - (int)canopyPerChunk)) ? 1 : 0);
    	for (int i = 0; i < nc; i++) {
    		int rx = chunkPos.getX() + randomGenerator.nextInt(16) + 8;
    		int rz = chunkPos.getZ() + randomGenerator.nextInt(16) + 8;
			BlockPos genPos = world.getHeight(new BlockPos(rx, 0, rz));
    		if (this.alternateCanopyChance > 0 && randomGenerator.nextFloat() <= alternateCanopyChance) {
    			alternateCanopyGen.generate(world, randomGenerator, genPos);
    		} else {
    			canopyTreeGen.generate(world, randomGenerator, genPos);
	        }
    	}

    	// mangrove trees
    	for (int i = 0; i < mangrovesPerChunk; i++) {
	        int rx = chunkPos.getX() + randomGenerator.nextInt(16) + 8;
	        int rz = chunkPos.getZ() + randomGenerator.nextInt(16) + 8;
	        mangroveTreeGen.generate(world, randomGenerator, world.getHeight(new BlockPos(rx, 0, rz)));
    	}
    	// add extra lakes for swamps
    	for (int i = 0; i < lakesPerChunk; i++) {
	        int rx = chunkPos.getX() + randomGenerator.nextInt(16) + 8;
	        int rz = chunkPos.getZ() + randomGenerator.nextInt(16) + 8;
	        extraLakeGen.generate(world, randomGenerator, world.getHeight(new BlockPos(rx, 0, rz)));
    	}
    	
    	// add extra lava for fire swamps
    	if (randomGenerator.nextFloat() <= lavaPoolChance) {
	        int rx = chunkPos.getX() + randomGenerator.nextInt(16) + 8;
	        int rz = chunkPos.getZ() + randomGenerator.nextInt(16) + 8;
	        extraLavaPoolGen.generate(world, randomGenerator, world.getHeight(new BlockPos(rx, 0, rz)));
    	}
    	
    	// mycelium blobs
    	for (int i = 0; i < myceliumPerChunk; i++) {
	        int rx = chunkPos.getX() + randomGenerator.nextInt(16) + 8;
	        int rz = chunkPos.getZ() + randomGenerator.nextInt(16) + 8;
	        myceliumBlobGen.generate(world, randomGenerator, world.getHeight(new BlockPos(rx, 0, rz)));
    	}
    	
        super.genDecorations(biome, world, randomGenerator);
        
        decorateUnderground(world, randomGenerator, chunkPos);
        

    }

    /**
     * Generate the Twilight Forest underground decorations
     */
	protected void decorateUnderground(World world, Random rand, BlockPos pos) {
		// generate roots
		for (int i = 0; i < 12; ++i)
		{
		    int rx = pos.getX() + rand.nextInt(16) + 8;
		    byte ry = 64;
		    int rz = pos.getZ() + rand.nextInt(16) + 8;
		    plantRootGen.generate(world, rand, new BlockPos(rx, ry, rz));
		}

		// generate roots
		for (int i = 0; i < 20; ++i)
		{
		    int rx = pos.getX() + rand.nextInt(16) + 8;
		    int ry = rand.nextInt(64);
		    int rz = pos.getZ() + rand.nextInt(16) + 8;
		    woodRootGen.generate(world, rand, new BlockPos(rx, ry, rz));
		}
		
		// extra underground water sources
		if (this.generateLakes) {
			for (int i = 0; i < 50; ++i)
			{
				int rx = pos.getX() + rand.nextInt(16) + 8;
				int ry = rand.nextInt(24) + 4;
				int rz = pos.getZ() + rand.nextInt(16) + 8;
				caveWaterGen.generate(world, rand, new BlockPos(rx, ry, rz));
			}
		}
		
        // torch berries are almost guaranteed to spawn so we don't need many
        for (int i = 0; i < 3; ++i)
        {
            int rx = pos.getX() + rand.nextInt(16) + 8;
            int ry = 64;
            int rz = pos.getZ() + rand.nextInt(16) + 8;
            torchBerryGen.generate(world, rand, new BlockPos(rx, ry, rz));
        }
	}
	
	/**
	 * Generates ores only
	 */
	public void decorateOnlyOres(World world, Random rand, BlockPos pos) {
		this.chunkPos = pos;
        this.generateOres(world, rand);
	}

    /**
     * Gets a random feature suitible to the current biome.
     */
    public TFGenerator randomFeature(Random rand)
    {
    	return WeightedRandom.getRandomItem(rand, ruinList).generator;
    }
    
    public void setTreesPerChunk(int treesPerChunk) {
    	this.treesPerChunk = treesPerChunk;
    }
    
    public void setBigMushroomsPerChunk(int bigMushroomsPerChunk) {
    	this.bigMushroomsPerChunk = bigMushroomsPerChunk;
    }
    
    public void setClayPerChunk(int clayPerChunk) {
    	this.clayPerChunk = clayPerChunk;
    }
    
    public void setDeadBushPerChunk(int deadBushPerChunk) {
    	this.deadBushPerChunk = deadBushPerChunk;
    }
    
    public void setMushroomsPerChunk(int mushroomsPerChunk) {
    	this.mushroomsPerChunk = mushroomsPerChunk;
    }
    
    public void setFlowersPerChunk(int flowersPerChunk) {
    	this.flowersPerChunk = flowersPerChunk;
    }
    
    public void setReedsPerChunk(int reedsPerChunk) {
    	this.reedsPerChunk = reedsPerChunk;
    }
    
    public void setWaterlilyPerChunk(int waterlilyPerChunk) {
    	this.waterlilyPerChunk = waterlilyPerChunk;
    }
    
    public void setGrassPerChunk(int grassPerChunk) {
    	this.grassPerChunk = grassPerChunk;
    }
    
    
}

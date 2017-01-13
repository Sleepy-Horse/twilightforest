package twilightforest.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;


public class EntityTFSwarmSpider extends EntitySpider {
	
	protected boolean shouldSpawn = false;
	
	public EntityTFSwarmSpider(World world) {
		this(world, true);
	}
	
	public EntityTFSwarmSpider(World world, boolean spawnMore) {
		super(world);
		
		setSize(0.8F, 0.4F);
		setSpawnMore(spawnMore);
		//texture = TwilightForestMod.MODEL_DIR + "swarmspider.png";
		experienceValue = 2; // XP value
	}
	
    public EntityTFSwarmSpider(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }

	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
    }

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		// todo 1.9 need to replace player target task with the normal one that doesnt turn docile in light
	}

	@Override
	public float getRenderSizeModifier() {
		 return 0.5F;
	}

	@Override
	public void onUpdate() {
		if (shouldSpawnMore()) {
			if (!worldObj.isRemote) {
				int more = 1 + rand.nextInt(2);
				for (int i = 0; i < more; i++) {
					// try twice to spawn
					if (!spawnAnother()) {
						spawnAnother();
					}
				}
			}
			setSpawnMore(false);
		}

		super.onUpdate();
	}

	@Override
    public boolean attackEntityAsMob(Entity entity)
    {
		return rand.nextInt(4) == 0 && super.attackEntityAsMob(entity);
    }

	protected boolean spawnAnother() {
		EntityTFSwarmSpider another = new EntityTFSwarmSpider(worldObj, false);


		double sx = posX + (rand.nextBoolean() ? 0.9 : -0.9);
		double sy = posY;
		double sz = posZ + (rand.nextBoolean() ? 0.9 : -0.9);
		another.setLocationAndAngles(sx, sy, sz, rand.nextFloat() * 360F, 0.0F);
		if(!another.getCanSpawnHere())
		{
			another.setDead();
			return false;
		}
		worldObj.spawnEntityInWorld(another);
		
		return true;
	}

	@Override
    protected boolean isValidLightLevel()
    {
		int chunkX = MathHelper.floor(posX) >> 4;
		int chunkZ = MathHelper.floor(posZ) >> 4;
		// We're allowed to spawn in bright light only in hedge mazes.
		if (TFFeature.getNearestFeature(chunkX, chunkZ, worldObj) == TFFeature.hedgeMaze) 
		{
			return true;
		}
		else
		{
			return super.isValidLightLevel();
		}
    }
	
    public boolean shouldSpawnMore()
    {
    	return shouldSpawn;
    }

    public void setSpawnMore(boolean flag)
    {
    	this.shouldSpawn = flag;
    }

    @Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setBoolean("SpawnMore", shouldSpawnMore());
    }

    @Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        setSpawnMore(nbttagcompound.getBoolean("SpawnMore"));
    }

	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		if (par1DamageSource.getSourceOfDamage() instanceof EntityPlayer) {
			((EntityPlayer)par1DamageSource.getSourceOfDamage()).addStat(TFAchievementPage.twilightHunter);
			// are we in a hedge maze?
			int chunkX = MathHelper.floor(posX) >> 4;
			int chunkZ = MathHelper.floor(posZ) >> 4;
			if (TFFeature.getNearestFeature(chunkX, chunkZ, worldObj) == TFFeature.hedgeMaze) {
				// award hedge maze cheevo
				((EntityPlayer)par1DamageSource.getSourceOfDamage()).addStat(TFAchievementPage.twilightHedge);
			}
		}
	}

    @Override
	protected float getSoundPitch()
    {
        return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F;
    }

	@Override
    public int getMaxSpawnedInChunk()
    {
        return 16;
    }

}

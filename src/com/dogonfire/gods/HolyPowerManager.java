package com.dogonfire.gods;

import com.dogonfire.gods.tasks.BoostKnowledgeTask;
import com.dogonfire.gods.tasks.CallMoonTask;
import com.dogonfire.gods.tasks.CallSunTask;
import com.dogonfire.gods.tasks.DrunkTask;
import com.dogonfire.gods.tasks.FireworkTask;
import com.dogonfire.gods.tasks.HealRadiusTask;
import com.dogonfire.gods.tasks.ThunderStormTask;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Tameable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class HolyPowerManager
{
	private Gods plugin;

	static enum HolyPower
	{
		KNOWLEDGE, CALLMOON, CALLSUN, HEALING, TAME, LIGHTNING, LIGHTNING_STORM, TELEPORT, FREEZE, ICE, FIREBALL, EARTHQUAKE, NATURE, FIREWORK, DRUNK;
	}

	private Random random = new Random();

	HolyPowerManager(Gods p)
	{
		this.plugin = p;
	}

	public String describe(HolyPower power, int value)
	{
		String descriptionText = "";
		switch (power)
		{
		case CALLMOON:
			descriptionText = "Boosts " + value + " levels of xp";
			break;
		case EARTHQUAKE:
			descriptionText = "Heals up to " + value + " other players";
			break;
		case CALLSUN:
			descriptionText = "Calls the moon for " + value + " min";
			break;
		case DRUNK:
			descriptionText = "Calls the sun for " + value + " min";
			break;
		case TAME:
			descriptionText = "Shoots " + value + " firework rockets";
			break;
		case NATURE:
			descriptionText = "Grows nature in a radius of " + value + " blocks";
			break;
		case LIGHTNING_STORM:
			descriptionText = "Earthquake in " + value + " block radius";
			break;
		case FIREBALL:
			descriptionText = "Tame all beasts within " + 2 * value + " blocks";
			break;
		case FIREWORK:
			descriptionText = "Lightning strikes up to " + value + " enemies";
			break;
		case FREEZE:
			descriptionText = "Creates a thunderstorm lasting " + value + " minutes";
			break;
		case HEALING:
			descriptionText = "Teleport up to " + value + " blocks";
			break;
		case KNOWLEDGE:
			descriptionText = "Shoots ice spike which freezes a target on hit";
			break;
		case ICE:
			descriptionText = "Freezes all water within a " + value + " block radius";
			break;
		case LIGHTNING:
			descriptionText = "Fires a fireball with a " + value + " seconds cooldown";
			break;
		case TELEPORT:
			descriptionText = "Gets you drunk for " + value + " seconds!";
		}
		return descriptionText;
	}

	public HolyPower getHolyPowerFromDescription(String description)
	{
		HolyPower holyPower = null;
		String[] text = description.split(" ");

		this.plugin.logDebug(description);
		if (text[0].equals("Boosts"))
		{
			holyPower = HolyPower.KNOWLEDGE;
		}
		else if (text[0].equals("Heals"))
		{
			holyPower = HolyPower.HEALING;
		}
		else if ((text[0].equals("Shoots")) && (text[1].equals("1")))
		{
			holyPower = HolyPower.FIREWORK;
		}
		else if (text[0].equals("Gets"))
		{
			holyPower = HolyPower.DRUNK;
		}
		else if (text[0].equals("Freezes"))
		{
			holyPower = HolyPower.FREEZE;
		}
		else if ((text[0].equals("Shoots")) && (text[1].equals("ice")))
		{
			holyPower = HolyPower.ICE;
		}
		else if ((text[0].equals("Fires")) && (text[1].equals("a")))
		{
			holyPower = HolyPower.FIREBALL;
		}
		return holyPower;
	}

	public Entity[] getNearbyEntities(Location l, double radius)
	{
		int iRadius = (int) radius;
		int chunkRadius = iRadius < 16 ? 1 : (iRadius - iRadius % 16) / 16;
		HashSet radiusEntities = new HashSet();
		for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++)
		{
			for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++)
			{
				int x = (int) l.getX();
				int y = (int) l.getY();
				int z = (int) l.getZ();
				for (Entity e : new Location(l.getWorld(), x + chX * 16, y, z + chZ * 16).getChunk().getEntities())
				{
					if ((e.getLocation().distance(l) <= radius) && (e.getLocation().getBlock() != l.getBlock()))
					{
						radiusEntities.add(e);
					}
				}
			}
		}
		return (Entity[]) radiusEntities.toArray(new Entity[radiusEntities.size()]);
	}

	public Entity[] getNearbyLivingEntities(Location l, double radius)
	{
		int iRadius = (int) radius;
		int chunkRadius = iRadius < 16 ? 1 : (iRadius - iRadius % 16) / 16;
		HashSet radiusEntities = new HashSet();
		for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++)
		{
			for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++)
			{
				int x = (int) l.getX();
				int y = (int) l.getY();
				int z = (int) l.getZ();
				for (Entity e : new Location(l.getWorld(), x + chX * 16, y, z + chZ * 16).getChunk().getEntities())
				{
					if ((e instanceof LivingEntity))
					{
						if ((e.getLocation().distance(l) <= radius) && (e.getLocation().getBlock() != l.getBlock()))
						{
							radiusEntities.add(e);
						}
					}
				}
			}
		}
		return (Entity[]) radiusEntities.toArray(new Entity[radiusEntities.size()]);
	}

	private boolean checkBlock(double x1, double y1, double z1, double w1, double h1, double d1, double x2, double y2, double z2, double w2, double h2, double d2)
	{
		if (x1 + w1 < x2)
		{
			return false;
		}
		if (x2 + w2 < x1)
		{
			return false;
		}
		if (y1 + h1 < y2)
		{
			return false;
		}
		if (y2 + h2 < y1)
		{
			return false;
		}
		if (z1 + d1 < z2)
		{
			return false;
		}
		if (z2 + d2 < z1)
		{
			return false;
		}
		return true;
	}

	public void activatePower(Player player, HolyPower powerType, int powerStrength)
	{
		switch (powerType)
		{
		case EARTHQUAKE:
			healing(player, powerStrength);
			break;
		case CALLSUN:
			callMoon(player, powerStrength);
			break;
		case DRUNK:
			callSun(player, powerStrength);
			break;
		case CALLMOON:
			boostKnowledge(player, powerStrength);
			break;
		case TAME:
			shootFirework(player, powerStrength);
			break;
		case NATURE:
			growNature(player, powerStrength);
			break;
		case LIGHTNING_STORM:
			earthQuake(player, powerStrength);
			break;
		case HEALING:
			teleport(player, powerStrength);
			break;
		case FIREBALL:
			tame(player, powerStrength);
			break;
		case FREEZE:
			lightningStorm(player, powerStrength);
			break;
		case FIREWORK:
			lightning(player, powerStrength);
			break;
		case LIGHTNING:
			fireBall(player, powerStrength);
			break;
		case KNOWLEDGE:
			ice(player, powerStrength);
			break;
		case ICE:
			freeze(player, powerStrength);
			break;
		case TELEPORT:
			drunk(player, powerStrength);
			break;
		default:
			this.plugin.log("Unknown holy power");
		}
	}

	private void tame(Player player, int powerStrength)
	{
		for (Entity entity : getNearbyEntities(player.getLocation(), powerStrength * 2))
		{
			if ((entity instanceof Tameable))
			{
				Tameable creature = (Tameable) entity;

				creature.setOwner(player);
				creature.setTamed(true);
			}
		}
	}

	void lightning(Player player, int powerStrength)
	{
		int maxStrikes = 10;
		int strikes = 0;
		for (Entity entity : getNearbyEntities(player.getLocation(), 10.0D))
		{
			if (entity.getEntityId() != player.getEntityId())
			{
				if (strikes >= maxStrikes)
				{
					break;
				}
				entity.getWorld().strikeLightning(entity.getLocation());

				strikes++;
			}
		}
	}

	public void teleport(Player player, int powerStrength)
	{
		int distance = 100 * powerStrength;

		World world = player.getWorld();
		Location start = player.getLocation();
		start.setY(start.getY() + 1.6D);

		Block lastSafe = world.getBlockAt(start);

		BlockIterator bi = new BlockIterator(player, distance);
		while (bi.hasNext())
		{
			Block block = bi.next();
			if ((block.getType().isSolid()) && (block.getType() != Material.AIR))
			{
				break;
			}
			lastSafe = block;
		}
		Location newLoc = lastSafe.getLocation();
		newLoc.setPitch(start.getPitch());
		newLoc.setYaw(start.getYaw());
		player.teleport(newLoc);
		world.playEffect(newLoc, Effect.ENDER_SIGNAL, 0);
		world.playSound(newLoc, Sound.ENTITY_ENDERMEN_AMBIENT, 1.0F, 0.3F);
	}

	public void magicArrow(Player player, int powerStrength)
	{
		player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 1.0F);
		Arrow arrow = (Arrow) player.launchProjectile(Arrow.class);
	}

	public void earthQuake(final Player player, int value)
	{
		int earthQuakePower = 10;
		int earthQuakeDistance = 10;

		final Location location = player.getLocation().add(0.0D, -0.2D, 0.0D);
		final Vector direction = player.getLocation().getDirection();
		direction.setY(0);
		direction.normalize();
		BukkitRunnable task = new BukkitRunnable()
		{
			private int count = 0;

			public void run()
			{
				Location above = location.clone().add(0.0D, 1.0D, 0.0D);
				if ((above.getBlock().getType().isSolid()) || (!location.getBlock().getType().isSolid()))
				{
					cancel();
					return;
				}
				Location temp = location.clone();
				for (int x = -2; x <= 2; x++)
				{
					for (int z = -2; z <= 2; z++)
					{
						temp.setX(x + location.getBlockX());
						temp.setZ(z + location.getBlockZ());
						Block block = temp.getBlock();
						temp.getWorld().playEffect(temp, Effect.STEP_SOUND, block.getTypeId());
					}
				}
				Entity[] near = HolyPowerManager.this.getNearbyEntities(location, 1.5D);

				boolean hit = false;

				Random random = new Random();
				for (Entity e : near)
				{
					if (e != player)
					{
						hit = true;
						break;
					}
				}
				if (hit)
				{
					location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 10.0F, false, false);
					near = HolyPowerManager.this.getNearbyEntities(location, 2.5D);
					for (Entity e : near)
					{
						if (e != player)
						{
							e.setVelocity(new Vector(random.nextGaussian() / 4.0D, 1.0D + random.nextDouble() * 10.0D, random.nextGaussian() / 4.0D));
						}
					}
					cancel();
					return;
				}
				location.add(direction);
				if (this.count >= 10)
				{
					cancel();
				}
				this.count += 1;
			}
		};
		task.runTaskTimer(this.plugin, 0L, 3L);
	}

	private void flames()
	{
	}

	public void fireBall(Player player, int power)
	{
		player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0F, 1.0F);
		player.launchProjectile(SmallFireball.class);
	}

	public void freeze(Player player, int power)
	{
		player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 0.1F);
		for (int x = -10 * power; x < 10 * power; x++)
		{
			for (int y = -10; y < 2; y++)
			{
				for (int z = -10 * power; z < 10 * power; z++)
				{
					Location location = player.getLocation().add(x, y, z);
					if (location.getBlock().getType() == Material.WATER)
					{
						location.getBlock().setType(Material.ICE);
					}
				}
			}
		}
	}

	public void growNature(Player player, int power)
	{
		player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1.0F, 0.1F);
		for (int x = -10 * power; x < 10 * power; x++)
		{
			for (int y = -10; y < 20; y++)
			{
				for (int z = -10 * power; z < 10 * power; z++)
				{
					Location location = player.getLocation().add(x, y, z);
					if (location.getBlock().getType() == Material.DIRT)
					{
						if (location.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR)
						{
							location.getBlock().setType(Material.GRASS);
						}
					}
					if ((location.getBlock().getType() == Material.STONE) || (location.getBlock().getType() == Material.COBBLESTONE))
					{
						if (location.getBlock().getRelative(BlockFace.WEST).getType() == Material.AIR)
						{
							location.getBlock().getRelative(BlockFace.WEST).setTypeIdAndData(Material.VINE.getId(), (byte) 1, false);
						}
						if (location.getBlock().getRelative(BlockFace.EAST).getType() == Material.AIR)
						{
							location.getBlock().getRelative(BlockFace.EAST).setTypeIdAndData(Material.VINE.getId(), (byte) 1, false);
						}
						if (location.getBlock().getRelative(BlockFace.NORTH).getType() == Material.AIR)
						{
							location.getBlock().getRelative(BlockFace.NORTH).setTypeIdAndData(Material.VINE.getId(), (byte) 1, false);
						}
						if (location.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.AIR)
						{
							location.getBlock().getRelative(BlockFace.SOUTH).setTypeIdAndData(Material.VINE.getId(), (byte) 1, false);
						}
					}
					if ((location.getBlock().getType() == Material.GRASS) && (location.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR))
					{
						switch (this.random.nextInt(5))
						{
						case 0:
							location.getBlock().getRelative(BlockFace.UP).setType(Material.RED_ROSE);
							break;
						case 1:
							location.getBlock().getRelative(BlockFace.UP).setType(Material.LONG_GRASS);
							break;
						case 2:
							location.getBlock().getRelative(BlockFace.UP).setType(Material.YELLOW_FLOWER);
							break;
						case 3:
							location.getBlock().getRelative(BlockFace.UP).setType(Material.GRASS);
						}
					}
				}
			}
		}
	}

	public void ice(final Player player, int power)
	{
		player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 0.1F);

		final FallingBlock block = player.getWorld().spawnFallingBlock(player.getLocation().add(0.0D, 1.8D, 0.0D), Material.ICE, (byte) 0);
		block.setVelocity(player.getLocation().getDirection().multiply(2.0D));

		block.setDropItem(false);

		BukkitRunnable run = new BukkitRunnable()
		{
			public void run()
			{
				boolean hit = false;
				World world = block.getWorld();
				Location bLoc = block.getLocation();
				Location loc;
				
				for (int x = -1; x < 2; x++)
				{
					for (int y = -1; y < 2; y++)
					{
						for (int z = -1; z < 2; z++)
						{
							loc = block.getLocation().add(x, y, z);
							if (world.getBlockTypeIdAt(loc) != Material.AIR.getId())
							{
								Block b = world.getBlockAt(loc);
								if ((b.getType().isSolid()) && (HolyPowerManager.this.checkBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 1.0D, 1.0D, 1.0D, bLoc.getX() - 0.5D, bLoc.getY() - 0.5D, bLoc.getZ() - 0.5D, 1.0D, 1.0D, 1.0D)))
								{
									hit = true;
									break;
								}
							}
						}
					}
				}
				
				if (!hit)
				{
					List<Entity> entities = block.getNearbyEntities(1.0D, 1.0D, 1.0D);
					for (Entity e : entities)
					{
						if (e != player)
						{
							hit = true;
							break;
						}
					}
				}

				if (block.isDead() || hit)
				{
					block.remove();

					block.getLocation().getBlock().setType(Material.AIR);

					cancel();

					final HashMap<Location, Long> changedBlocks = new HashMap();
					
					for (int x = -1; x < 2; x++)
					{
						for (int y = -1; y < 3; y++)
						{
							for (int z = -1; z < 2; z++)
							{
								loc = block.getLocation().add(x, y, z);
								
								Block b = world.getBlockAt(loc);
								
								if (!b.getType().isSolid())
								{
									changedBlocks.put(b.getLocation(), Long.valueOf(b.getTypeId() | b.getData() << 16));
									b.setType(Material.ICE);
								}
							}
						}
					}
					
					new BukkitRunnable()
					{
						Random random = new Random();

						public void run()
						{
							for (int i = 0; i < 4; i++)
							{
								if (changedBlocks.isEmpty())
								{
									cancel();
									return;
								}
								
								int index = this.random.nextInt(changedBlocks.size());
								long data = ((Long) changedBlocks.values().toArray()[index]).longValue();
								Location position = (Location) changedBlocks.keySet().toArray()[index];
								changedBlocks.remove(position);
								Block c = position.getBlock();
								position.getWorld().playEffect(position, Effect.STEP_SOUND, c.getTypeId());
								c.setTypeId((int) (data & 0xFFFF));
								c.setData((byte) (int) (data >> 16));
							}
						}
					}.runTaskTimer(HolyPowerManager.this.plugin, 80 + new Random().nextInt(40), 3L);
				}
			}
		};
		run.runTaskTimer(this.plugin, 0L, 1L);
	}

	public void lightningStorm(Player player, int powerValue)
	{
		player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 1.0F, 0.1F);

		player.getWorld().setStorm(true);

		this.plugin.logDebug("Starting thunderstorm for " + powerValue + " minutes");

		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new ThunderStormTask(this.plugin, player, System.currentTimeMillis() + powerValue * 60000), 60L);
	}

	public void shootFirework(Player player, int powerValue)
	{
		this.plugin.logDebug("Starting fireworks with " + powerValue + " rockets");

		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new FireworkTask(this.plugin, player, powerValue), 1L);
	}

	public void callSun(Player player, int powerValue)
	{
		this.plugin.logDebug("Starting call sun " + powerValue + " rockets");

		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new CallSunTask(this.plugin, player, powerValue), 1L);
	}

	public void callMoon(Player player, int powerValue)
	{
		this.plugin.logDebug("Starting call moon " + powerValue + " ");

		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new CallMoonTask(this.plugin, player, powerValue), 1L);
	}

	public void healing(Player player, int powerValue)
	{
		this.plugin.logDebug("Healing up to " + powerValue + " creatures");

		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new HealRadiusTask(this.plugin, player, powerValue), 1L);
	}

	public void drunk(Player player, int powerValue)
	{
		this.plugin.logDebug("Drunking " + powerValue + " creatures");

		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new DrunkTask(this.plugin, player, powerValue), 1L);
	}

	public void boostKnowledge(Player player, int powerValue)
	{
		this.plugin.logDebug("Boosting " + powerValue + " xp of knowledge");

		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new BoostKnowledgeTask(this.plugin, player, powerValue), 1L);
	}
}
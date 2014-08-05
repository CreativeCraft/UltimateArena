package net.dmulloy2.ultimatearena.arenas.mob;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.ultimatearena.arenas.Arena;
import net.dmulloy2.ultimatearena.integration.VaultHandler;
import net.dmulloy2.ultimatearena.types.ArenaPlayer;
import net.dmulloy2.ultimatearena.types.ArenaZone;
import net.dmulloy2.util.Util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

/**
 * @author dmulloy2
 */

public class MobArena extends Arena
{
	private int mobTimer, mobSpawn, mobPerWave;
	private int maxWave, wave;

	private List<LivingEntity> mobs;
	private List<String> spawning;

	public MobArena(ArenaZone az)
	{
		super(az);
		this.winningTeam = -1;

		this.spawning = new ArrayList<>();
		spawning.add("ZOMBIE");
		spawning.add("ZOMBIE");
		spawning.add("ZOMBIE");

		this.mobs = new ArrayList<>();
		this.newWave();
	}

	private final void newWave()
	{
		if (wave > 0)
		{
			tellPlayers("&aYou survived the wave!");
			tellPlayers("&aNow going to wave &c{0}&a!", wave);
		}

		this.wave++;
		this.mobPerWave = 4 + ((int) (wave * 1.5)) + (active.size() * 3);
		this.mobTimer = (wave * 4) + 20;

		// TODO: Make entities spawned configurable
		if (wave <= 1)
		{
			mobTimer = 1;
		}
		if (wave > 1)
		{
			spawning.add("ZOMBIE");
			spawning.add("ZOMBIE");
			spawning.add("SKELETON");
		}
		if (wave > 3)
		{
			spawning.add("SPIDER");
		}
		if (wave > 6)
		{
			spawning.add("BLAZE");
			spawning.add("BLAZE");
		}
		if (wave > 9)
		{
			spawning.add("PIG_ZOMBIE");
			spawning.add("ENDERMAN");
		}
		if (wave > 12)
		{
			spawning.add("GHAST");
		}
	}

	@Override
	public void endPlayer(ArenaPlayer ap, boolean end)
	{
		super.endPlayer(ap, end);
		this.reward(ap);
	}

	@Override
	public void reward(ArenaPlayer pl)
	{
		// Enable check
		if (! az.getConfig().isGiveRewards())
			return;

		// If there are predefined rewards, give those
		if (! az.getConfig().getRewards().isEmpty())
		{
			super.reward(pl);
			return;
		}

		// Default to old system
		int amtGold = (int) Math.round(pl.getGameXP() / 500.0);
		int amtSlime = (int) Math.round(pl.getGameXP() / 550.0);
		int amtGlowStone = (int) Math.round(pl.getGameXP() / 450.0);
		int amtGunPowder = (int) Math.round(pl.getGameXP() / 425.0);
		int amtCash = (int) Math.round(pl.getGameXP() / 10.0);

		if (amtGold > 0)
			pl.giveItem(new ItemStack(Material.GOLD_INGOT, amtGold));

		if (amtSlime > 0)
			pl.giveItem(new ItemStack(Material.SLIME_BALL, amtSlime));

		if (amtGlowStone > 0)
			pl.giveItem(new ItemStack(Material.GLOWSTONE_DUST, amtGlowStone));

		if (amtGunPowder > 0)
			pl.giveItem(new ItemStack(Material.SULPHUR, amtGunPowder));

		if (amtCash > 0 && plugin.getConfig().getBoolean("moneyrewards"))
		{
			VaultHandler vault = plugin.getVaultHandler();
			if (vault.isEnabled())
			{
				vault.depositPlayer(pl.getPlayer(), amtCash);

				String cash = vault.getEconomy().format(amtCash);
				pl.sendMessage("&a{0} has been added to your balance!", cash);
			}
		}
	}

	@Override
	public void onOutOfTime()
	{
		setWinningTeam(-1);
		rewardTeam(winningTeam);
	}

	@Override
	public void check()
	{
		if (startTimer <= 0)
		{
			mobTimer--;
			mobSpawn--;
			if (mobSpawn < 0)
			{
				if (mobTimer < 0)
				{
					newWave();
					synchronized (mobs)
					{
						for (int i = 0; i < mobPerWave; i++)
						{
							Location loc = az.getSpawns().get(Util.random(az.getSpawns().size())).getLocation();
							String mob = spawning.get(Util.random(spawning.size()));
							LivingEntity newMob = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.valueOf(mob));

							// Special skeletons
							if (newMob instanceof Skeleton)
							{
								boolean giveBow = true;
								int enchantmentLevel = 0;

								// Skeletons get amped up starting at level 12
								if (wave >= 12)
								{
									// Wither Skeletons
									if (Util.random(2) == 0)
									{
										// Set skeleton type to Wither
										((Skeleton) newMob).setSkeletonType(SkeletonType.WITHER);

										// Wither skeletons dont have bows
										giveBow = false;
									}
									else
									{
										// Give them power bows
										enchantmentLevel = Util.random(5) == 0 ? 2 : 1;
									}
								}

								// Give them a bow, if applicable
								if (giveBow)
								{
									ItemStack item = new ItemStack(Material.BOW);

									// Enchant
									if (enchantmentLevel > 0)
									{
										item.addEnchantment(Enchantment.ARROW_DAMAGE, enchantmentLevel);
									}

									newMob.getEquipment().setItemInHand(item);
								}
							}
							// Special zombies
							else if (newMob instanceof Zombie)
							{
								// For zombies, it starts at wave 7
								if (wave >= 7)
								{
									int rand = Util.random(10);

									if (rand == 10)
									{
										// Zombies with swords
										ItemStack item = new ItemStack(wave >= 12 ? Material.DIAMOND_SWORD : Material.IRON_SWORD);

										// Possibly enchant it
										if (wave >= 12)
										{
											item.addEnchantment(Enchantment.DAMAGE_ALL, 2);
											item.addEnchantment(Enchantment.FIRE_ASPECT, 1);
										}

										newMob.getEquipment().setItemInHand(item);
									}
									else if (rand == 5)
									{
										// Babie zombies
										((Zombie) newMob).setBaby(true);
									}
									else if (rand == 0)
									{
										// Villager zombies
										((Zombie) newMob).setVillager(true);
									}
								}
							}
							// TODO: More fun entity calculations? >:D

							mobs.add(newMob);
						}
					}
				}
			}

			if (active.size() <= 0)
			{
				stop();
			}

			if (wave > maxWave)
			{
				setWinningTeam(-1);

				stop();

				rewardTeam(-1);
			}
		}
	}

	@Override
	public void announceWinner()
	{
		if (winningTeam == -1)
		{
			if (wave > maxWave)
			{
				tellPlayers("&3You have beat the MobArena!");
			}
			else
			{
				tellPlayers("&3You survived the MobArena!");
			}
		}
	}

	@Override
	public void onReload()
	{
		this.countMobKills = true;
		this.maxWave = getConfig().getMaxWave();
	}

	@Override
	public MobConfig getConfig()
	{
		return (MobConfig) super.getConfig();
	}

	@Override
	public List<String> getExtraInfo()
	{
		return Util.toList("&3Wave: &e" + wave);
	}
}
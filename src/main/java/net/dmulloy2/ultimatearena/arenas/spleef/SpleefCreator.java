/**
 * UltimateArena - fully customizable PvP arenas
 * Copyright (C) 2012 - 2015 MineSworn
 * Copyright (C) 2013 - 2015 dmulloy2
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.dmulloy2.ultimatearena.arenas.spleef;

import net.dmulloy2.ultimatearena.api.ArenaType;
import net.dmulloy2.ultimatearena.integration.WorldEditHandler;
import net.dmulloy2.ultimatearena.types.ArenaCreator;
import net.dmulloy2.ultimatearena.types.ArenaLocation;
import net.dmulloy2.ultimatearena.types.ArenaZone;
import net.dmulloy2.ultimatearena.types.Tuple;
import net.dmulloy2.util.FormatUtil;
import net.dmulloy2.util.MaterialUtil;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class SpleefCreator extends ArenaCreator
{
	public SpleefCreator(Player player, String name, ArenaType type)
	{
		super(player, name, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPoint(String[] args)
	{
		boolean error = false;
		Player player = getPlayer();
		switch (stepNumber)
		{
			case 1 -> // Arena
			{
				WorldEditHandler worldEdit = plugin.getWorldEditHandler();
				if (worldEdit != null && worldEdit.isEnabled())
				{
					if (!worldEdit.hasCuboidSelection(player))
					{
						sendMessage("&cYou must have a WorldEdit selection to do this!");
						return;
					}

					Tuple<Location, Location> sel = worldEdit.getSelection(player);
					Location first = sel.getFirst();
					Location second = sel.getSecond();

					// Perform some checks
					if (first == null || second == null)
					{
						sendMessage("&cPlease make sure you have two valid points in your selection!");
						return;
					}

					ArenaLocation arena1 = new ArenaLocation(first);
					ArenaLocation arena2 = new ArenaLocation(second);

					checkOverlap(arena1, arena2);

					target.setArena1(arena1);
					target.setArena2(arena2);

					sendMessage("&3Arena points set!");
					break; // Step completed
				} else
				{
					if (target.getArena1() == null)
					{
						target.setArena1(new ArenaLocation(player));
						sendMessage("&3First point set.");
						sendMessage("&3Please set the &e2nd &3point.");
						return;
					} else
					{
						target.setArena2(new ArenaLocation(player));
						sendMessage("&3Second point set!");
						break; // Step completed
					}
				}
			}
			case 2 -> // Lobby
			{
				WorldEditHandler worldEdit = plugin.getWorldEditHandler();
				if (worldEdit != null && worldEdit.isEnabled())
				{
					if (!worldEdit.hasCuboidSelection(player))
					{
						sendMessage("&cYou must have a WorldEdit selection to do this!");
						return;
					}

					Tuple<Location, Location> sel = worldEdit.getSelection(player);
					Location first = sel.getFirst();
					Location second = sel.getSecond();

					// Perform some checks
					if (first == null || second == null)
					{
						sendMessage("&cPlease make sure you have two valid points in your selection!");
						return;
					}

					if (!first.getWorld().equals(second.getWorld()))
					{
						sendMessage("&cYou must make your lobby in the same world as your arena!");
						return;
					}

					ArenaLocation lobby1 = new ArenaLocation(first);
					ArenaLocation lobby2 = new ArenaLocation(second);

					checkOverlap(lobby1, lobby2);

					target.setLobby1(lobby1);
					target.setLobby2(lobby2);

					sendMessage("&3Lobby points set!");
					break; // Step completed
				} else
				{
					ArenaLocation loc = new ArenaLocation(player);
					if (plugin.isInArena(loc))
					{
						sendMessage("&cThis point overlaps an existing arena!");
						return;
					}

					if (loc.getWorld().getUID() != target.getArena1().getWorld().getUID())
					{
						sendMessage("&cYou must make your lobby in the same world as your arena!");
						return;
					}

					if (target.getLobby1() == null)
					{
						target.setLobby1(new ArenaLocation(player));
						sendMessage("&3First point set.");
						sendMessage("&3Please set the &e2nd &3point.");
						return;
					} else
					{
						target.setLobby2(new ArenaLocation(player));
						sendMessage("&3Second point set!");
						break; // Step completed
					}
				}
			}
			case 3 -> // Lobby spawn
			{
				target.setLobbyREDspawn(new ArenaLocation(player));
				sendMessage("&eLobby &3spawnpoint set.");
				break; // Step completed
			}
			case 4 -> {
				Location location = player.getLocation().subtract(0.0D, 1.0D, 0.0D);

				if (target.getFlags().isEmpty())
				{
					target.getFlags().add(new ArenaLocation(location));
					sendMessage("&3First point set. Please set the &e2nd &3point.");
					return;
				} else
				{
					target.getFlags().add(new ArenaLocation(location));
					sendMessage("&eSpleef zone &3set.");
					break; // Step completed
				}
			}
			case 5 -> {
				if (target.getFlags().size() == 2)
				{
					target.getFlags().add(new ArenaLocation(player));
					sendMessage("&3First point set. Please set the &e2nd &3point.");
					return;
				} else
				{
					target.getFlags().add(new ArenaLocation(player));
					sendMessage("&eOut zone &3set.");
					break; // Step completed
				}
			}
			case 6 -> {
				Material specialType = Material.QUARTZ_BLOCK;
				if (args.length > 0)
				{
					Material material = Material.matchMaterial(args[0]);
					if (material != null)
					{
						specialType = material;
					}
					else
					{
						sendMessage("&4Invalid spleef ground type \"&c{0}&4\"");
						error = true;
						break;
					}
				}

				((SpleefZone) target).setSpecialType(specialType);
				sendMessage("&3Set &eSpleef Ground Type &3to: &e{0}", FormatUtil.getFriendlyName(specialType));
				break; // Step completed
			}
		}

		if (!error)
			stepUp(); // Next step
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stepInfo()
	{
		switch (stepNumber)
		{
			case 1, 2 -> super.stepInfo();
			case 3 -> sendMessage("&3Please set the &eLobby &3spawnpoint.");
			case 4 -> {
				sendMessage("&3Please set the &eSpleef ground&3.");
				sendMessage("&3This should be a 1 block thick rectangle where players will spawn");
			}
			case 5 -> sendMessage("&3Please set the &eOut zone&3.");
			case 6 -> {
				sendMessage("&3Please set the &eSpleef Ground Type&3.");
				sendMessage("&3Use &e/ua sp <material>");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSteps()
	{
		this.steps = 6;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArenaZone createArenaZone()
	{
		return new SpleefZone(type);
	}
}

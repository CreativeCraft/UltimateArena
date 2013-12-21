package net.dmulloy2.ultimatearena.creation;

import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.handlers.WorldEditHandler;
import net.dmulloy2.ultimatearena.types.FieldType;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * @author dmulloy2
 */

public class MobCreator extends ArenaCreator
{
	public MobCreator(Player player, String name, UltimateArena plugin)
	{
		super(player, name, plugin);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPoint(String[] args)
	{
		switch (stepNumber)
		{
			case 1: // Arena
			{
				WorldEditHandler worldEdit = plugin.getWorldEditHandler();
				if (worldEdit.useWorldEdit())
				{
					if (! worldEdit.hasSelection(player))
					{
						sendMessage("&cYou must have a WorldEdit selection to do this!");
						return;
					}

					Selection sel = worldEdit.getSelection(player);
					if (! worldEdit.isCuboidSelection(sel))
					{
						sendMessage("&cYou must have a cuboid selection to do this!");
						return;
					}

					Location arena1 = sel.getMaximumPoint();
					Location arena2 = sel.getMinimumPoint();

					// Perform some checks
					if (arena1 == null || arena2 == null)
					{
						sendMessage("&cPlease make sure you have two valid points in your selection!");
						return;
					}

					if (plugin.isInArena(arena1) || plugin.isInArena(arena2))
					{
						sendMessage("&cThese points overlap an existing arena!");
						return;
					}

					target.setArena1(arena1);
					target.setArena2(arena2);

					sendMessage("&3Arena points set!");
					break; // Step completed
				}
				else
				{
					if (target.getArena1() == null)
					{
						target.setArena1(player.getLocation());
						sendMessage("&3First point set.");
						sendMessage("&3Please set the &e2nd &3point.");
						return;
					}
					else
					{
						target.setArena2(player.getLocation());
						sendMessage("&3Second point set!");
						break; // Step completed
					}
				}
			}
			case 2: // Lobby
			{
				WorldEditHandler worldEdit = plugin.getWorldEditHandler();
				if (worldEdit.useWorldEdit())
				{
					if (! worldEdit.hasSelection(player))
					{
						sendMessage("&cYou must have a WorldEdit selection to do this!");
						return;
					}

					Selection sel = worldEdit.getSelection(player);
					if (! worldEdit.isCuboidSelection(sel))
					{
						sendMessage("&cYou must have a cuboid selection to do this!");
						return;
					}

					Location lobby1 = sel.getMaximumPoint();
					Location lobby2 = sel.getMinimumPoint();

					// Perform some checks
					if (lobby1 == null || lobby2 == null)
					{
						sendMessage("&cPlease make sure you have two valid points in your selection!");
						return;
					}

					if (plugin.isInArena(lobby1) || plugin.isInArena(lobby2))
					{
						sendMessage("&cThese points overlap an existing arena!");
						return;
					}

					if (lobby1.getWorld().getUID() != target.getArena1().getWorld().getUID())
					{
						sendMessage("&cYou must make your lobby in the same world as your arena!");
						return;
					}

					target.setLobby1(lobby1);
					target.setLobby2(lobby2);

					sendMessage("&3Lobby points set!");
					break; // Step completed
				}
				else
				{
					Location loc = player.getLocation();
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
						target.setLobby1(player.getLocation());
						sendMessage("&3First point set.");
						sendMessage("&3Please set the &e2nd &3point.");
						return;
					}
					else
					{
						target.setArena2(player.getLocation());
						sendMessage("&3Second point set!");
						break; // Step completed
					}
				}
			}
			case 3:
			{
				target.setLobbyREDspawn(player.getLocation());
				sendMessage("&eLobby &3spawnpoint set!");
				break; // Step completed
			}
			case 4:
			{
				target.setTeam1spawn(player.getLocation());
				sendMessage("&ePlayer &3spawnpoint set!");
				break; // Step completed
			}
			case 5:
			{
				if (args.length > 0 && args[0].equalsIgnoreCase("done"))
				{
					sendMessage("&3Done setting mob spawns!");
					break; // Step completed
				}
				else
				{
					target.getSpawns().add(player.getLocation());
					sendMessage("&eMob &3spawn set. Total: &e{0}", target.getSpawns().size());
					return;
				}
			}
		}

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
			case 1:
				sendMessage("&3Please set &e2 &3points for the arena");
				break;
			case 2:
				sendMessage("&3Please set &e2 &3points for the lobby.");
				break;
			case 3:
				sendMessage("&3Please set the &eLobby &3spawn.");
				break;
			case 4:
				sendMessage("&3Please set the &eplayer &3spawn.");
				break;
			case 5:
				sendMessage("&3Please add some &emob &3spawns.");
				sendMessage("&3Use &e/ua sp done &3when they are set.");
				break;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws UnsupportedOperationException Not supported yet
	 */
	@Override
	public void undo()
	{
		throw new UnsupportedOperationException("Not supported yet"); // TODO
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FieldType getType()
	{
		return FieldType.MOB;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSteps()
	{
		this.steps = 5;
	}
}
package net.dmulloy2.ultimatearena.flags;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.arenas.Arena;
import net.dmulloy2.ultimatearena.arenas.BOMBArena;
import net.dmulloy2.ultimatearena.types.ArenaPlayer;
import net.dmulloy2.ultimatearena.util.Util;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class BombFlag extends ArenaFlag
{
	protected int bnum;
	protected int fuser;
	protected int timer = 45;

	protected boolean fused;
	protected boolean exploded;

	public BombFlag(Arena arena, Location loc, final UltimateArena plugin)
	{
		super(arena, loc, plugin);
	}

	@Override
	public void checkNear(List<ArenaPlayer> arenaplayers)
	{
		if (fused)
		{
			timer--;
			Util.playEffect(Effect.STEP_SOUND, getLoc(), 4);

			if (timer == 30 || timer == 20 || timer == 10 || timer <= 5)
			{
				arena.tellPlayers("&3Bomb &e{0} &3will explode in &e{0} &3seconds!", bnum, timer);
			}

			if (timer < 1)
			{
				if (!isExploded())
				{
					Util.playEffect(Effect.EXTINGUISH, getLoc(), 4);
					
					BOMBArena ba = null;
					if (getArena() instanceof BOMBArena)
					{
						ba = (BOMBArena) getArena();
					}

					if (ba != null)
					{
						int amte = 0;
						if (ba.bomb1.isExploded())
							amte++;
						if (ba.bomb2.isExploded())
							amte++;

						if (amte == 0)
							arena.killAllNear(getLoc(), 12);
					}
					
					this.fused = false;
					this.exploded = true;
					
					arena.tellPlayers("&cRED &3team blew up bomb &e{0}&3!", bnum);
				}
			}
		}

		boolean fuse = false;
		boolean defuse = false;
		ArenaPlayer capturer = null;
		List<Player> players = new ArrayList<Player>();
		for (int i = 0; i < arenaplayers.size(); i++)
		{
			ArenaPlayer apl = arenaplayers.get(i);
			Player pl = apl.getPlayer();
			if (pl != null)
			{
				if (Util.pointDistance(pl.getLocation(), getLoc()) < 3.0 && pl.getHealth() > 0)
				{
					players.add(pl);
					capturer = apl;
					if (apl.getTeam() == 1)
					{
						fuse = true;
					}
					else
					{
						defuse = true;
					}
				}
			}
		}

		if (! (fuse && defuse) && ! exploded)
		{
			if (capturer != null)
			{
				if (fuse)
				{
					if (! fused)
					{
						// team 1 is fusing
						fuser++;
						capturer.sendMessage("&3Fusing Bomb &e{0}! &3(&e{1}&3/&e10)", getBnum(), fuser);
						if (fuser > 10)
						{
							fuser = 0;
							fused = true;
							arena.tellPlayers("&3Bomb &e{0} &3is now &efused&3!", getBnum());
						}
					}
				}
				else if (defuse)
				{
					// team 2 is desfusing
					if (fused)
					{
						fuser++;
						capturer.sendMessage("&3Defusing Bomb &e{0}! &3(&e{1}&3/&e10&3)", getBnum(), fuser);
						if (fuser > 10)
						{
							fuser = 0;
							fused = false;
							timer = 45;
							arena.tellPlayers("&3Bomb &e{0} &3is now &edefused&3!", getBnum());
						}
					}
				}
			}
		}
	}

	public int getBnum()
	{
		return bnum;
	}

	public void setBnum(int bnum)
	{
		this.bnum = bnum;
	}

	public boolean isExploded()
	{
		return exploded;
	}

	public void setExploded(boolean exploded)
	{
		this.exploded = exploded;
	}
}
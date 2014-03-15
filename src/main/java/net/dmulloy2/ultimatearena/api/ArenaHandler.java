package net.dmulloy2.ultimatearena.api;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.util.Util;

import com.google.common.collect.Maps;

/**
 * @author dmulloy2
 */

public class ArenaHandler
{
	private final Map<String, ArenaType> arenaTypes;
	private final UltimateArena plugin;

	public ArenaHandler(UltimateArena plugin)
	{
		this.plugin = plugin;
		this.arenaTypes = Maps.newHashMap();
	}

	public final void loadArenaTypes()
	{
		File directory = new File(plugin.getDataFolder(), "arenas");
		if (! directory.exists())
		{
			directory.mkdir();
			// TODO: Generate default arenas
		}

		Pattern filter = Pattern.compile("\\.jar$");
		ArenaLoader loader = new ArenaLoader(plugin);

		for (File file : directory.listFiles())
		{
			Matcher match = filter.matcher(file.getName());
			if (! match.find()) continue;

			ArenaType arenaType;

			try
			{
				arenaType = loader.loadArenaType(file);
				arenaType.onLoad();
			}
			catch (Exception e)
			{
				plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "loading arena type '" + file.getName() + "'"));
				continue;
			}

			arenaTypes.put(arenaType.getName(), arenaType);
		}
	}

	public final void enableArenaTypes()
	{
		for (ArenaType type : arenaTypes.values())
		{
			try
			{
				type.onEnable();
			}
			catch (Exception e)
			{
				plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "enabling arena type '" + type.getName() + "'"));
			}
		}
	}

	public final void disableArenaTypes()
	{
		for (ArenaType type : arenaTypes.values())
		{
			try
			{
				type.onDisable();
			}
			catch (Exception e)
			{
				plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "disabling arena type '" + type.getName() + "'"));
			}
		}

		arenaTypes.clear();
	}
}
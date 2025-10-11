package com.arckenver.towny.cmdexecutor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.cmdexecutor.towny.TownyInfoExecutor;
import com.arckenver.towny.cmdexecutor.townyadmin.TownyadminExecutor;
import com.arckenver.towny.cmdexecutor.townyworld.TownyworldExecutor;
import com.arckenver.towny.cmdexecutor.plot.PlotExecutor;
import com.arckenver.towny.cmdexecutor.towny.TownMapExecutor;
import com.arckenver.towny.cmdexecutor.resident.ResidentExecutor;


public class TownyCmds {

	public static void create(TownyPlugin plugin)
	{

		CommandSpec.Builder townyCmd = CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.execute")
				.executor(new TownyInfoExecutor());

		CommandSpec.Builder townyadminCmd = CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.execute")
				.executor(new TownyadminExecutor());

		CommandSpec.Builder plotCmd = CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.execute")
				.executor(new PlotExecutor());

		CommandSpec.Builder townyworldCmd = CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyworld.execute")
				.executor(new TownyworldExecutor());

		// inside create(TownyPlugin plugin)
		CommandSpec.Builder residentCmd = CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.resident.execute")
				.executor(new com.arckenver.towny.cmdexecutor.resident.ResidentInfoExecutor()); // show self/others

		createCmds(residentCmd, "com.arckenver.towny.cmdexecutor.resident");

// Register the alias pair
		Sponge.getCommandManager().register(plugin, residentCmd.build(), "resident", "res");

		createCmds(residentCmd, "com.arckenver.towny.cmdexecutor.resident");

		createCmds(townyCmd, "com.arckenver.towny.cmdexecutor.towny");
		createCmds(townyadminCmd, "com.arckenver.towny.cmdexecutor.townyadmin");
		createCmds(plotCmd, "com.arckenver.towny.cmdexecutor.plot");
		createCmds(townyworldCmd, "com.arckenver.towny.cmdexecutor.townyworld");

		Sponge.getCommandManager().register(plugin, townyadminCmd.build(), "townyadmin", "ta", "townyadmin");
		Sponge.getCommandManager().register(plugin, townyCmd.build(), "town", "t", "towny");
		Sponge.getCommandManager().register(plugin, plotCmd.build(), "plot", "p");
		Sponge.getCommandManager().register(plugin, townyworldCmd.build(), "townyworld", "tw");
	}

	private static void createCmds(CommandSpec.Builder cmd, String path)
	{
		path = path.concat(".");
		try {
			JarFile jarFile = new JarFile(URLDecoder.decode(TownyPlugin.class.getProtectionDomain().getCodeSource().getLocation().toString().split("!")[0].replaceFirst("jar:file:", ""), "UTF-8"));
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements())
			{
				JarEntry entry = entries.nextElement();
				if (entry.getName().replace("/", ".").replace("\\", ".").startsWith(path) && entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
					String className = path.concat(entry.getName().substring(path.length()).replace(".class", ""));
					try {
						Class<?> cl = Class.forName(className);
						cl.getMethod("create", CommandSpec.Builder.class).invoke(null, cmd);
					} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						System.out.println(className);
						e.printStackTrace();
					}
				}
			}
			jarFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

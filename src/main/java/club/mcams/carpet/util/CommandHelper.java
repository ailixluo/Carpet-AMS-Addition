/*
 * This file is part of the Carpet AMS Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  A Minecraft Server and contributors
 *
 * Carpet AMS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Carpet AMS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Carpet AMS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package club.mcams.carpet.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@SuppressWarnings("unused")
public final class CommandHelper {
    private CommandHelper() {}
    /**
     * Notifies all players that the commands changed by resending the command tree.
     */
    public static void notifyPlayersCommandsChanged(MinecraftServer server) {
        if (server == null || server.getPlayerManager() == null) {
            return;
        }
        server.send(new ServerTask(server.getTicks(), () -> {
            try {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    server.getCommandManager().sendCommandTree(player);
                }
            }
            catch (NullPointerException ignored) {}
        }));
    }

    /**
     * Whether the given source has enough permission level to run a command that requires the given commandLevel
     */
    public static boolean canUseCommand(ServerCommandSource source, Object commandLevel) {
        if (commandLevel instanceof Boolean) return (Boolean) commandLevel;
        String commandLevelString = commandLevel.toString();
        switch (commandLevelString) {
            case "true": return true;
            case "false": return false;
            case "ops": return source.hasPermissionLevel(2); // typical for other cheaty commands
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
                return source.hasPermissionLevel(Integer.parseInt(commandLevelString));
        }
        return false;
    }
}
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

package club.mcams.carpet.mixin.rule.amsUpdateSuppressionCrashFix;

import carpet.utils.Messenger;

import club.mcams.carpet.AmsServerSettings;
import club.mcams.carpet.helpers.rule.amsUpdateSuppressionCrashFix.ThrowableSuppression;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    //#if MC<11900
    @WrapOperation(
            method = "tickWorlds",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V"
            )
    )
    private void ficUpdateSuppressionCrashTick(ServerWorld serverWorld, BooleanSupplier shouldKeepTicking, Operation<Void> original){
        if (AmsServerSettings.amsUpdateSuppressionCrashFix) {
            try {
                serverWorld.tick(shouldKeepTicking);
            } catch (CrashException e) {
                if (!(e.getCause() instanceof ThrowableSuppression)) throw e;
                logUpdateSuppression();
            } catch (ThrowableSuppression ignored) {
                logUpdateSuppression();
            }
        }

        if (!AmsServerSettings.amsUpdateSuppressionCrashFix) {
            serverWorld.tick(shouldKeepTicking);
            original.call(serverWorld, shouldKeepTicking);
        }
    }

    @Unique
    public void logUpdateSuppression() {
        Messenger.print_server_message((MinecraftServer) (Object) this, "You caused a server crash.");
    }
    //#endif
}

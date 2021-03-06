/*
 * Copyright 2017 Aljoscha Grebe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.almightyalpaca.intellij.plugins.discord.rpc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.almightyalpaca.intellij.plugins.discord.presence.PresenceRenderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
public class RPC
{
    @Nullable
    private static volatile Thread callbackRunner;
    @Nullable
    private static volatile Thread delayedPresenceRunner;
    private static volatile long nextPresenceUpdate = Long.MAX_VALUE;

    @Nullable
    private static volatile DiscordRichPresence presence;
    private static volatile boolean initialized = false;

    private RPC() {}

    public static synchronized void init(@NotNull DiscordEventHandlers handlers, @NotNull String clientId, @NotNull Supplier<PresenceRenderContext> contextSupplier, @NotNull Function<PresenceRenderContext, DiscordRichPresence> renderer)
    {
        if (!RPC.initialized)
        {
            RPC.initialized = true;

            DiscordRPC.INSTANCE.Discord_Initialize(clientId, handlers, true, null);

            RPC.callbackRunner = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted())
                {
                    try
                    {
                        DiscordRPC.INSTANCE.Discord_RunCallbacks();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    try
                    {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException ignored) {}
                }
            }, "RPC-Callback-Handler");

            RPC.callbackRunner.start();

            RPC.delayedPresenceRunner = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted())
                {
                    try
                    {
                        long timeout = RPC.nextPresenceUpdate - System.nanoTime();

                        if (timeout > 0)
                        {
                            LockSupport.parkNanos(timeout);
                        }
                        else
                        {
                            DiscordRichPresence newPresence = renderer.apply(contextSupplier.get());
                            if (!Objects.equals(RPC.presence, newPresence))
                            {
                                DiscordRPC.INSTANCE.Discord_UpdatePresence(newPresence);
                                RPC.presence = newPresence;
                            }
                        }

                        LockSupport.park();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }, "RPC-Delayed-Presence-Handler");

            RPC.delayedPresenceRunner.start();
        }

    }

    public static synchronized void dispose()
    {
        if (RPC.initialized)
        {
            RPC.initialized = false;

            RPC.delayedPresenceRunner.interrupt();
            RPC.delayedPresenceRunner = null;

            DiscordRPC.INSTANCE.Discord_Shutdown();

            RPC.callbackRunner.interrupt();
            RPC.callbackRunner = null;
        }
    }

    public static void updatePresence(long delay, @NotNull TimeUnit unit)
    {
        RPC.nextPresenceUpdate = System.nanoTime() + unit.convert(delay, TimeUnit.MILLISECONDS);

        LockSupport.unpark(RPC.delayedPresenceRunner);
    }
}

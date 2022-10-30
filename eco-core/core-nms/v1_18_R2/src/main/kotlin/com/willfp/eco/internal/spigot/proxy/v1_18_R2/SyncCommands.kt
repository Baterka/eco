package com.willfp.eco.internal.spigot.proxy.v1_18_R2

import com.willfp.eco.internal.spigot.proxy.SyncCommandsProxy
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_18_R2.CraftServer

class SyncCommands : SyncCommandsProxy {
    override fun syncCommands() {
        (Bukkit.getServer() as CraftServer).syncCommands()
    }
}
package net.deamjava.plotarmor

import java.util.UUID

object PlotArmorData {
    private val armoredPlayers = mutableSetOf<UUID>()
    private lateinit var saveFile: java.io.File

    fun init(configDir: java.io.File) {
        saveFile = java.io.File(configDir, "plotarmor.txt")
        if (saveFile.exists()) {
            saveFile.readLines()
                .mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
                .forEach { armoredPlayers.add(it) }
        }
    }

    private fun save() {
        saveFile.writeText(armoredPlayers.joinToString("\n"))
    }

    fun add(uuid: UUID): Boolean = armoredPlayers.add(uuid).also { if (it) save() }
    fun remove(uuid: UUID): Boolean = armoredPlayers.remove(uuid).also { if (it) save() }
    fun isArmored(uuid: UUID): Boolean = uuid in armoredPlayers
    fun getAll(): Set<UUID> = armoredPlayers.toSet()
}
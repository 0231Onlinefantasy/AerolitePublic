/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.script.remapper

import net.ccbluex.liquidbounce.utils.ClientUtils
import org.apache.commons.io.IOUtils

/**
 * A srg remapper
 *
 * @author CCBlueX
 */
object Remapper {

    private var srgLoaded = false

    private val fields: HashMap<String, HashMap<String, String>> = hashMapOf()
    private val methods: HashMap<String, HashMap<String, String>> = hashMapOf()

    /**
     * Load srg
     */
    fun loadSrg() {
        if (srgLoaded)
            return
        // Load srg
        ClientUtils.logInfo("[Remapper] Loading srg...")
        parseSrg(IOUtils.readLines(Remapper::class.java.classLoader.getResourceAsStream("assets/minecraft/aerolite/misc/mcp-stable_22.srg")))
        srgLoaded = true
        ClientUtils.logInfo("[Remapper] Loaded srg.")
    }

    private fun parseSrg(srgData: List<String>) {
        srgData.forEach {
            val args = it.split(" ")

            when {
                it.startsWith("FD:") -> {
                    val name = args[1]
                    val srg = args[2]

                    val className = name.substring(0, name.lastIndexOf('/')).replace('/', '.')
                    val fieldName = name.substring(name.lastIndexOf('/') + 1)
                    val fieldSrg = srg.substring(srg.lastIndexOf('/') + 1)

                    if (!fields.contains(className)) {
                        fields[className] = hashMapOf()
                    }

                    fields[className]!![fieldSrg] = fieldName
                }

                it.startsWith("MD:") -> {
                    val name = args[1]
                    val desc = args[2]
                    val srg = args[3]

                    val className = name.substring(0, name.lastIndexOf('/')).replace('/', '.')
                    val methodName = name.substring(name.lastIndexOf('/') + 1)
                    val methodSrg = srg.substring(srg.lastIndexOf('/') + 1)

                    if (!methods.contains(className)) {
                        methods[className] = hashMapOf()
                    }

                    methods[className]!![methodSrg + desc] = methodName
                }
            }
        }
    }

    /**
     * Remap field
     */
    fun remapField(clazz: Class<*>, name: String): String {
        if (!fields.containsKey(clazz.name)) {
            return name
        }

        return fields[clazz.name]!!.getOrDefault(name, name)
    }

    /**
     * Remap method
     */
    fun remapMethod(clazz: Class<*>, name: String, desc: String): String {
        if (!methods.containsKey(clazz.name)) {
            return name
        }

        return methods[clazz.name]!!.getOrDefault(name + desc, name)
    }
}
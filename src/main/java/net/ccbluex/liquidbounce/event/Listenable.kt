/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

import java.lang.reflect.Method

interface Listenable {
    fun handleEvents(): Boolean
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class EventTarget(val ignoreCondition: Boolean = false, val priority: Int = 0)

internal class EventHook(val eventClass: Listenable, val method: Method, val priority: Int, eventTarget: EventTarget) {
    val isIgnoreCondition = eventTarget.ignoreCondition

    constructor(eventClass: Listenable, method: Method, eventTarget: EventTarget): this(eventClass, method, 0, eventTarget)
}

package net.ccbluex.liquidbounce.slib.marks

import net.ccbluex.liquidbounce.features.module.ModuleCategory

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AeroLiteModule(
    val name: String,
    val category: ModuleCategory,
    val author: String,
    val skid: Boolean,
    val fromScript: Boolean
)
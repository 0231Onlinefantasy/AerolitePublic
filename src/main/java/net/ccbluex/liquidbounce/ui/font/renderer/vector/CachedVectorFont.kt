
import net.ccbluex.liquidbounce.ui.font.renderer.AbstractCachedFont
import net.ccbluex.liquidbounce.utils.render.glu.tess.CacheTessCallback
import org.lwjgl.opengl.GL11

class CachedVectorFont(val list: Int, val width: Int) : AbstractCachedFont(System.currentTimeMillis()) {
    override fun finalize() {
        GL11.glDeleteLists(list, 1)
    }
}
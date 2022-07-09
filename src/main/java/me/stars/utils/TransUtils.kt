package me.stars.utils

class TransUtils(var x: Float, var y: Float) {
    private var first = false
    fun interpolate(targetX: Float, targetY: Float, smoothing: Double) {
        if (first) {
            x = AnimationUtil.animate(targetX, x, smoothing)
            y = AnimationUtil.animate(targetY, y, smoothing)
        } else {
            x = targetX
            y = targetY
            first = true
        }
    }

    fun translate(targetX: Float, targetY: Float) {
        x = AnimationUtils.lstransition(x, targetX, 0.0)
        y = AnimationUtils.lstransition(y, targetY, 0.0)
    }

    fun translate(targetX: Float, targetY: Float, speed: Double) {
        x = AnimationUtils.lstransition(x, targetX, speed)
        y = AnimationUtils.lstransition(y, targetY, speed)
    }

    fun interpolate2(targetX: Float, targetY: Float, smoothing: Double) {
        x = targetX
        y = AnimationUtil.animate(targetY, y, smoothing)
    }
}
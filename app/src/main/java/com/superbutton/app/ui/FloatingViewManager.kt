package com.superbutton.app.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import com.superbutton.app.R
import com.superbutton.app.data.PreferenceManager
import com.superbutton.app.model.ButtonAction
import com.superbutton.app.services.PowerAccessibilityService
import kotlin.math.abs

class FloatingViewManager(private val context: Context) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefManager: PreferenceManager = PreferenceManager(context)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var floatingRootView: View? = null
    private var btnPowerView: ImageView? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    // Radial Menu
    private var radialMenuView: View? = null
    private var isRadialMenuShowing: Boolean = false

    // Screen dimensions
    private val screenSize = Point()
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    // Gesture tracking
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var lastTapTime = 0L
    private val doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout().toLong()

    // Vibrator
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Auto-fade Runnable
    private val autoFadeRunnable = Runnable {
        animateFade(prefManager.idleOpacity)
    }

    // Long-press Runnable
    private val longPressRunnable = Runnable {
        if (!isDragging) {
            triggerHapticFeedback(strong = true)
            handleAction(prefManager.longPressAction)
        }
    }

    init {
        updateScreenDimensions()
    }

    private fun updateScreenDimensions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenSize.set(bounds.width(), bounds.height())
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getRealSize(screenSize)
        }
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun show() {
        if (floatingRootView != null) return

        updateScreenDimensions()

        val inflater = LayoutInflater.from(context)
        floatingRootView = inflater.inflate(R.layout.layout_floating_button, null)
        btnPowerView = floatingRootView?.findViewById(R.id.btnPower)

        applyButtonSize()
        applyTransparency(prefManager.activeOpacity)

        // Set layout parameters
        val sizePx = dpToPx(prefManager.buttonSizeDp)
        val initialXPos = if (prefManager.lastPositionX >= 0) {
            prefManager.lastPositionX
        } else {
            screenSize.x - sizePx - dpToPx(8)
        }
        val initialYPos = if (prefManager.lastPositionY >= 0) {
            prefManager.lastPositionY
        } else {
            screenSize.y / 2
        }

        windowLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialXPos
            y = initialYPos
        }

        floatingRootView?.setOnTouchListener { _, event ->
            handleTouchEvent(event)
        }

        try {
            windowManager.addView(floatingRootView, windowLayoutParams)
            resetAutoFadeTimer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hide() {
        dismissRadialMenu()
        mainHandler.removeCallbacksAndMessages(null)

        floatingRootView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        floatingRootView = null
        btnPowerView = null
    }

    fun reloadPreferences() {
        applyButtonSize()
        applyTransparency(prefManager.activeOpacity)
        resetAutoFadeTimer()
    }

    private fun applyButtonSize() {
        val sizePx = dpToPx(prefManager.buttonSizeDp)
        btnPowerView?.let { view ->
            val lp = view.layoutParams
            lp.width = sizePx
            lp.height = sizePx
            view.layoutParams = lp
        }
    }

    private fun applyTransparency(alpha: Float) {
        btnPowerView?.alpha = alpha
    }

    private fun animateFade(targetAlpha: Float) {
        btnPowerView?.let { view ->
            view.animate()
                .alpha(targetAlpha)
                .setDuration(400)
                .start()
        }
    }

    private fun resetAutoFadeTimer() {
        mainHandler.removeCallbacks(autoFadeRunnable)
        animateFade(prefManager.activeOpacity)

        if (prefManager.idleTimeoutSeconds > 0) {
            mainHandler.postDelayed(
                autoFadeRunnable,
                (prefManager.idleTimeoutSeconds * 1000).toLong()
            )
        }
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        val layoutParams = windowLayoutParams ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                resetAutoFadeTimer()
                isDragging = false
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY

                mainHandler.postDelayed(longPressRunnable, 550)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - initialTouchX).toInt()
                val dy = (event.rawY - initialTouchY).toInt()

                if (!isDragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                    isDragging = true
                    mainHandler.removeCallbacks(longPressRunnable)
                }

                if (isDragging) {
                    layoutParams.x = initialX + dx
                    layoutParams.y = initialY + dy
                    try {
                        windowManager.updateViewLayout(floatingRootView, layoutParams)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                mainHandler.removeCallbacks(longPressRunnable)

                if (isDragging) {
                    if (prefManager.isSnapToEdge) {
                        snapToNearestEdge()
                    } else {
                        savePosition(layoutParams.x, layoutParams.y)
                    }
                    resetAutoFadeTimer()
                } else {
                    // Tap detection
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < doubleTapTimeout) {
                        // Double Tap
                        lastTapTime = 0L
                        triggerHapticFeedback(strong = false)
                        handleAction(prefManager.doubleTapAction)
                    } else {
                        lastTapTime = currentTime
                        mainHandler.postDelayed({
                            if (lastTapTime != 0L) {
                                lastTapTime = 0L
                                triggerHapticFeedback(strong = false)
                                handleAction(prefManager.singleTapAction)
                            }
                        }, doubleTapTimeout)
                    }
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                mainHandler.removeCallbacks(longPressRunnable)
                resetAutoFadeTimer()
                return true
            }
        }
        return false
    }

    private fun snapToNearestEdge() {
        val layoutParams = windowLayoutParams ?: return
        updateScreenDimensions()

        val buttonWidth = floatingRootView?.width ?: dpToPx(prefManager.buttonSizeDp)
        val middleX = (screenSize.x - buttonWidth) / 2
        val targetX = if (layoutParams.x < middleX) 0 else screenSize.x - buttonWidth

        val animator = ValueAnimator.ofInt(layoutParams.x, targetX)
        animator.duration = 250
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            layoutParams.x = animation.animatedValue as Int
            try {
                windowManager.updateViewLayout(floatingRootView, layoutParams)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        animator.start()

        savePosition(targetX, layoutParams.y)
    }

    private fun savePosition(x: Int, y: Int) {
        prefManager.lastPositionX = x
        prefManager.lastPositionY = y
    }

    private fun handleAction(action: ButtonAction) {
        if (action == ButtonAction.RADIAL_MENU) {
            toggleRadialMenu()
            return
        }

        val service = PowerAccessibilityService.instance
        if (service != null) {
            service.executeAction(action)
        } else {
            // Service not running: fallback to opening settings or showing toast
            toggleRadialMenu()
        }
    }

    @SuppressLint("InflateParams")
    private fun toggleRadialMenu() {
        if (isRadialMenuShowing) {
            dismissRadialMenu()
        } else {
            showRadialMenu()
        }
    }

    private fun showRadialMenu() {
        if (radialMenuView != null) return
        updateScreenDimensions()

        val inflater = LayoutInflater.from(context)
        radialMenuView = inflater.inflate(R.layout.layout_radial_menu, null)

        // Setup button clicks
        radialMenuView?.findViewById<View>(R.id.actionPowerMenu)?.setOnClickListener {
            triggerHapticFeedback(false)
            PowerAccessibilityService.instance?.executeAction(ButtonAction.POWER_MENU)
            dismissRadialMenu()
        }
        radialMenuView?.findViewById<View>(R.id.actionLockScreen)?.setOnClickListener {
            triggerHapticFeedback(false)
            PowerAccessibilityService.instance?.executeAction(ButtonAction.LOCK_SCREEN)
            dismissRadialMenu()
        }
        radialMenuView?.findViewById<View>(R.id.actionScreenshot)?.setOnClickListener {
            triggerHapticFeedback(false)
            PowerAccessibilityService.instance?.executeAction(ButtonAction.SCREENSHOT)
            dismissRadialMenu()
        }
        radialMenuView?.findViewById<View>(R.id.actionVolume)?.setOnClickListener {
            triggerHapticFeedback(false)
            PowerAccessibilityService.instance?.executeAction(ButtonAction.VOLUME_PANEL)
            dismissRadialMenu()
        }
        radialMenuView?.findViewById<View>(R.id.actionNotifications)?.setOnClickListener {
            triggerHapticFeedback(false)
            PowerAccessibilityService.instance?.executeAction(ButtonAction.NOTIFICATIONS)
            dismissRadialMenu()
        }
        radialMenuView?.findViewById<View>(R.id.actionHome)?.setOnClickListener {
            triggerHapticFeedback(false)
            PowerAccessibilityService.instance?.executeAction(ButtonAction.HOME)
            dismissRadialMenu()
        }

        // Tap root background to dismiss
        radialMenuView?.setOnClickListener {
            dismissRadialMenu()
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            windowManager.addView(radialMenuView, params)
            isRadialMenuShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dismissRadialMenu() {
        radialMenuView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        radialMenuView = null
        isRadialMenuShowing = false
    }

    private fun triggerHapticFeedback(strong: Boolean) {
        if (!prefManager.isHapticEnabled) return
        val duration = if (strong) 50L else 25L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitude = if (strong) VibrationEffect.DEFAULT_AMPLITUDE else 120
            vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}

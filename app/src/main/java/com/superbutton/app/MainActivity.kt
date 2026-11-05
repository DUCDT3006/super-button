package com.superbutton.app

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.superbutton.app.data.PreferenceManager
import com.superbutton.app.databinding.ActivityMainBinding
import com.superbutton.app.model.ButtonAction
import com.superbutton.app.services.FloatingButtonService
import com.superbutton.app.services.PowerAccessibilityService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefManager: PreferenceManager

    private var isUpdatingUi = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(this)

        setupPermissionCards()
        setupMainSwitch()
        setupAppearanceSliders()
        setupMovementSwitches()
        setupGestureSpinners()
        setupSamsungTips()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        syncSwitchState()
    }

    private fun setupPermissionCards() {
        binding.btnGrantOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "Quyền hiển thị trên ứng dụng khác đã được bật", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGrantAccessibility.setOnClickListener {
            showAccessibilityGuideDialog()
        }
    }

    private fun showAccessibilityGuideDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Kích hoạt Quyền Trợ năng (Accessibility)")
            .setMessage(
                "Để nút nguồn ảo có thể Khóa màn hình và Mở menu tắt nguồn:\n\n" +
                "1. Nhấn 'Đi đến Cài đặt' bên dưới.\n" +
                "2. Chọn mục 'Ứng dụng đã cài đặt' (Installed apps).\n" +
                "3. Tìm 'Super Button - Hỗ trợ Nút Nguồn Ảo' và gạt BẬT.\n\n" +
                "⚠️ Lưu ý nếu máy báo 'Cài đặt bị hạn chế':\n" +
                "• Ra màn hình chính, nhấn giữ icon app Super Button -> Chọn (i) Thông tin ứng dụng.\n" +
                "• Bấm dấu 3 chấm góc trên bên phải -> Chọn 'Cho phép cài đặt bị hạn chế'."
            )
            .setPositiveButton("Đi đến Cài đặt") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun updatePermissionStatus() {
        val hasOverlay = Settings.canDrawOverlays(this)
        if (hasOverlay) {
            binding.btnGrantOverlay.text = getString(R.string.perm_granted)
            binding.btnGrantOverlay.setIconResource(R.drawable.ic_check)
            binding.btnGrantOverlay.isEnabled = false
        } else {
            binding.btnGrantOverlay.text = getString(R.string.perm_grant_action)
            binding.btnGrantOverlay.icon = null
            binding.btnGrantOverlay.isEnabled = true
        }

        val hasAccessibility = isAccessibilityServiceEnabled()
        if (hasAccessibility) {
            binding.btnGrantAccessibility.text = getString(R.string.perm_granted)
            binding.btnGrantAccessibility.setIconResource(R.drawable.ic_check)
            binding.btnGrantAccessibility.isEnabled = false
        } else {
            binding.btnGrantAccessibility.text = getString(R.string.perm_grant_action)
            binding.btnGrantAccessibility.icon = null
            binding.btnGrantAccessibility.isEnabled = true
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        if (PowerAccessibilityService.isServiceRunning) return true

        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        return enabledServices.contains(packageName)
    }

    private fun setupMainSwitch() {
        syncSwitchState()

        binding.switchEnableButton.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdatingUi) return@setOnCheckedChangeListener

            if (isChecked) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Vui lòng cấp quyền Hiển thị trên ứng dụng khác trước", Toast.LENGTH_SHORT).show()
                    binding.switchEnableButton.isChecked = false
                    return@setOnCheckedChangeListener
                }
                startFloatingService()
            } else {
                stopFloatingService()
            }
            prefManager.isServiceEnabled = isChecked
        }
    }

    private fun syncSwitchState() {
        isUpdatingUi = true
        binding.switchEnableButton.isChecked = FloatingButtonService.isRunning
        isUpdatingUi = false
    }

    private fun startFloatingService() {
        val serviceIntent = Intent(this, FloatingButtonService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopFloatingService() {
        val serviceIntent = Intent(this, FloatingButtonService::class.java).apply {
            action = FloatingButtonService.ACTION_STOP_SERVICE
        }
        startService(serviceIntent)
    }

    private fun notifyPreferenceChanged() {
        val intent = Intent(PreferenceManager.ACTION_PREFERENCE_CHANGED)
        sendBroadcast(intent)
    }

    private fun setupAppearanceSliders() {
        // Active Opacity
        val activeVal = (prefManager.activeOpacity * 100).toInt()
        binding.sliderActiveOpacity.value = activeVal.toFloat().coerceIn(20f, 100f)
        binding.txtActiveOpacity.text = "Độ mờ khi chạm: $activeVal%"
        binding.sliderActiveOpacity.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.txtActiveOpacity.text = "Độ mờ khi chạm: ${value.toInt()}%"
                prefManager.activeOpacity = value / 100f
                notifyPreferenceChanged()
            }
        }

        // Idle Opacity
        val idleVal = (prefManager.idleOpacity * 100).toInt()
        binding.sliderIdleOpacity.value = idleVal.toFloat().coerceIn(5f, 80f)
        binding.txtIdleOpacity.text = "Độ mờ khi tự ẩn (Auto-fade): $idleVal%"
        binding.sliderIdleOpacity.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.txtIdleOpacity.text = "Độ mờ khi tự ẩn (Auto-fade): ${value.toInt()}%"
                prefManager.idleOpacity = value / 100f
                notifyPreferenceChanged()
            }
        }

        // Idle Timeout
        val timeoutVal = prefManager.idleTimeoutSeconds
        binding.sliderIdleTimeout.value = timeoutVal.toFloat().coerceIn(1f, 10f)
        binding.txtIdleTimeout.text = "Thời gian tự mờ khi nhàn rỗi: $timeoutVal giây"
        binding.sliderIdleTimeout.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val sec = value.toInt()
                binding.txtIdleTimeout.text = "Thời gian tự mờ khi nhàn rỗi: $sec giây"
                prefManager.idleTimeoutSeconds = sec
                notifyPreferenceChanged()
            }
        }

        // Button Size
        val sizeVal = prefManager.buttonSizeDp
        binding.sliderButtonSize.value = sizeVal.toFloat().coerceIn(40f, 76f)
        binding.txtButtonSize.text = "Kích thước nút: $sizeVal dp"
        binding.sliderButtonSize.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val size = value.toInt()
                binding.txtButtonSize.text = "Kích thước nút: $size dp"
                prefManager.buttonSizeDp = size
                notifyPreferenceChanged()
            }
        }
    }

    private fun setupMovementSwitches() {
        binding.switchSnapToEdge.isChecked = prefManager.isSnapToEdge
        binding.switchSnapToEdge.setOnCheckedChangeListener { _, isChecked ->
            prefManager.isSnapToEdge = isChecked
            notifyPreferenceChanged()
        }

        binding.switchHaptic.isChecked = prefManager.isHapticEnabled
        binding.switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            prefManager.isHapticEnabled = isChecked
            notifyPreferenceChanged()
        }
    }

    private fun setupGestureSpinners() {
        val actions = ButtonAction.entries.toList()
        val actionNames = actions.map { getString(it.titleResId) }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, actionNames)

        // Single Tap
        binding.spinnerSingleTap.adapter = adapter
        binding.spinnerSingleTap.setSelection(actions.indexOf(prefManager.singleTapAction))
        binding.spinnerSingleTap.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefManager.singleTapAction = actions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Double Tap
        binding.spinnerDoubleTap.adapter = adapter
        binding.spinnerDoubleTap.setSelection(actions.indexOf(prefManager.doubleTapAction))
        binding.spinnerDoubleTap.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefManager.doubleTapAction = actions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Long Press
        binding.spinnerLongPress.adapter = adapter
        binding.spinnerLongPress.setSelection(actions.indexOf(prefManager.longPressAction))
        binding.spinnerLongPress.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefManager.longPressAction = actions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSamsungTips() {
        binding.btnOpenSamsungSettings.setOnClickListener {
            try {
                // Try to open Samsung Motion and Gestures setting
                val intent = Intent("com.samsung.android.motionphoto.MOTION_SETTINGS")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e: Exception) {
                try {
                    // Fallback to advanced feature settings
                    val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                    startActivity(fallbackIntent)
                } catch (ex: Exception) {
                    Toast.makeText(this, "Hãy mở Cài đặt máy -> Tính năng nâng cao", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

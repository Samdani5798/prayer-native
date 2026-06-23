package com.realislamic.prayertimes.ui.qibla

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import com.realislamic.prayertimes.data.local.PreferencesManager
import com.realislamic.prayertimes.databinding.FragmentQiblaBinding
import com.realislamic.prayertimes.util.QiblaUtil

/**
 * Compass-based Qibla finder using the device's accelerometer + magnetometer
 * to compute true device azimuth, then rotates both the compass dial (so North
 * stays correctly oriented) and the qibla needle (so it always points to the
 * Kaaba bearing) as the phone is physically rotated.
 */
class QiblaFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentQiblaBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var qiblaBearing: Double = 0.0
    private var currentDialRotation = 0f
    private var currentNeedleRotation = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQiblaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireContext().getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val prefs = PreferencesManager(requireContext())
        if (prefs.hasLastKnownLocation()) {
            qiblaBearing = QiblaUtil.calculateQiblaBearing(prefs.lastLatitude, prefs.lastLongitude)
            binding.tvQiblaDegreeReading.text = "${qiblaBearing.toInt()}°"
        }

        if (accelerometer == null || magnetometer == null) {
            binding.tvCalibrationHint.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, gravity, 0, 3)
            Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, geomagnetic, 0, 3)
        }

        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        val success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)

        if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthRad = orientation[0]
            var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            azimuthDeg = (azimuthDeg + 360) % 360

            rotateView(binding.ivCompassDial, currentDialRotation, -azimuthDeg)
            currentDialRotation = -azimuthDeg

            val needleTarget = (qiblaBearing - azimuthDeg).toFloat()
            rotateView(binding.ivQiblaNeedle, currentNeedleRotation, needleTarget)
            currentNeedleRotation = needleTarget
        }
    }

    private fun rotateView(view: View, from: Float, to: Float) {
        val animation = RotateAnimation(
            from, to,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        )
        animation.duration = 150
        animation.fillAfter = true
        view.startAnimation(animation)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW ||
            accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
        ) {
            binding.tvCalibrationHint.visibility = View.VISIBLE
        } else {
            binding.tvCalibrationHint.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager.unregisterListener(this)
        _binding = null
    }
}

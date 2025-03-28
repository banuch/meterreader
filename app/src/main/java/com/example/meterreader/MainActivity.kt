package com.example.meterreader
import  android.R
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import com.example.meterreader.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // Define result codes matching the OCR app
    private val OCR_KWH_RESULT_CODE: Int = 666
    private val OCR_KVAH_RESULT_CODE: Int = 667
    private val OCR_RMD_RESULT_CODE: Int = 668
    private val OCR_LT_RESULT_CODE: Int = 669

    // Current selection state
    private var currentMeterType = "KWH"
    private var currentServiceId = "TEST001"

    // Activity result launcher
    private lateinit var ocrActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the activity result launcher
        initActivityResultLauncher()

        // Set up the meter type spinner
        setupMeterTypeSpinner()

        // Set up UI components and listeners
        setupUI()
    }

    private fun initActivityResultLauncher() {
        ocrActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                OCR_KWH_RESULT_CODE -> {
                    val data = result.data
                    val kwhValue = data?.getStringExtra("KWH") ?: "No value returned"
                    val imagePath = data?.getStringExtra("RESULT_VALUE")
                    val editFlag = data?.getStringExtra("rFlag")

                    Log.d(TAG, "Received KWH: $kwhValue, Image: $imagePath, Flag: $editFlag")
                    updateUIWithResults("KWH", kwhValue, imagePath, editFlag)
                }
                OCR_KVAH_RESULT_CODE -> {
                    val data = result.data
                    val kvahValue = data?.getStringExtra("KVAH") ?: "No value returned"
                    val imagePath = data?.getStringExtra("RESULT_VALUE")
                    val editFlag = data?.getStringExtra("rFlag")

                    Log.d(TAG, "Received KVAH: $kvahValue, Image: $imagePath, Flag: $editFlag")
                    updateUIWithResults("KVAH", kvahValue, imagePath, editFlag)
                }
                OCR_RMD_RESULT_CODE -> {
                    val data = result.data
                    val rmdValue = data?.getStringExtra("RMD") ?: "No value returned"
                    val imagePath = data?.getStringExtra("RESULT_VALUE")
                    val editFlag = data?.getStringExtra("rFlag")

                    Log.d(TAG, "Received RMD: $rmdValue, Image: $imagePath, Flag: $editFlag")
                    updateUIWithResults("RMD", rmdValue, imagePath, editFlag)
                }
                OCR_LT_RESULT_CODE -> {
                    val data = result.data
                    val ltValue = data?.getStringExtra("LT") ?: "No value returned"
                    val imagePath = data?.getStringExtra("RESULT_VALUE")
                    val editFlag = data?.getStringExtra("rFlag")

                    Log.d(TAG, "Received LT: $ltValue, Image: $imagePath, Flag: $editFlag")
                    updateUIWithResults("LT", ltValue, imagePath, editFlag)
                }
            }
        }
    }

    private fun setupMeterTypeSpinner() {
        val meterTypes = arrayOf("KWH", "KVAH", "RMD", "LT")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, meterTypes)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        binding.spinnerMeterType.adapter = adapter
        binding.spinnerMeterType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentMeterType = meterTypes[position]
                Log.d(TAG, "Selected meter type: $currentMeterType")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupUI() {
        // Service ID input field
        binding.editTextServiceId.setText(currentServiceId)

        // Launch OCR button
        binding.buttonLaunchOcr.setOnClickListener {
            // Get the service ID from the input field
            currentServiceId = binding.editTextServiceId.text.toString()

            if (currentServiceId.isBlank()) {
                Toast.makeText(this, "Please enter a Service ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            launchOcrApp()
        }

        // Clear button to reset the UI
        binding.buttonClear.setOnClickListener {
            binding.textViewResult.text = "Reading: N/A"
            binding.imageViewMeter.setImageResource(R.color.transparent)
            binding.textViewEditFlag.text = "Edit Status: N/A"
        }
    }

    private fun launchOcrApp() {
        try {
            // Create an intent to launch the OCR app activity
            val intent = Intent()
            intent.setClassName("com.app.autocrop", "com.app.autocrop.MainActivity")

            // Set necessary extras
            intent.putExtra("SERVICE_ID", currentServiceId)
            intent.putExtra("TYPE", currentMeterType)

            // Launch the activity for result
            ocrActivityLauncher.launch(intent)

            Log.d(TAG, "Launched OCR app with Service ID: $currentServiceId, Type: $currentMeterType")
        } catch (e: Exception) {
            Log.e(TAG, "Error launching OCR app: ${e.message}", e)
            Toast.makeText(this, "Error: OCR app not installed or unavailable", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUIWithResults(type: String, value: String, imagePath: String?, editFlag: String?) {
        // Update result text view
        binding.textViewResult.text = "$type Reading: $value"

        // Update edit flag status
        binding.textViewEditFlag.text = "Edit Status: ${editFlag ?: "N/A"}"

        // Load and display the captured image if available
        if (!imagePath.isNullOrEmpty()) {
            try {
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    binding.imageViewMeter.setImageBitmap(bitmap)
                } else {
                    Log.e(TAG, "Image file does not exist: $imagePath")
                    binding.imageViewMeter.setImageResource(R.drawable.ic_menu_report_image)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image: ${e.message}", e)
                binding.imageViewMeter.setImageResource(R.drawable.ic_menu_report_image)
            }
        }
    }

    companion object {
        private const val TAG = "MeterReaderCompanion"
    }
}
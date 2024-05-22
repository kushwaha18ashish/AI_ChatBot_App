package com.mjolnir.aiapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.mjolnir.aiapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

   lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGenerateAns.setOnClickListener {
            val prompt = binding.etPrompt.text.toString()
            hideKeyboard()

            if (prompt.isEmpty()) {
                if(binding.tvAnswer.text.isNotEmpty()){
                    binding.tvAnswer.text=" "
                }
                Toast.makeText(this, "Please enter the question.", Toast.LENGTH_SHORT).show()
            } else {
                binding.etPrompt.clearFocus()
                binding.llButtons.visibility = View.GONE
                binding.pbProgress.visibility = View.VISIBLE

                val generativeModel = GenerativeModel(
                    modelName = "gemini-pro",
                    apiKey = "AIzaSyCqwDPqgQCWFwvlDovqiZWUpsTKxmc_1nw"
                )

                // Launch a coroutine in the lifecycle scope
                lifecycleScope.launch {
                    try {
                        // Perform the network operation on the IO dispatcher
                        val response = withContext(Dispatchers.IO) {
                            generativeModel.generateContent(prompt)
                        }

                        // Update the UI on the main dispatcher
                        withContext(Dispatchers.Main) {
                            binding.pbProgress.visibility = View.GONE
                            binding.tvAnswer.text = response.text.toString()
                            binding.llButtons.visibility = View.VISIBLE
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            binding.pbProgress.visibility = View.GONE
                            Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            binding.llButtons.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }


        binding.btnClear.setOnClickListener {
            binding.etPrompt.text.clear()
        }

    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.etPrompt.windowToken, 0)
    }
}
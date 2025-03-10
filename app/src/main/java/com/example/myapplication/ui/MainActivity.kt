package com.example.myapplication.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogGenderAgeBinding
import com.example.myapplication.network.ServerResponse
import com.example.myapplication.viewModel.MainViewModel


class MainActivity : AppCompatActivity() {
    var binding: DialogGenderAgeBinding? = null
    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showGenderAgeDialog()
        findViewById<ConstraintLayout>(R.id.main).setOnClickListener {
            showGenderAgeDialog()
        }
        viewModel.initSharedPreferences(this)
    }

    private fun showGenderAgeDialog() {
        binding = DialogGenderAgeBinding.inflate(LayoutInflater.from(this))
        binding?.let {
            with(it) {
                val ages = viewModel.ageArray
                    .map { it.toString() }
                    .toMutableList()
                    .apply { add(0, getString(R.string.empty_age)) }
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, ages)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerAge.adapter = adapter
                spinnerAge.selectedItem
                ivMale.setOnClickListener {
                    viewModel.pickedMale()
                }
                ivFemale.setOnClickListener {
                    viewModel.pickedFemale()
                }
                spinnerAge.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if(spinnerAge.selectedItem.toString() != getString(R.string.empty_age)) {
                            viewModel.pickedAge(spinnerAge.selectedItem.toString().toInt())
                        } else viewModel.pickedAge(null)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        viewModel.checkReady()
                    }
                }
                AlertDialog.Builder(this@MainActivity)
                    .setView(binding?.root)
                    .create()
                    .apply {
                        setCancelable(true)
                        show()
                    }
                btnContinue.setOnClickListener {
                    viewModel.sendData()
                }
            }
        }
        observeServerAnswers()
        observeViewState()
    }

    fun observeServerAnswers() {
        viewModel.responseModel.observe(this) { response ->
            when (response) {
                is ServerResponse.Success -> {
                    Toast.makeText(
                        this,
                        getString(R.string.success, response.allowed.toString()),
                        Toast.LENGTH_LONG
                    ).show()
                }

                is ServerResponse.Error -> Toast.makeText(
                    this,
                    response.error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun observeViewState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is MainViewModel.MainViewState.PickedMale -> {
                    male()
                }
                is MainViewModel.MainViewState.PickedFemale -> {
                    female()
                }
                is MainViewModel.MainViewState.ReadyToSend -> {
                    binding?.btnContinue?.isEnabled = true
                    binding?.btnContinue?.background = ContextCompat.getDrawable(this,R.drawable.roundged_green)
                }
                is MainViewModel.MainViewState.NotReadyToSend -> {
                    binding?.btnContinue?.isEnabled = false
                    binding?.btnContinue?.background = ContextCompat.getDrawable(this,R.drawable.roundged_gray)
                }
                is MainViewModel.MainViewState.SavedData -> {
                    val ageIndex = viewModel.ageArray.indexOf(state.user?.age ?: 0)
                    binding?.spinnerAge?.setSelection(ageIndex + 1)
                    if(state.user?.gender == getString(R.string.male_code)) {
                        male()
                    } else female()

                }
            }
        }
    }
    fun female() {
        binding?.ivFemale?.setBackgroundResource(R.drawable.selected_background)
        binding?.ivMale?.setBackgroundResource(0)
        viewModel.user.gender = getString(R.string.female_code)
    }

    fun male() {
        binding?.ivMale?.setBackgroundResource(R.drawable.selected_background)
        binding?.ivFemale?.setBackgroundResource(0)
        viewModel.user.gender = getString(R.string.male_code)
    }
}
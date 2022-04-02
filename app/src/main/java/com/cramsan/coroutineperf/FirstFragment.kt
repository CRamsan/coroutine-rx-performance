package com.cramsan.coroutineperf

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.cramsan.coroutineperf.databinding.FragmentFirstBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class FirstFragment : Fragment() {

    private val viewModel: FragmentViewModel by viewModels()

    private lateinit var binding: FragmentFirstBinding

    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)

        val taskNumberPref = sharedPref.getInt(TEXT_INPUT_TASK_NUMBER_PREF, 100)
        val taskModeCoroutinePref = sharedPref.getBoolean(TASK_MODE_COROUTINE_PREF, true)
        val taskRunConcurrentPref = sharedPref.getBoolean(TASK_RUN_CONCURRENT_PREF, true)

        binding.textInputTaskNumber.setText(taskNumberPref.toString())
        binding.switchRunConcurrently.isChecked = taskRunConcurrentPref
        if (taskModeCoroutinePref) {
            binding.radioButtonCoroutine.isChecked = true
        } else {
            binding.radioButtonRx.isChecked = true
        }

        viewModel.runButtonEnabled.observe(viewLifecycleOwner) {
            binding.buttonRunTasks.isEnabled = it
        }
        viewModel.taskOutput.observe(viewLifecycleOwner) {
            binding.textViewOutput.text = it
        }
        binding.buttonRunTasks.setOnClickListener {
            val taskMode = if (binding.radioButtonRx.isChecked) {
                TaskMode.RX
            } else {
                TaskMode.COROUTINE
            }
            val iterations = binding.textInputTaskNumber.text.toString().toIntOrNull() ?: 0
            val runConcurrently = binding.switchRunConcurrently.isChecked

            with (sharedPref.edit()) {
                putInt(TEXT_INPUT_TASK_NUMBER_PREF, iterations)
                putBoolean(TASK_MODE_COROUTINE_PREF, taskMode == TaskMode.COROUTINE)
                putBoolean(TASK_RUN_CONCURRENT_PREF, runConcurrently)
                apply()
            }

            viewModel.runTasks(taskMode, iterations, runConcurrently)
        }
        binding.buttonKillApp.setOnClickListener {
            requireActivity().finish()
            exitProcess(0)
        }
    }

    companion object {
        const val TEXT_INPUT_TASK_NUMBER_PREF = "TEXT_INPUT_TASK_NUMBER"
        const val TASK_MODE_COROUTINE_PREF = "TASK_MODE_COROUTINE"
        const val TASK_RUN_CONCURRENT_PREF = "TASK_RUN_CONCURRENT"
    }
}
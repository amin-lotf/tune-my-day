package com.aminook.tunemyday.framework.presentation.addroutine

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.interactors.routine.InsertRoutine.Companion.ROUTINE_INSERT_SUCCESS
import com.aminook.tunemyday.business.interactors.routine.UpdateRoutine.Companion.ROUTINE_UPDATE_SUCCESS
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.hideKeyboard
import com.aminook.tunemyday.util.showKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_routine.*


@AndroidEntryPoint
class AddRoutineFragment : BaseFragment(R.layout.fragment_add_routine) {

    private val viewModel: AddRoutineViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: AddRoutineFragmentArgs by navArgs()
        args.routineName?.let {
            edt_add_routine.setText(it.trim())
        }
        viewModel.routineInEditId = args.routineId
        edt_add_routine.showKeyboard()
    }

    override fun onResume() {
        super.onResume()
        subscribeObservers()
        setupToolbar()
    }

    private fun subscribeObservers() {
        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
                val res = stateMessage.response.message
                if (res == ROUTINE_UPDATE_SUCCESS) {
                    findNavController().popBackStack()
                }
            }
            viewModel.activeRoutineUpdated.observe(viewLifecycleOwner) { activeRoutineUpdated ->
                if (activeRoutineUpdated) {
                    findNavController().popBackStack()
                }
            }

        }


    }

    private fun setupToolbar() {
        toolbar_add_routine.apply {
            if (viewModel.routineInEditId == 0L) {
                menu.findItem(R.id.action_delete).isVisible = false
            }

            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_save -> {
                        if (viewModel.routineInEditId == 0L) {
                            viewModel.addRoutine(edt_add_routine.text.toString())
                        } else {
                            viewModel.updateRoutine(edt_add_routine.text.toString())
                        }
                        true
                    }

                    R.id.action_delete -> {
                        viewModel.getRoutineIndex().observe(viewLifecycleOwner) { activeRoutineId ->


                            viewModel.deleteRoutine(activeRoutineId)
                        }

                        true
                    }

                    else -> false

                }

            }
        }

    }

    override fun onPause() {
        requireActivity().hideKeyboard()
        super.onPause()
    }

}
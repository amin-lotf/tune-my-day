package com.aminook.tunemyday.framework.presentation.addroutine

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
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
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.view.*
import kotlinx.android.synthetic.main.fragment_add_program.*
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
        subscribeObservers()
        setupToolbar()
        setupInputField()
    }

    private fun setupInputField() {
        edt_add_routine.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank() &&  txt_add_routine_input_layout.error!=null ){
                txt_add_routine_input_layout.error=null
            }
        }
        edt_add_routine.setOnEditorActionListener { _, actionId, event ->
            var handled=false
            if (actionId== EditorInfo.IME_ACTION_GO){
                toolbar_add_routine.menu.performIdentifierAction(R.id.action_save,0)
                handled=true
            }
            return@setOnEditorActionListener handled
        }
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
            }else{
                title="Edit Plan"
            }

            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_save -> {
                        val routine=edt_add_routine.text.toString().trim()
                        if (routine.isNotBlank()) {
                            if (viewModel.routineInEditId == 0L) {
                                viewModel.addRoutine(edt_add_routine.text.toString())
                            } else {
                                viewModel.updateRoutine(edt_add_routine.text.toString())
                            }
                            true
                        }else{
                            txt_add_routine_input_layout.error="Invalid Name"
                            false
                        }
                    }

                    R.id.action_delete -> {
                        viewModel.getRoutineIndex().observe(viewLifecycleOwner) { activeRoutineId ->

                            requireActivity().hideKeyboard()
                            viewModel.requestDelete(activeRoutineId)
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
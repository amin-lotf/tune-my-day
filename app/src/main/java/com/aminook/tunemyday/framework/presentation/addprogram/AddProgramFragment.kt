package com.aminook.tunemyday.framework.presentation.addprogram

import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Color
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.interactors.program.DeleteProgram.Companion.DELETE_PROGRAM_SUCCEED
import com.aminook.tunemyday.business.interactors.program.UpdateProgram.Companion.PROGRAM_UPDATE_SUCCESS
import com.aminook.tunemyday.framework.presentation.addschedule.AddEditScheduleFragment.Companion.ADD_PROGRAM
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.framework.presentation.common.ProgramColorsAdapter
import com.aminook.tunemyday.util.hideKeyboard
import com.aminook.tunemyday.util.observeOnce
import com.aminook.tunemyday.util.showKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.view.*
import kotlinx.android.synthetic.main.fragment_add_program.*
import javax.inject.Inject


@AndroidEntryPoint
class AddProgramFragment : BaseFragment(R.layout.fragment_add_program) {

    //private val TAG = "aminjoon"

    private var fromAddSchedule = false
    private val viewModel: AddProgramViewModel by viewModels()

    @Inject
    lateinit var colors: List<Color>

    private var programColorsAdapter: ProgramColorsAdapter? = null


    override fun onResume() {
        super.onResume()
        subscribeObservers()

        val args: AddProgramFragmentArgs by navArgs()
        fromAddSchedule = args.fromAddSchedule
        setupToolbar(args.ProgramId)
        setupInputField()
        if (args.ProgramId != 0L) {
            edt_add_program.hint = ""
            viewModel.getProgram(args.ProgramId).observeOnce(viewLifecycleOwner) {
                it?.let { program ->
                    edt_add_program.setText(program.program.name)
                    edt_add_program.setSelection(edt_add_program.text?.length?:0)
                    colors.onEach { it.isChosen = false }
                    val chosenColor = colors.filter { it.value == program.program.color }[0]
                    chosenColor.isChosen = true

                    programColorsAdapter = ProgramColorsAdapter(colors)
                    recycler_program_colors.apply {
                        layoutManager = GridLayoutManager(requireContext(), 4)
                        adapter = programColorsAdapter
                    }
                }
            }
        } else {
            programColorsAdapter = ProgramColorsAdapter(colors)
            recycler_program_colors.apply {
                layoutManager = GridLayoutManager(requireContext(), 4)
                adapter = programColorsAdapter
            }
        }
        edt_add_program.showKeyboard()
    }

    private fun setupInputField() {
        edt_add_program.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank() && txt_add_activity_input_layout.error!=null){
                txt_add_activity_input_layout.error=null
            }
        }
        edt_add_program.setOnEditorActionListener { _, actionId, event ->
            var handled=false
            if (actionId== EditorInfo.IME_ACTION_GO){
                toolbar_add_program.menu.performIdentifierAction(R.id.action_save,0)
                handled=true
            }
            return@setOnEditorActionListener handled
        }
    }

    private fun subscribeObservers() {

        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
                if (stateMessage.response.message== PROGRAM_UPDATE_SUCCESS ||
                        stateMessage.response.message==DELETE_PROGRAM_SUCCEED){
                    findNavController().popBackStack()
                }
            }
        }

        viewModel.newProgramId.observe(viewLifecycleOwner) {
            if (it != 0L) {
                if (fromAddSchedule) {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(
                            ADD_PROGRAM,
                            it
                        )
                }
                findNavController().popBackStack()
            }
        }

    }
        private fun setupToolbar(programId: Long) {

            if (programId == 0L) {
                toolbar_add_program.menu.findItem(R.id.action_delete).isVisible = false
            }

            toolbar_add_program.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_save -> {
                        val programName = edt_add_program.text.toString().trim()
                        if (programName.isNotBlank()) {
                            val color = programColorsAdapter?.selectedColor
                                ?: Color(ContextCompat.getColor(requireContext(), R.color.label9))
                            val program = Program(name = programName, color = color.value)
                            if (programId == 0L) {
                                viewModel.addProgram(program)
                            } else {
                                program.id = programId
                                viewModel.updateProgram(program)
                            }

                            true
                        }else{
                            txt_add_activity_input_layout.error="Invalid Name"
                            false
                        }
                    }

                    R.id.action_delete -> {
                        viewModel.savedProgram?.let { programDetail ->
                            requireActivity().hideKeyboard()
                            viewModel.requestDelete(programDetail)
                        }
                        true
                    }

                    else -> false
                }
            }

            toolbar_add_program.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        override fun onPause() {
            requireActivity().hideKeyboard()
            programColorsAdapter = null
            super.onPause()

        }


    }
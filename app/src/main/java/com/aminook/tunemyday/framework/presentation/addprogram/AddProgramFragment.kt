package com.aminook.tunemyday.framework.presentation.addprogram

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Color
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.interactors.program.InsertProgram
import com.aminook.tunemyday.business.interactors.program.UpdateProgram
import com.aminook.tunemyday.business.interactors.program.UpdateProgram.Companion.PROGRAM_UPDATE_SUCCESS
import com.aminook.tunemyday.framework.presentation.addschedule.AddEditScheduleFragment.Companion.ADD_PROGRAM
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.framework.presentation.common.ProgramColorsAdapter
import com.aminook.tunemyday.util.hideKeyboard
import com.aminook.tunemyday.util.observeOnce
import com.aminook.tunemyday.util.showKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_program.*
import javax.inject.Inject


@AndroidEntryPoint
class AddProgramFragment : BaseFragment(R.layout.fragment_add_program) {

    private val TAG = "aminjoon"

    private var fromAddSchedule = false
    private val viewModel: AddProgramViewModel by viewModels()

    @Inject
    lateinit var colors: List<Color>

    private var programColorsAdapter: ProgramColorsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onResume() {
        super.onResume()
        subscribeObservers()

        val args: AddProgramFragmentArgs by navArgs()
        fromAddSchedule = args.fromAddSchedule
        setupToolbar(args.ProgramId)
        if (args.ProgramId != 0L) {
            edt_add_program.hint = ""
            viewModel.getProgram(args.ProgramId).observeOnce(viewLifecycleOwner) {
                it?.let { program ->
                    edt_add_program.setText(program.program.name)
                    edt_add_program.setSelection(edt_add_program.text.length)
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

    private fun subscribeObservers() {

        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
                if (stateMessage.response.message== PROGRAM_UPDATE_SUCCESS){
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
                        val programName = edt_add_program.text.toString()
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
                    }

                    R.id.action_delete -> {
                        viewModel.savedProgram?.let { programDetail ->
                            onDeleteListener?.onProgramDeleteListener(programDetail)
                        }
                        findNavController().popBackStack()
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
package com.aminook.tunemyday.framework.presentation.ProgramList

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Color
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.interactors.schedule.InsertSchedule
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.framework.presentation.addschedule.OnColorClickListener
import com.aminook.tunemyday.framework.presentation.addschedule.ProgramColorsAdapter
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.DragManageAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_add_program.*
import kotlinx.android.synthetic.main.dialog_add_program.view.*
import kotlinx.android.synthetic.main.fragment_program_list.*
import javax.inject.Inject

@AndroidEntryPoint
class ProgramListFragment : BaseFragment(R.layout.fragment_program_list),
    ProgramListAdapter.ProgramDetailListener, MainActivity.MainActivityListener,
    OnColorClickListener {
    private val TAG = "aminjoon"

    @Inject
    lateinit var colors: List<Color>

    private var programListAdapter: ProgramListAdapter? = null
    private var programColorsAdapter: ProgramColorsAdapter? = null
    private lateinit var addProgramBtmSheetDialog: BottomSheetDialog
    private val viewModel: ProgramListViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setListener(this)
        programListAdapter = ProgramListAdapter().apply {
            setListener(this@ProgramListFragment)

            val callback = DragManageAdapter(
                this,
                requireContext(),
                0,
                ItemTouchHelper.LEFT
            )

            val helper = ItemTouchHelper(callback)

            helper.attachToRecyclerView(recycler_programs_detail)
        }

        recycler_programs_detail.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            adapter = programListAdapter
        }



        subscribeObservers()


    }


    private fun subscribeObservers() {

        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                uiController?.onResponseReceived(stateMessage.response, null)
                if (event.peekContent()?.response?.message == InsertSchedule.INSERT_SCHEDULE_SUCCESS) {
                    //TODO Change
                    Log.d(TAG, "add schedule subscribeObservers: INSERT_SCHEDULE_SUCCESS")
                    //alarmController?.setupAlarms(viewModel.modifiedAlarmIndexes)
                    findNavController().popBackStack()
                }
            }
        }

        viewModel.getAllPrograms().observe(viewLifecycleOwner) {
            Log.d(TAG, "program subscribeObservers: size:${it.size}")
            it.forEach { p ->
                Log.d(TAG, "subscribeObservers: ${p.program.name} ")
            }
            programListAdapter?.submitList(it)
        }
    }

    private fun showAddProgramDialog(programDetail: ProgramDetail? = null) {
        addProgramBtmSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        addProgramBtmSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val view = layoutInflater.inflate(R.layout.dialog_add_program, btm_sheet_add_program)

        programDetail?.let { pd->
            colors.onEach { it.isChosen=false }
            val chosenColor=colors.filter { it.value==pd.program.color }[0]
            chosenColor.isChosen=true

            view.edt_add_program.setText(pd.program.name)

        }

        addProgramBtmSheetDialog.setContentView(view)
        addProgramBtmSheetDialog.show()
        addProgramBtmSheetDialog.setOnDismissListener {
            view.recycler_program_colors.adapter = null
            programColorsAdapter = null
        }
        view.edt_add_program.requestFocus()
        view.btn_save_Program.setOnClickListener {
            if (!view.edt_add_program.text.isNullOrBlank()) {

                    val programName = view.edt_add_program.text.toString()
                    val color = programColorsAdapter?.selectedColor
                        ?: Color(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                if (programDetail==null) {
                    val program = Program(name = programName, color = color.value)
                    viewModel.addProgram(program)
                }
                else{
                    val updatedProgram = Program(
                        id=programDetail.program.id,
                        name = programName,
                        color = color.value)
                    viewModel.updateProgram(updatedProgram)
                }
                addProgramBtmSheetDialog.dismiss()
            } else {
                //TODO(Field must not be empty error
            }
        }

        programColorsAdapter = ProgramColorsAdapter(colors)

        programColorsAdapter?.setOnColorClickListener(this)
        view.recycler_program_colors.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = programColorsAdapter
        }


    }


    override fun onProgramClick(program: ProgramDetail) {
        showAddProgramDialog(program)
    }

    override fun onProgramSwipe(programDetail: ProgramDetail) {
        viewModel.deleteProgram(programDetail)
    }


    override fun onDestroyView() {
        (requireActivity() as MainActivity).setListener(null)
        programListAdapter = null
        super.onDestroyView()
    }

    override fun onFabClick() {
        showAddProgramDialog()
    }

    override fun onSelectColor(color: Color) {

    }
}
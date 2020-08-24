package com.aminook.tunemyday.framework.presentation.addschedule

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Color
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.model.ToDo
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_programs.*
import kotlinx.android.synthetic.main.bottom_sheet_programs.view.*
import kotlinx.android.synthetic.main.dialog_add_program.*
import kotlinx.android.synthetic.main.dialog_add_program.view.*
import kotlinx.android.synthetic.main.fragment_add_schedule.*
import javax.inject.Inject


@AndroidEntryPoint
class AddScheduleFragment : BaseFragment(R.layout.fragment_add_schedule), ProgramClickListener,
    OnColorClickListener {
    private val TAG = "aminjoon"

   @Inject
   lateinit var colors:List<Color>

    @Inject
    lateinit var days:List<Day>

    private val viewModel : AddScheduleViewModel by viewModels()

    private var toDoListAdapter: ToDoListAdapter? = null
    private var programColorsAdapter:ProgramColorsAdapter?=null
    private var programsAdapter:SheetProgramAdapter?=null
    private lateinit var chooseProgramBtnSheetDialog:BottomSheetDialog
    private lateinit var addProgramBtnSheetDialog: BottomSheetDialog



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toDoListAdapter = ToDoListAdapter()
        showPrograms()
        add_schedule_name.setOnClickListener { showPrograms() }

        recycler_schedule_todo.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=toDoListAdapter
            setHasFixedSize(true)
        }

        subscribeObservers()

    }

    private fun subscribeObservers() {
        viewModel.catchDaysOfWeek(1) //TODO(change the number to corresponding day
        viewModel.selectedProgram.observe(viewLifecycleOwner) {
            add_schedule_name.text = it.name

        }
        viewModel.allPrograms.observe(viewLifecycleOwner) {
            programsAdapter?.submitList(it)
        }

        viewModel.chosenDay.observe(viewLifecycleOwner) { day->
            add_schedule_day.text=day.fullName
        }
    }

    private fun showPrograms() {
        chooseProgramBtnSheetDialog=BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme)
        val view=LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_programs,bottom_sheet_programs)

        view.img_add_new_program.setOnClickListener {
            chooseProgramBtnSheetDialog.dismiss()
            showAddProgramDialog()
        }

        programsAdapter=SheetProgramAdapter()
        viewModel.getAllPrograms()

        programsAdapter?.setProgramClickListener(this)
        view.recycler_programs_sheet.apply {
            layoutManager=LinearLayoutManager(requireContext())
            adapter=programsAdapter
            setHasFixedSize(true)
        }
        chooseProgramBtnSheetDialog.setContentView(view)
        chooseProgramBtnSheetDialog.show()
        chooseProgramBtnSheetDialog.setOnDismissListener {
            programsAdapter=null
        }
    }

    private fun showAddProgramDialog() {
        addProgramBtnSheetDialog= BottomSheetDialog(requireContext(),R.style.DialogStyle)
        val view=layoutInflater.inflate(R.layout.dialog_add_program,btn_sheet_add_program)


        addProgramBtnSheetDialog.setContentView(view)
        addProgramBtnSheetDialog.show()
        addProgramBtnSheetDialog.setOnDismissListener {
            programColorsAdapter=null
        }
        view.edt_add_program.requestFocus()
        view.btn_save_Program.setOnClickListener {
           if(!view.edt_add_program.text.isNullOrBlank()){
               val programName=view.edt_add_program.text.toString()
               val color= programColorsAdapter?.selectedColor
                   ?: Color(ContextCompat.getColor(requireContext(),R.color.colorAccent))
               val program=Program(name = programName,color = color.value)
               viewModel.addProgram(program)
               addProgramBtnSheetDialog.dismiss()
           } else{
               //TODO(Field must not be empty error
           }
        }

        programColorsAdapter= ProgramColorsAdapter(colors)

        programColorsAdapter?.setOnColorClickListener(this)
        view.recycler_program_colors.apply {
            layoutManager=GridLayoutManager(requireContext(),4)
            adapter=programColorsAdapter
        }


    }

    override fun onDestroy() {
        toDoListAdapter = null
        super.onDestroy()
    }

    override fun AddProgramClick(program: Program) {
        chooseProgramBtnSheetDialog.dismiss()
        viewModel.bufferChosenProgram(program)
    }

    override fun onSelectColor(color: Color) {
//        //TODO(reset the color list uncheck isChosen)
    }
}
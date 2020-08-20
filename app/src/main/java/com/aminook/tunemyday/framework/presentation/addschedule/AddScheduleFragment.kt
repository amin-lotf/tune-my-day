package com.aminook.tunemyday.framework.presentation.addschedule

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Color
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


    private var toDoListAdapter: ToDoListAdapter? = null
    private var programColorsAdapter:ProgramColorsAdapter?=null
    private var programsAdapter:SheetProgramAdapter?=null
    private lateinit var chooseProgramBtnSheetDialog:BottomSheetDialog
    private lateinit var addProgramBtnSheetDialog: BottomSheetDialog

    //TODO(Add to Hilt)
    private lateinit var colors:MutableList<Color>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toDoListAdapter = ToDoListAdapter()

        add_schedule_name.setOnClickListener { showPrograms() }

        recycler_schedule_todo.apply {
            Log.d(TAG, "onViewCreated: recycler")
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=toDoListAdapter
            setHasFixedSize(true)
        }

        toDoListAdapter?.submitList(listOf(ToDo("2","3",false,false)))

    }

    private fun showPrograms() {
        chooseProgramBtnSheetDialog=BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme)
        val view=LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_programs,bottom_sheet_programs)

        view.img_add_new_program.setOnClickListener {
            chooseProgramBtnSheetDialog.dismiss()
            showAddProgramDialog()
        }

        programsAdapter=SheetProgramAdapter(
            listOf(
                Program(1,"Gym",1),
                Program(1,"Sleep",1),
                Program(1,"Gym",1),
                Program(1,"Gym",1),
                Program(1,"Gym",1),
                Program(1,"Gym",1),
                Program(1,"Gym",1),
                Program(1,"Gym",1),
                Program(1,"Gym",1)
            )
        )
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
               //TODO(Call viewmodel to save)

               addProgramBtnSheetDialog.dismiss()
           } else{
               //TODO(Field must not be empty error
           }
        }
        colors= mutableListOf(
            Color(ContextCompat.getColor(requireContext(),R.color.colorAccent),true),
            Color(ContextCompat.getColor(requireContext(),R.color.colorPrimary),false),
            Color(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark),false)
        )
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
        //TODO(manage chosen program from list)
    }

    override fun onSelectColor(color: Color) {
//        //TODO(reset the color list uncheck isChosen)
    }
}
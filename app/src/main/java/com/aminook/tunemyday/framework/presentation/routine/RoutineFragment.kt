package com.aminook.tunemyday.framework.presentation.routine

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.interactors.schedule.InsertSchedule
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.*
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.view.*
import kotlinx.android.synthetic.main.fragment_routine.*

@AndroidEntryPoint
class RoutineFragment : BaseFragment(R.layout.fragment_routine),
    RoutineAdapter.RoutineAdapterListener {
    private val TAG="aminjoon"
    private var routineAdapter:RoutineAdapter?=null
    private lateinit var addRoutineBtmSheetDialog:BottomSheetDialog
    private val viewModel:RoutineViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()
        toolbar_routines.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        setupAdapter()
    }

    private fun setupAdapter() {
        routineAdapter= RoutineAdapter()
        routineAdapter?.setListener(this)
        recycler_routines.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=routineAdapter
        }
    }

    private fun subscribeObservers() {
        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                uiController?.onResponseReceived(stateMessage.response, null)
            }
        }

        viewModel.routineLoaded.observe(viewLifecycleOwner){
            findNavController().popBackStack()
        }

        viewModel.getRoutines().observe(viewLifecycleOwner){routines->
            routineAdapter?.submitList(routines)
        }
    }

    override fun onRoutineClick(routineEntity: RoutineEntity) {
        viewModel.saveRoutineIndex(routineEntity.id)

    }

    override fun onDeleteRoutineClick(routineEntity: RoutineEntity) {
       viewModel.deleteRoutine(routineEntity)
    }

    override fun onUpdateRouineClick(routineEntity: RoutineEntity) {
        showAddRoutineDialog(routineEntity)
    }



    override fun onPreviewRoutineClick(routineEntity: RoutineEntity) {

    }

    private fun showAddRoutineDialog(routineEntity: RoutineEntity?=null) {
        addRoutineBtmSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        addRoutineBtmSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_routine, btn_sheet_add_routine)
        addRoutineBtmSheetDialog.setContentView(view)
        addRoutineBtmSheetDialog.show()
        routineEntity?.let {
            view.txt_add_routine.setText(it.name)
        }
        view.txt_add_routine.requestFocus()

        view.btn_save_routine.setOnClickListener {
            if (view.txt_add_routine.text.isNotBlank()) {
                if(routineEntity==null) {
                    Log.d(TAG, "showAddRoutineDialog: empty routine")
                    viewModel.addRoutine(view.txt_add_routine.text.toString())

                }else{
                    val updatedRoutine=RoutineEntity(view.txt_add_routine.text.toString()).apply {
                        id=routineEntity.id
                    }
                    viewModel.updateRoutine(updatedRoutine)
                }
                addRoutineBtmSheetDialog.dismiss()
            }
        }
    }


}
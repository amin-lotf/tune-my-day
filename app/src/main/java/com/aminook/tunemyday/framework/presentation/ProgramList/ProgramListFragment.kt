package com.aminook.tunemyday.framework.presentation.ProgramList

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.interactors.schedule.InsertSchedule
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.DragManageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_program_list.*

@AndroidEntryPoint
class ProgramListFragment : BaseFragment(R.layout.fragment_program_list),
    ProgramListAdapter.ProgramDetailListener {
    private val TAG="aminjoon"

    private var programListAdapter:ProgramListAdapter?=null

    private val viewModel:ProgramListViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        programListAdapter=ProgramListAdapter().apply {
            setListener(this@ProgramListFragment)

            val callback= DragManageAdapter(
                this,
                requireContext(),
                0,
                ItemTouchHelper.LEFT
            )

            val helper= ItemTouchHelper(callback)

            helper.attachToRecyclerView(recycler_programs_detail)
        }

        recycler_programs_detail.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            setHasFixedSize(true)
            adapter=programListAdapter
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

        viewModel.getAllPrograms().observe(viewLifecycleOwner){
            Log.d(TAG, "program subscribeObservers: size:${it.size}")
            it.forEach {p->
                Log.d(TAG, "subscribeObservers: ${p.program.name} ")
            }
            programListAdapter?.submitList(it)
        }
    }

    override fun onProgramClick() {

    }

    override fun onProgramSwipe(programDetail: ProgramDetail) {
        viewModel.deleteProgram(programDetail)
    }

    override fun onPause() {
        programListAdapter=null
        super.onPause()
    }
}
package com.aminook.tunemyday.framework.presentation.ProgramList

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Color
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.interactors.schedule.InsertSchedule
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.framework.presentation.addschedule.AddEditScheduleFragmentDirections
import com.aminook.tunemyday.framework.presentation.common.ProgramColorsAdapter
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.DragManageAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_add_program.*
import kotlinx.android.synthetic.main.dialog_add_program.view.*
import kotlinx.android.synthetic.main.fragment_program_list.*
import javax.inject.Inject

@AndroidEntryPoint
class ProgramListFragment : BaseFragment(R.layout.fragment_program_list),
    ProgramListAdapter.ProgramDetailListener {
    private val TAG = "aminjoon"

    private var programListAdapter: ProgramListAdapter? = null
    private val viewModel: ProgramListViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() {
        toolbar_program_list.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        programListAdapter = ProgramListAdapter().apply {
            setListener(this@ProgramListFragment)
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
               onResponseReceived(stateMessage.response)
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



    override fun onProgramClick(program: ProgramDetail) {
        val action=ProgramListFragmentDirections.actionTaskListFragmentToAddProgramFragment(program.program.id)
        findNavController().navigate(action)
    }

    override fun onPause() {
        programListAdapter = null
        super.onPause()
    }




}
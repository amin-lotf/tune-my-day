package com.aminook.tunemyday.framework.presentation.routine

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.SCREEN_WEEKLY
import com.aminook.tunemyday.util.observeOnce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.*
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.view.*
import kotlinx.android.synthetic.main.fragment_routine.*

@AndroidEntryPoint
class RoutineFragment : BaseFragment(R.layout.fragment_routine),
    RoutineAdapter.RoutineAdapterListener {
    private val TAG = "aminjoon"
    private var routineAdapter: RoutineAdapter? = null

    private val viewModel: RoutineViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args:RoutineFragmentArgs by navArgs()
        viewModel.routineId=args.curRoutineId

        subscribeObservers()
        toolbar_routines.setNavigationOnClickListener {
           viewModel.getRoutineIndex().observe(viewLifecycleOwner){
               if (it==0L){
                   val action=RoutineFragmentDirections.actionRoutineFragmentToNoDataFragment()
                   findNavController().navigate(action)
               }else {
                   findNavController().popBackStack()
               }
           }
        }
    }



    override fun onResume() {
        super.onResume()
        setupAdapter()
    }

    private fun setupAdapter() {
        routineAdapter = RoutineAdapter()

        routineAdapter?.setListener(this)
        recycler_routines.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = routineAdapter
        }
    }

    private fun subscribeObservers() {
        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
               onResponseReceived(stateMessage.response)
            }
        }

        viewModel.routineLoaded.observe(viewLifecycleOwner) {
            if (it == true) {
                val action=RoutineFragmentDirections.actionRoutineFragmentToWeeklyListFragment()
                findNavController().navigate(action)
            }
        }

        viewModel.getRoutines().observe(viewLifecycleOwner) { routines ->
            routineAdapter?.submitList(routines)
        }
    }

    override fun onRoutineClick(routineEntity: RoutineEntity) {
        viewModel.saveRoutineIndex(routineEntity.id)


    }

    override fun onUpdateRoutineClick(routineEntity: RoutineEntity) {
        val action=RoutineFragmentDirections.actionRoutineFragmentToAddRoutineFragment(
            routineName = routineEntity.name,
            routineId = routineEntity.id
        )
        findNavController().navigate(action)
    }


    override fun onPause() {
        routineAdapter=null
        super.onPause()
    }

}
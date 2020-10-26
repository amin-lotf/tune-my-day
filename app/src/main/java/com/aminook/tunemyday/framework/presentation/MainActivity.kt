package com.aminook.tunemyday.framework.presentation

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Color
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.*
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.presentation.ProgramList.ProgramListFragmentDirections
import com.aminook.tunemyday.framework.presentation.common.NoDataFragmentDirections
import com.aminook.tunemyday.framework.presentation.dailylist.DailyFragmentDirections
import com.aminook.tunemyday.framework.presentation.routine.RoutineFragmentDirections
import com.aminook.tunemyday.framework.presentation.weeklylist.WeeklyListFragmentDirections
import com.aminook.tunemyday.util.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.view.*
import kotlinx.android.synthetic.main.bottom_sheet_main.*
import kotlinx.android.synthetic.main.bottom_sheet_main.view.*

import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), UIController, AlarmController, OnDeleteListener {

    private val TAG = "aminjoon"
    private val mainViewModel: MainViewModel by viewModels()


    private lateinit var mainBottomSheet: BottomSheetDialog
    var isDialogShowing = false

    @Inject
    lateinit var colors: List<Color>


    @Inject
    lateinit var appFragmentFactory: FragmentFactory

    @Inject
    lateinit var dateUtil: DateUtil

    @Inject
    @DataStoreSettings
    lateinit var dataStore: DataStore<Preferences>
    private var dialogInView: AlertDialog? = null
    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomAppBar()
        subscribeObservers()
    }

    private fun setupBottomAppBar() {


        bottom_app_bar.setNavigationOnClickListener {

            if (!isDialogShowing) {
                mainViewModel.getScreenType().observeOnce(this@MainActivity) { screenType ->

                    mainBottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)

                    val view =
                        layoutInflater.inflate(R.layout.bottom_sheet_main, btm_sheet_main).apply {

                            when (screenType) {
                                SCREEN_DAILY -> {
                                    this.txt_schedule_type.text = "Switch to weekly schedules"
                                    this.txt_schedule_type.setOnClickListener {
//                                        val navOptions = NavOptions.Builder().setPopUpTo(
//                                            R.id.weeklyListFragment, true
//                                        ).build()
                                        navController.navigate(R.id.action_dailyFragment_to_weeklyListFragment)
                                        mainViewModel.setScreenType(SCREEN_WEEKLY)
                                        mainBottomSheet.dismiss()
                                    }

                                }
                                SCREEN_WEEKLY -> {
                                    this.txt_schedule_type.text = "Switch to daily schedules"

                                    this.txt_schedule_type.setOnClickListener {
                                        mainViewModel.setDayIndex(
                                            mainViewModel.dateUtil.curDayIndex
                                        )
//                                        val navOptions = NavOptions.Builder().setPopUpTo(
//                                            R.id.dailyFragment, true
//                                        ).build()
                                        navController.navigate(R.id.action_weeklyListFragment_to_dailyFragment)
                                        mainViewModel.setScreenType(SCREEN_DAILY)
                                        mainBottomSheet.dismiss()
                                    }

                                }
                                else -> {
                                    this.txt_schedule_type.visibility = View.GONE
                                    this.line_under_schedule_type.visibility = View.GONE
                                }
                            }


                            this.txt_load_weekly.setOnClickListener {
                                navController.navigate(R.id.action_global_history)
                                mainBottomSheet.dismiss()
                            }

                            this.txt_show_activity.setOnClickListener {
                                navController.navigate(R.id.action_global_activities)
                                mainBottomSheet.dismiss()
                            }

                            this.txt_add_weekly.setOnClickListener {
                                mainBottomSheet.dismiss()
                                navController.navigate(R.id.action_global_add_routine)

                            }

                        }


                    mainBottomSheet.apply {


                        setOnShowListener {
                            isDialogShowing = true
                        }
                        setOnDismissListener {
                            isDialogShowing = false
                        }
                    }
                    mainBottomSheet.setContentView(view)
                    mainBottomSheet.show()
                }
            }


        }


    }


    private fun subscribeObservers() {
        mainViewModel.stateMessage.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response, null)
            }
        }

        mainViewModel.getScreenType().observeOnce(this) { screenType ->
            Log.d(TAG, "subscribeObservers: observe screen")
            setupNavigation(screenType)
        }

        mainViewModel.getRoutineIndex().observe(this) {
            mainViewModel.routineId = it
            Log.d(TAG, "subscribeObservers: routine id: $it")
            if (it != 0L) {
                Log.d(TAG, "subscribeObservers: before check buffer")
                mainViewModel.buffRoutineId?.let { buffered ->
                    Log.d(TAG, "subscribeObservers: after check buffer")
                    if (it != buffered) {
                        Log.d(
                            TAG,
                            "subscribeObservers: inside routine id: ${mainViewModel.buffRoutineId}"
                        )
                        mainViewModel.setScreenType(SCREEN_WEEKLY)
                        Log.d(TAG, "subscribeObservers: it is initialized")
                        mainViewModel.buffRoutineId?.let {
                            mainViewModel.cancelPrevRoutineAlarms(it)
                        }
                    }
                }

                mainViewModel.getUpcomingAlarms().observe(this) { alarms ->
                    Log.d(TAG, "doWorkk subscribeObservers: get upComings size: ${alarms.size}")
                    alarms.forEach {
                        Log.d(TAG, "doWorkk subscribeObservers: id :${it.id}  ")
                    }
                    mainViewModel.scheduleUpcomingAlarms(alarms)
                }
            } else {
                mainViewModel.setScreenType(SCREEN_BLANK)
//                if (this::navController.isInitialized &&
//                    navController.currentDestination?.id != R.id.noDataFragment &&
//                    navController.currentDestination?.id != R.id.routineFragment &&
//                    navController.currentDestination?.id != R.id.addRoutineFragment
//                ) {
//                    val navOptions = NavOptions.Builder().setPopUpTo(
//                        R.id.noDataFragment, true
//                    ).build()
//                    navController.navigate(R.id.action_global_no_data, null, navOptions)
//                }

            }
            mainViewModel.buffRoutineId = it
        }
    }

    override fun onBackPressed() {
        if (mainViewModel.buffRoutineId == 0L && navController.currentDestination?.id == R.id.routineFragment) {
            val action=RoutineFragmentDirections.actionRoutineFragmentToNoDataFragment()
            navController.navigate(action)
        } else {

            super.onBackPressed()
        }
    }


    private fun setupNavigation(screenType: String) {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment


        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        when (screenType) {
            SCREEN_DAILY -> navGraph.startDestination = R.id.dailyFragment
            SCREEN_WEEKLY -> navGraph.startDestination = R.id.weeklyListFragment
            else -> navGraph.startDestination = R.id.noDataFragment
        }
        navController.graph = navGraph

        navController.addOnDestinationChangedListener { controller, destination, _ ->

            when (destination.id) {
                R.id.weeklyListFragment,
                R.id.dailyFragment,
                R.id.noDataFragment -> {
                    bottom_app_bar.visibility = View.VISIBLE
                    bottom_app_bar.performShow()
                    fab_schedule.show()
                    setupFabClickListener(destination.id)

                }


                R.id.taskListFragment,
                R.id.routineFragment -> {
                    bottom_app_bar.visibility = View.GONE
                    fab_schedule.show()
                    setupFabClickListener(destination.id)
                }

                else -> {
                    bottom_app_bar.performHide()
                    bottom_app_bar.visibility = View.GONE
                    fab_schedule.hide()
                }
            }

        }
    }

    private fun setupFabClickListener(fragmentId: Int) {
        fab_schedule.setOnClickListener {
            val action = when (fragmentId) {
                R.id.noDataFragment -> {
                    NoDataFragmentDirections.actionNoDataFragmentToAddRoutineFragment()
                }

                R.id.weeklyListFragment -> {
                    WeeklyListFragmentDirections.actionWeeklyListFragmentToAddScheduleFragment(
                        SCHEDULE_REQUEST_NEW
                    )
                }
                R.id.dailyFragment -> {
                    DailyFragmentDirections.actionDailyFragmentToAddScheduleFragment(
                        SCHEDULE_REQUEST_NEW
                    )
                }

                R.id.taskListFragment -> {
                    ProgramListFragmentDirections.actionTaskListFragmentToAddProgramFragment()
                }

                R.id.routineFragment -> {
                    RoutineFragmentDirections.actionRoutineFragmentToAddRoutineFragment()
                }

                else -> {
                    return@setOnClickListener
                }
            }

            navController.navigate(action)
        }
    }

    override fun <T> onResponseReceived(response: Response?, data: T?) {

        response?.let {
            when (response.uiComponentType) {
                is UIComponentType.Toast -> {
                    response.message?.let {
                        displayToast(it)
                    }
                }

                is UIComponentType.SnackBar -> {
                    val onDismissCallback = response.uiComponentType.onDismissCallback
                    val undoCallback = response.uiComponentType.undoCallback

                    response.message?.let {
                        displaySnackbar(
                            message = it,
                            snackbarUndoCallback = undoCallback,
                            onDismissCallback = onDismissCallback
                        )
                    }
                }
                is UIComponentType.Dialog -> {
                    displayDialog(response)
                }

//                is UIComponentType.AreYouSureDialog -> {
//
//                    response.message?.let {
//                        dialogInView = areYouSureDialog(
//                            message = it,
//                            callback = response.uiComponentType.callback
//                        )
//                    }
//                }


                else -> {
                }
            }
        }
    }

    private fun areYouSureDialog(
        message: String,
        callback: AreYouSureCallback
    ): AlertDialog {


        return MaterialAlertDialogBuilder(this)
            .setTitle("Are you Sure?")
            .setMessage(message)
            .setPositiveButton("Confirm") { _, _ ->
                callback.proceed()
            }
            .setNegativeButton("Cancel") { _, _ ->
                callback.cancel()
            }.show()

    }

    private fun displayDialog(response: Response) {
        response.message?.let { message ->
            dialogInView = when (response.messageType) {

                is MessageType.Error -> {
                    displayErrorDialog(message = message)
                }

                is MessageType.Success -> {
                    displaySuccessDialog(message = message)
                }

                is MessageType.Info -> {
                    displayInfoDialog(message = message)
                }


                else -> null
            }
        }
    }

    private fun displayToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun displaySnackbar(
        message: String,
        snackbarUndoCallback: SnackbarUndoCallback?,
        onDismissCallback: TodoCallback?
    ) {

        val snackbar = Snackbar.make(
            main_container,
            message,
            Snackbar.LENGTH_LONG
        )
        //snackbar.setAnchorView(fab_schedule.id)
        val lp = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
//        if(anchorSnackToFab){
//            lp.anchorId=layout_weekly_schedule.id
//            lp.anchorGravity=Gravity.BOTTOM
//            lp.gravity = Gravity.BOTTOM
//        }else{
//            lp.anchorId=bottom_navigation.id
//            lp.anchorGravity=Gravity.TOP
//            lp.gravity = Gravity.TOP
//        }
        snackbar.view.layoutParams = lp
        snackbar.setAction(
            R.string.text_undo,
            SnackbarUndoListener(snackbarUndoCallback)
        )
        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                onDismissCallback?.execute()
                super.onDismissed(transientBottomBar, event)

            }
        })
        Log.d(TAG, "displaySnackbar: ")
        snackbar.show()


    }

    private fun displaySuccessDialog(
        message: String?
    ): AlertDialog {
        return MaterialAlertDialogBuilder(this)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("Ok") { _, _ ->

            }.show()

    }

    private fun displayErrorDialog(
        message: String?
    ): AlertDialog {
        return MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("ok") { _, _ ->
            }.show()

    }

    private fun displayInfoDialog(
        message: String?
    ): AlertDialog {
        return MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("ok") { _, _ ->
            }.show()
    }

    override fun onPause() {
        if (dialogInView != null) {
            dialogInView = null
        }
        super.onPause()
    }

    override fun setupAlarms(modifiedAlarmsIndex: List<Long>) {

//        val data = Data.Builder()
//            .putLongArray(MODIFIED_ALARMS_INDEX, modifiedAlarmsIndex.toLongArray())
//            .putString(ACTION_TYPE, TYPE_NEW_SCHEDULE)
//            .build()
//
//        val alarmWorker = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
//            .setInputData(data)
//            .build()
//
//        Log.d(TAG, "setupAlarms: ")
//        WorkManager.getInstance(applicationContext)
//            .enqueueUniqueWork("setAlarms", ExistingWorkPolicy.REPLACE, alarmWorker)
    }

    override fun onProgramDeleteListener(program: ProgramDetail) {
        mainViewModel.deleteProgram(program)
    }

    override fun onScheduleDeleted(schedule: Schedule) {
        mainViewModel.deleteSchedule(schedule)
    }


}


package com.aminook.tunemyday.framework.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.*
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.presentation.weeklylist.WeeklyListFragmentDirections
import com.aminook.tunemyday.util.DAY_INDEX
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_NEW
import com.aminook.tunemyday.util.TodoCallback
import com.aminook.tunemyday.worker.AlarmWorker
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ACTION_TYPE
import com.aminook.tunemyday.worker.AlarmWorker.Companion.MODIFIED_ALARMS_INDEX
import com.aminook.tunemyday.worker.AlarmWorker.Companion.TYPE_NEW_SCHEDULE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), UIController, AlarmController,OnScheduleDeleteListener {

    private val TAG = "aminjoon"
    private val mainViewModel: MainViewModel by viewModels()


    @Inject
    lateinit var appFragmentFactory: FragmentFactory

    @Inject
    lateinit var dateUtil:DateUtil

    @Inject
    @DataStoreSettings
    lateinit var dataStore: DataStore<Preferences>
    private var dialogInView: AlertDialog? = null
    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    private var listener:MainActivityListener?=null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)
        //supportFragmentManager.fragmentFactory = appFragmentFactory
        setupNavigation()
        subscribeObservers()
    }


    private fun subscribeObservers() {
        mainViewModel.stateMessage.observe(this){event->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response,null)
            }
        }
    }

    fun setListener(listener: MainActivityListener?){
        this.listener=listener
    }

    private fun setupNavigation() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment

        val bundle=Bundle()

        navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph,bundle)
        bottom_navigation.setupWithNavController(navController)
        
        navController.addOnDestinationChangedListener{controller, destination, _ ->

            fab_schedule.animate().translationY(0f)


            // First page is main menu
            if( destination.id==R.id.weeklyListFragment || destination.id==R.id.taskListFragment){
                fab_schedule.show()
                fab_schedule.setOnClickListener {
                    when(destination.id){
                        R.id.weeklyListFragment->{
                            val dayIndex= runBlocking { dataStore.data.map { it[DAY_INDEX] }.first() }?: dateUtil.curDayIndex
                            val action = WeeklyListFragmentDirections.actionWeeklyListFragmentToAddScheduleFragment(
                                scheduleRequestType = SCHEDULE_REQUEST_NEW,
                                chosenDay =dayIndex
                            )
                            navController.navigate(action)
                        }

                        R.id.taskListFragment->{
                            listener?.onFabClick()
                        }
                    }

                }
            }else{
                fab_schedule.hide()
            }

            if (destination.id==R.id.addScheduleFragment){
                bottom_navigation.visibility=View.GONE
            }else{
                bottom_navigation.visibility=View.VISIBLE
            }
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

                is UIComponentType.AreYouSureDialog -> {

                    response.message?.let {
                        dialogInView = areYouSureDialog(
                            message = it,
                            callback = response.uiComponentType.callback
                        )
                    }
                }


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

        val data = Data.Builder()
            .putLongArray(MODIFIED_ALARMS_INDEX, modifiedAlarmsIndex.toLongArray())
            .putString(ACTION_TYPE, TYPE_NEW_SCHEDULE)
            .build()

        val alarmWorker = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            .setInputData(data)
            .build()

        Log.d(TAG, "setupAlarms: ")
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork("setAlarms", ExistingWorkPolicy.REPLACE, alarmWorker)
    }

    override fun onScheduleDeleted(schedule: Schedule) {
        mainViewModel.deleteSchedule(schedule)
    }

    interface MainActivityListener{
        fun onFabClick()
    }


}


package com.aminook.tunemyday.framework.presentation.addschedule

import android.animation.LayoutTransition
import android.app.TimePickerDialog
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TimePicker
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.business.interactors.schedule.InsertSchedule.Companion.INSERT_SCHEDULE_SUCCESS
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_ADDED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_LIST_ADDED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_REMOVED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_END
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_START
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.*
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_programs.*
import kotlinx.android.synthetic.main.bottom_sheet_programs.view.*
import kotlinx.android.synthetic.main.fragment_add_edit_schedule.*
import kotlinx.android.synthetic.main.layout_add_alarm.*
import kotlinx.android.synthetic.main.layout_add_alarm.view.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


@AndroidEntryPoint
class AddEditScheduleFragment : BaseFragment(R.layout.fragment_add_edit_schedule),
    ProgramClickListener,
    TimePickerDialog.OnTimeSetListener, AlarmListAdapter.AlarmClickListener{
    //private val TAG = "aminjoon"


    private var isShowingDialog = false
    private var chosenDayIndex = 0
    private var startTime: Time? = null
    private var endTime: Time? = null
    private var timeType: Int = TIME_START

    @Inject
    lateinit var colors: List<Color>

    @Inject
    lateinit var days: List<Day>

    private val viewModel: AddScheduleViewModel by viewModels()

    private var alarmAdapter: AlarmListAdapter? = null

    private var programsAdapter: SheetProgramAdapter? = null

    private lateinit var chooseProgramBtmSheetDialog: BottomSheetDialog


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(ADD_PROGRAM)?.observeOnce(
            viewLifecycleOwner
        ){
            if (it!=0L){
                viewModel.getProgram(it)
                findNavController().currentBackStackEntry?.savedStateHandle?.set(ADD_PROGRAM,0L)
            }
        }



        toolbar_add_schedule.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        alarmAdapter = AlarmListAdapter()

        startTime = Time()
        endTime = Time()
        setClickListeners()
        val chLayoutManager= ChipsLayoutManager.newBuilder(requireContext())
            .setOrientation(ChipsLayoutManager.HORIZONTAL)
            .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
            .build()
        recycler_alarms.apply {
            this.layoutManager =
                chLayoutManager
            viewModel
            adapter = alarmAdapter
        }
        alarmAdapter?.setOnClickListener(this)
        subscribeObservers()
    }




    private fun setClickListeners() {

        layout_todo_group.setOnClickListener {
            val action=AddEditScheduleFragmentDirections.actionAddScheduleFragmentToViewTodoFragment(
                scheduleId = viewModel.scheduleId,
                isSummary = true
            )
            findNavController().navigate(action)
        }

        toolbar_add_schedule.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> {

                    viewModel.validateSchedule()
                    true
                }
                R.id.action_delete -> {


                    onDeleteListener?.onScheduleDeleted(viewModel.addScheduleManager.buffSchedule)
                    findNavController().popBackStack()
                    true
                }
                else -> false
            }
        }
        add_schedule_name.setOnClickListener {
            if (!isShowingDialog) {
                isShowingDialog = true
                showPrograms()
            }
        }
        add_schedule_day.setOnClickListener {
            showDaysDialog()
        }
        add_schedule_start.setOnClickListener { timeView ->
            openTimeDialog(timeView)
        }
        add_schedule_end.setOnClickListener { timeView ->
            openTimeDialog(timeView)
        }
        img_add_alert.setOnClickListener {
                showAlarmDialog()
        }
    }


    private fun subscribeObservers() {
        val args: AddEditScheduleFragmentArgs by navArgs()
        args.scheduleId.let {
            if(it>0){
                viewModel.setScheduleId(args.scheduleId)
            }
        }

        viewModel.isNextDay.observe(viewLifecycleOwner){isNextDay->
            if (isNextDay){
                txt_is_next_day.visibility=View.VISIBLE
            }else{
                txt_is_next_day.visibility=View.INVISIBLE
            }
        }

        viewModel.alarmCounter.observe(viewLifecycleOwner){alarmsAmount->
            if (alarmsAmount>=4){
                img_add_alert.visibility=View.GONE
            }else{
                img_add_alert.visibility=View.VISIBLE
            }

        }

        viewModel.addScheduleManager.initializeSchedule()
        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
                if (stateMessage.response.message == INSERT_SCHEDULE_SUCCESS) {
                    findNavController().popBackStack()
                }
            }
        }


        viewModel.scheduleValidated.observe(viewLifecycleOwner){scheduleValidated->
           if(scheduleValidated){
               viewModel.saveSchedule()
           }
        }

        viewModel.scheduleLoaded.observe(viewLifecycleOwner){loaded->
            if (loaded){
                scroll_view_add_schedule.visibility=View.VISIBLE
                scroll_view_add_schedule.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                scroll_view_add_schedule.layoutTransition.setDuration(70)
                add_schedule_name.postDelayed({
                    if (add_schedule_name.text.isNullOrBlank()){
                        add_schedule_name.text = "Choose an activity"
                    }
                    add_schedule_name.visibility=View.VISIBLE
                },50)
                if(viewModel.requestType==SCHEDULE_REQUEST_EDIT){
                    layout_todo_group.visibility = View.VISIBLE
                }else{
                    toolbar_add_schedule.menu.findItem(R.id.action_delete).isVisible = false
                }

            }else{
                viewModel.getRoutineIndex().observeOnce(viewLifecycleOwner){routineId->
                    if (routineId!=0L){
                        args.scheduleRequestType?.apply {
                            viewModel.processRequest(this, args,routineId)
                            if (this == SCHEDULE_REQUEST_EDIT) {
                                toolbar_add_schedule.title = "Edit Schedule"
                                layout_todo_group.visibility = View.VISIBLE
                                toolbar_add_schedule.menu.findItem(R.id.action_delete).isVisible = true
                            } else {
                               
                                toolbar_add_schedule.title = "New Schedule"

                                add_schedule_name.text = "Choose an activity"
                                add_schedule_name.visibility=View.INVISIBLE
                                toolbar_add_schedule.menu.findItem(R.id.action_delete).isVisible = false
                                layout_todo_group.visibility = View.GONE
                            }
                        }
                    }else{
                        findNavController().popBackStack()
                    }
                }
            }
        }


        viewModel.getScheduleTodosSize(viewModel.scheduleId).observeOnce(viewLifecycleOwner){size->
            val text="Tasks ($size)"
            txt_todo_label.text= text
        }

        viewModel.allPrograms.observe(viewLifecycleOwner) {

            programsAdapter?.submitList(it)
        }

        viewModel.listChanged.observe(viewLifecycleOwner) { modificationType ->

            when (modificationType) {
                ALARM_ADDED -> {
                    val modifiedPos = viewModel.alarmModifiedPosition
                    alarmAdapter?.notifyListChanged(ALARM_ADDED, modifiedPos)

                }
                ALARM_REMOVED -> {

                    val modifiedPos = viewModel.alarmModifiedPosition
                    alarmAdapter?.notifyListChanged(ALARM_REMOVED, modifiedPos)

                }

                ALARM_LIST_ADDED -> {
                    viewModel.getAlarms().apply {
                        alarmAdapter?.submitList(this)
                    }

                }
            }
        }

        viewModel.startTime.observe(viewLifecycleOwner) { startTime ->
            this.startTime = startTime
            add_schedule_start.text = startTime.toString()
        }

        viewModel.endTime.observe(viewLifecycleOwner) { endTime ->
            this.endTime = endTime
            add_schedule_end.text = endTime.toString()
        }

        viewModel.selectedProgram.observe(viewLifecycleOwner) { program ->
            add_schedule_name.text = program.name
            add_schedule_name.visibility=View.VISIBLE
            txt_upper_label.setBackgroundColor(program.color)

        }


        viewModel.chosenDay.observe(viewLifecycleOwner) { day ->
            day?.let {
                add_schedule_day.text = it.fullName
                chosenDayIndex = it.dayIndex
            }

        }
    }


    private fun openTimeDialog(view: View?) {
        when (view) {
            add_schedule_start -> {
                timeType = TIME_START
                TimePickerDialog(
                    requireContext(),
                    this,
                    startTime?.hour ?: 0,
                    startTime?.minute ?: 0,
                    true
                ).show()
            }
            add_schedule_end -> {
                TimePickerDialog(
                    requireContext(),
                    this,
                    endTime?.hour ?: 0,
                    endTime?.minute ?: 0,
                    true
                ).show()
                timeType = TIME_END
            }
        }
    }

    private fun showDaysDialog() {
        MaterialDialog(requireContext()).show {
            icon(R.drawable.ic_day)
            title(text = "Days")

            val days = viewModel.daysOfWeek

            val daysNameList = days.map { day ->
                day.fullName
            }
            listItemsSingleChoice(
                items = daysNameList,
                initialSelection = chosenDayIndex
            ) { _, index, _ ->
                viewModel.updateBufferedDays(days[index])
            }
        }
    }

    private fun showAlarmDialog(alarm: Alarm?=null){
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_add_alarm, bottom_sheet_add_alarm)
        val alarmDialog=MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .show()


        view.chk_at_start.isChecked = alarm?.isAtStart ?: true

        if (!view.chk_at_start.isChecked){
            view.layout_custom_alarm_time.visibility = View.VISIBLE
            view.edt_alarm_hour.isEnabled = true
            view.edt_alarm_minute.isEnabled = true
        }

        view.chk_at_start.setOnClickListener {
            if (view.chk_at_start.isChecked) {
                requireActivity().hideKeyboard()
                view.edt_alarm_hour.apply {
                    setText("")
                    isEnabled = false
                }
                view.edt_alarm_minute.apply {
                    setText("")
                    isEnabled = false
                }

            } else {
                view.layout_custom_alarm_time.visibility = View.VISIBLE
                view.edt_alarm_hour.isEnabled = true
                view.edt_alarm_minute.isEnabled = true
                view.edt_alarm_hour.showKeyboard()
                }


        }

        view.edt_alarm_hour.apply {

            alarm?.let {
                if (!it.isAtStart) {
                    this@apply.setText(it.hourBefore.toString())
                }
            }

            this.addTextChangedListener(object : TimeTextWatcher(23) {
                override fun setText(input: String) {
                    this@apply.setText(input)
                }

                override fun setSelection(index: Int) {
                    this@apply.setSelection(index)
                }

            })

            this.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    this@apply.setText("")
                }
            }
        }

        view.edt_alarm_minute.apply {

            alarm?.let {
                if (!it.isAtStart) {
                    this@apply.setText(it.minuteBefore.toString())
                }
            }

            this.addTextChangedListener(object : TimeTextWatcher(59) {
                override fun setText(input: String) {
                    this@apply.setText(input)
                }

                override fun setSelection(index: Int) {
                    this@apply.setSelection(index)
                }

            })

            this.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && isEnabled) {
                    this@apply.setText("")
                }
            }
        }


        view.btn_add_alarm.setOnClickListener {
            var hour = 0
            var minute = 0
            if (!view.chk_at_start.isChecked) {

                hour = view.edt_alarm_hour.text.toString().toIntOrNull() ?: 0
                minute = view.edt_alarm_minute.text.toString().toIntOrNull() ?: 0
            }
            val alarmInEdit = if (alarm == null) {
                Alarm(hourBefore = hour, minuteBefore = minute)
            } else {
                alarm.copy(hourBefore = hour, minuteBefore = minute
                )
            }

            viewModel.setAlarm(alarmInEdit)
            alarmDialog.dismiss()
        }


    }



    private fun showPrograms() {
        chooseProgramBtmSheetDialog =
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_programs, bottom_sheet_programs)



        view.img_add_new_program.setOnClickListener {
            chooseProgramBtmSheetDialog.dismiss()
            add_schedule_name.visibility=View.INVISIBLE
            val action=AddEditScheduleFragmentDirections.actionAddScheduleFragmentToAddProgramFragment(fromAddSchedule = true)
            findNavController().navigate(action)
        }

        programsAdapter = SheetProgramAdapter()
        viewModel.getAllPrograms()

        viewModel.programListSize.observe(viewLifecycleOwner){
            if(it==0){
                view.txt_no_activity_schedule.visibility=View.VISIBLE
            }else{
                view.txt_no_activity_schedule.visibility=View.GONE
            }
        }

        programsAdapter?.setProgramClickListener(this)
        view.recycler_programs_sheet.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = programsAdapter
            setHasFixedSize(true)
        }
        chooseProgramBtmSheetDialog.setContentView(view)
        chooseProgramBtmSheetDialog.show()
        chooseProgramBtmSheetDialog.setOnDismissListener {
            view.recycler_programs_sheet.adapter = null
            isShowingDialog = false
            programsAdapter = null
        }
    }


    override fun addProgramClick(program: Program) {
        chooseProgramBtmSheetDialog.dismiss()
        viewModel.bufferChosenProgram(program)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        viewModel.setTime(hourOfDay, minute, timeType)
    }

    override fun onRemoveAlarmClick(alarm: Alarm) {

        viewModel.removeAlarm(alarm)
    }



    override fun onDestroyView() {
        recycler_alarms.adapter = null
        super.onDestroyView()
    }

    companion object{
        const val ADD_PROGRAM="add program"
    }

}
package com.aminook.tunemyday.framework.presentation.addschedule

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_LIST_ADDED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_LIST_REMOVED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_END
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_START
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.framework.presentation.common.DaysAdapter
import com.aminook.tunemyday.util.TimeTextWatcher
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_programs.*
import kotlinx.android.synthetic.main.bottom_sheet_programs.view.*
import kotlinx.android.synthetic.main.dialog_add_program.*
import kotlinx.android.synthetic.main.dialog_add_program.view.*
import kotlinx.android.synthetic.main.fragment_add_schedule.*
import kotlinx.android.synthetic.main.layout_add_alarm.*
import kotlinx.android.synthetic.main.layout_add_alarm.view.*
import javax.inject.Inject


@AndroidEntryPoint
class AddScheduleFragment : BaseFragment(R.layout.fragment_add_schedule), ProgramClickListener,
    OnColorClickListener, TimePickerDialog.OnTimeSetListener, AlarmListAdapter.AlarmClickListener {
    private val TAG = "aminjoon"

    private var chosenDayIndex = 0
    private var startTime:Time? = null
    private var endTime:Time? = null
    private var timeType: Int = TIME_START

    @Inject
    lateinit var colors: List<Color>

    @Inject
    lateinit var days: List<Day>

    private val viewModel: AddScheduleViewModel by viewModels()

    private var alarmAdapter: AlarmListAdapter?=null
    private var dayAdapter: DaysAdapter? = null
    private var toDoListAdapter: ToDoListAdapter? = null
    private var programColorsAdapter: ProgramColorsAdapter? = null
    private var programsAdapter: SheetProgramAdapter? = null
    private lateinit var chooseProgramBtmSheetDialog: BottomSheetDialog
    private lateinit var addProgramBtmSheetDialog: BottomSheetDialog
    private lateinit var addAlarmBtmSheetDialog:BottomSheetDialog



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alarmAdapter = AlarmListAdapter()
        toDoListAdapter = ToDoListAdapter()
        startTime= Time()
        endTime= Time()
        showPrograms()
        setClickListeners()

        val arg=arguments

        arg?.getString("request_type")?.let { request->

            viewModel.processRequest(request)
        }
        subscribeObservers()
        recycler_schedule_todo.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = toDoListAdapter
            setHasFixedSize(true)
        }

        recycler_alarms.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            viewModel
            adapter = alarmAdapter
        }
        alarmAdapter?.setOnClickListener(this)

    }

    private fun setClickListeners() {
        add_schedule_name.setOnClickListener { showPrograms() }
        add_schedule_day.setOnClickListener {
            showDaysDialog()
        }
        add_schedule_start.setOnClickListener { timeView ->
            openTimeDialog(timeView)
        }
        add_schedule_end.setOnClickListener { timeView ->
            openTimeDialog(timeView)
        }
        txt_add_alert.setOnClickListener {
            //openAlarmDialog()
            showAlarmDialog()
        }
    }


    private fun subscribeObservers() {
        viewModel.catchDaysOfWeek(0) //TODO(change the number to the day that opened this fragment

//        viewModel.checkReceivedAlarmFromDialog().observe(viewLifecycleOwner){alarm->
//            Log.d(TAG, "subscribeObservers: alarm received")
//            viewModel.setAlarm(alarm)
//        }

        viewModel.getAlarms().apply {

            alarmAdapter?.submitList(this)
        }

        viewModel.listChanged.observe(viewLifecycleOwner) { modificationType ->

            when (modificationType) {
                ALARM_LIST_ADDED -> {

                    val modifiedPos = viewModel.alarmModifiedPosition
                    alarmAdapter?.notifyListChanged(ALARM_LIST_ADDED, modifiedPos)

                }
                ALARM_LIST_REMOVED->{
                    val modifiedPos=viewModel.alarmModifiedPosition
                    viewModel.getAlarms().apply {
                        alarmAdapter?.submitList(this)
                    }
                    alarmAdapter?.notifyListChanged(ALARM_LIST_REMOVED,modifiedPos)
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

        viewModel.selectedProgram.observe(viewLifecycleOwner) {
            add_schedule_name.text = it.name

        }
        viewModel.allPrograms.observe(viewLifecycleOwner) {
            programsAdapter?.submitList(it)
        }

        viewModel.chosenDay.observe(viewLifecycleOwner) { day ->
            day?.let {
                add_schedule_day.text = it.fullName
                chosenDayIndex = it.dayIndex
            }

        }

    }

//    private fun openAlarmDialog(alarm: Alarm?=null) {
//
//        val alarmDialog = AlarmDialog().apply {
//
//        }
//        val action=AddScheduleFragmentDirections.actionAddScheduleFragmentToAlarmDialog(alarm)
//
//        findNavController().navigate(action)
////        alarmDialog.setTargetFragment(this,1)
////        alarmDialog.show(requireActivity().supportFragmentManager, "alarmDialog")
//
//    }

    private fun openTimeDialog(view: View?) {
        when (view) {
            add_schedule_start -> {
                timeType = TIME_START
                TimePickerDialog(
                    requireContext(),
                    this,
                    startTime?.hour?:0,
                    startTime?.minute?:0,
                    true
                ).show()
            }
            add_schedule_end -> {
                TimePickerDialog(requireContext(), this, endTime?.hour?:0, endTime?.minute?:0, true).show()
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
            ) { dialog, index, text ->
                viewModel.updateBufferedDays(days[index])
            }
        }
    }

    private fun showAlarmDialog(alarm: Alarm?=null){
        addAlarmBtmSheetDialog=
            BottomSheetDialog(requireContext(),R.style.DialogStyle)
        val view=LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_add_alarm,bottom_sheet_add_alarm)

        view.chk_at_start.isChecked=alarm?.isAtStart?:true

        view.edt_alarm_hour.apply {

            alarm?.let {
                if(!it.isAtStart){
                    this@apply.setText(it.hourBefore)
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
                if(hasFocus){
                    this@apply.setText("")
                }
            }
        }

        view.edt_alarm_minute.apply {

            alarm?.let {
                if(!it.isAtStart){
                    this@apply.setText(it.minuteBefore)
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
                if(hasFocus){
                    this.setText("")
                }
            }
        }
        view.btn_add_alarm.setOnClickListener {
            var hour=0
            var minute=0
            if(!view.chk_at_start.isChecked){

                hour=view.edt_alarm_hour.text.toString().toIntOrNull()?:0
                minute=view.edt_alarm_minute.text.toString().toIntOrNull()?:0
            }
            val alarmInEdit=if(alarm==null){
                 Alarm(hourBefore = hour,minuteBefore = minute)
            }else{
                alarm.apply {
                    this.hourBefore=hour
                    this.minuteBefore=minute
                }
            }

            viewModel.setAlarm(alarmInEdit)
            addAlarmBtmSheetDialog.dismiss()
        }

        addAlarmBtmSheetDialog.setContentView(view)
        addAlarmBtmSheetDialog.show()
    }

    private fun showPrograms() {
        chooseProgramBtmSheetDialog =
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_programs, bottom_sheet_programs)



        view.img_add_new_program.setOnClickListener {
            chooseProgramBtmSheetDialog.dismiss()
            showAddProgramDialog()
        }

        programsAdapter = SheetProgramAdapter()
        viewModel.getAllPrograms()

        programsAdapter?.setProgramClickListener(this)
        view.recycler_programs_sheet.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = programsAdapter
            setHasFixedSize(true)
        }
        chooseProgramBtmSheetDialog.setContentView(view)
        chooseProgramBtmSheetDialog.show()
        chooseProgramBtmSheetDialog.setOnDismissListener {
            view.recycler_programs_sheet.adapter=null
            programsAdapter = null
        }
    }

    private fun showAddProgramDialog() {
        addProgramBtmSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val view = layoutInflater.inflate(R.layout.dialog_add_program, btn_sheet_add_program)


        addProgramBtmSheetDialog.setContentView(view)
        addProgramBtmSheetDialog.show()
        addProgramBtmSheetDialog.setOnDismissListener {
            view.recycler_program_colors.adapter=null
            programColorsAdapter=null
        }
        view.edt_add_program.requestFocus()
        view.btn_save_Program.setOnClickListener {
            if (!view.edt_add_program.text.isNullOrBlank()) {
                val programName = view.edt_add_program.text.toString()
                val color = programColorsAdapter?.selectedColor
                    ?: Color(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                val program = Program(name = programName, color = color.value)
                viewModel.addProgram(program)
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







    override fun AddProgramClick(program: Program) {
        chooseProgramBtmSheetDialog.dismiss()
        viewModel.bufferChosenProgram(program)
    }

    override fun onSelectColor(color: Color) {
//        //TODO(reset the color list uncheck isChosen)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        viewModel.setTime(hourOfDay, minute, timeType)
    }

    override fun onRemoveClick(alarm: Alarm) {

        viewModel.removeAlarm(alarm)
    }

    override fun onAlarmClick(alarm: Alarm) {
       showAlarmDialog(
           alarm.apply {
               this.inEditMode=true
           }
       )
    }

    override fun onDestroyView() {
        recycler_alarms.adapter=null
        recycler_schedule_todo.adapter=null
        super.onDestroyView()
    }



}
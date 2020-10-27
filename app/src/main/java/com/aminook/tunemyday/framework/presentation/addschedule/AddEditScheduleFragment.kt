package com.aminook.tunemyday.framework.presentation.addschedule

import android.app.TimePickerDialog
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
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.interactors.schedule.InsertSchedule.Companion.INSERT_SCHEDULE_SUCCESS
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_ADDED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_LIST_ADDED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_REMOVED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_END
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_START
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.framework.presentation.common.DaysAdapter
import com.aminook.tunemyday.framework.presentation.common.ProgramColorsAdapter
import com.aminook.tunemyday.framework.presentation.common.TodoAdapter
import com.aminook.tunemyday.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.*
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.view.*
import kotlinx.android.synthetic.main.bottom_sheet_programs.*
import kotlinx.android.synthetic.main.bottom_sheet_programs.view.*
import kotlinx.android.synthetic.main.fragment_add_edit_schedule.*
import kotlinx.android.synthetic.main.layout_add_alarm.*
import kotlinx.android.synthetic.main.layout_add_alarm.view.*
import javax.inject.Inject


@AndroidEntryPoint
class AddEditScheduleFragment : BaseFragment(R.layout.fragment_add_edit_schedule),
    ProgramClickListener,
    TimePickerDialog.OnTimeSetListener, AlarmListAdapter.AlarmClickListener,
    TodoAdapter.ToDoRecyclerViewListener {
    private val TAG = "aminjoon"


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
    private var dayAdapter: DaysAdapter? = null
    private var todoListAdapter: TodoAdapter? = null
    private var programColorsAdapter: ProgramColorsAdapter? = null
    private var programsAdapter: SheetProgramAdapter? = null

    private lateinit var chooseProgramBtmSheetDialog: BottomSheetDialog
    private lateinit var addProgramBtmSheetDialog: BottomSheetDialog
    private lateinit var addAlarmBtmSheetDialog: BottomSheetDialog
    private lateinit var addTodoBtmSheetDialog: BottomSheetDialog


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout_animate_add_schedule.setTransition()
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(ADD_PROGRAM)?.observe(
            viewLifecycleOwner
        ){
            Log.d(TAG, "onViewCreated: got key from add progrram id:$it")
            if (it!=0L){
                viewModel.getProgram(it)
            }
        }

        val args: AddEditScheduleFragmentArgs by navArgs()
        viewModel.getRoutineIndex().observeOnce(viewLifecycleOwner){routineId->
            if (routineId!=0L){
                args.scheduleRequestType?.apply {
                    viewModel.processRequest(this, args,routineId)
                    if (this == SCHEDULE_REQUEST_EDIT) {
                        toolbar_add_schedule.title = "Edit Schedule"

                        toolbar_add_schedule.menu.findItem(R.id.action_delete).isVisible = true
                    } else {
                        toolbar_add_schedule.title = "New Schedule"
                        add_schedule_name.text = "Choose an activity"
                        toolbar_add_schedule.menu.findItem(R.id.action_delete).isVisible = false
                        layout_todo_group.isVisible = false
                    }
                }
            }else{
                findNavController().popBackStack()
            }
        }


        toolbar_add_schedule.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        alarmAdapter = AlarmListAdapter()

        startTime = Time()
        endTime = Time()
        setClickListeners()
        setTodoAdapter()
        recycler_alarms.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            viewModel
            adapter = alarmAdapter
        }
        alarmAdapter?.setOnClickListener(this)
        subscribeObservers()
    }


    private fun setTodoAdapter() {
        todoListAdapter = TodoAdapter(true,viewModel.dateUtil.currentDayInInt)
        todoListAdapter?.setListener(this)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        recycler_schedule_todo.apply {
            this.layoutManager = layoutManager
            adapter = todoListAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled=false
        }
        todoListAdapter?.let {
            val callback= DragManageAdapter(
                it,
                requireContext(),
                ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
                ItemTouchHelper.LEFT
            )

            val helper= ItemTouchHelper(callback)

            helper.attachToRecyclerView(recycler_schedule_todo)
        }


    }

    private fun setClickListeners() {

        img_add_todo_schedule.setOnClickListener {
            todoListAdapter?.let { adapter ->
                Log.d(TAG, "setClickListeners: ${viewModel.scheduleId}")
                showAddTodo(viewModel.scheduleId)
            }
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
            //openAlarmDialog()
            if (!isShowingDialog) {
                isShowingDialog = true
                showAlarmDialog()
            }
        }
    }


    private fun subscribeObservers() {

        viewModel.scheduleValidated.observe(viewLifecycleOwner){
           if(it){
               viewModel.saveSchedule()
           }
        }

        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
                if (stateMessage.response.message == INSERT_SCHEDULE_SUCCESS) {
                    Log.d(TAG, "add schedule subscribeObservers: INSERT_SCHEDULE_SUCCESS")
                    //alarmController?.setupAlarms(viewModel.modifiedAlarmIndexes)
                    findNavController().popBackStack()
                }
            }
        }

        viewModel.scheduleInEditId.observe(viewLifecycleOwner){
            if(it>0){
                viewModel.getTodos(it).observe(viewLifecycleOwner){todos->
                    todoListAdapter?.submitList(todos)
                }
            }
        }



        viewModel.allPrograms.observe(viewLifecycleOwner) {
            programsAdapter?.submitList(it)
        }

        viewModel.getAlarms().apply {

            alarmAdapter?.submitList(this)
        }

        viewModel.listChanged.observe(viewLifecycleOwner) { modificationType ->

            when (modificationType) {
                ALARM_ADDED -> {

                    val modifiedPos = viewModel.alarmModifiedPosition
                    alarmAdapter?.notifyListChanged(ALARM_ADDED, modifiedPos)

                }
                ALARM_REMOVED -> {
                    val modifiedPos = viewModel.alarmModifiedPosition
                    viewModel.getAlarms().apply {
                        alarmAdapter?.submitList(this)
                    }
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
             Log.d(TAG, "subscribeObservers: selectedProgram $program")
            add_schedule_name.text = program.name

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

    private fun showAddTodo(scheduleId: Long, todo: Todo? = null) {
        addTodoBtmSheetDialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_DialogStyle)
        addTodoBtmSheetDialog.behavior.state=BottomSheetBehavior.STATE_EXPANDED
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_todo, btn_sheet_add_todo)
        addTodoBtmSheetDialog.setContentView(view)
        addTodoBtmSheetDialog.show()


        view.txt_add_todo.requestFocus()
        todo?.let {
            view.txt_add_todo.setText(todo.title)
        }
        view.btn_save_todo.setOnClickListener {
            if (!view.txt_add_todo.text.isNullOrBlank()) {
                val task = view.txt_add_todo.text
                if (todo == null) {
                    viewModel.createTodo(scheduleId, task.toString(), false)
                        .observeOnce(viewLifecycleOwner) {
                            todoListAdapter?.submitList(it)
                            todoListAdapter?.let {adapter->
                                //adapter.submitList(it)
                                val pos=recycler_schedule_todo.bottom
                                scroll_view_add_schedule.smoothScrollTo(0,pos)
                            }

                        }
                } else {
//                    viewModel.updateTodo(todo.copy(title = task.toString()))
//                        .observeOnce(viewLifecycleOwner) {
//                            //todoListAdapter?.submitList(it)
//                            addTodoBtmSheetDialog.dismiss()
//                        }
                }
                view.txt_add_todo.setText("")
            } else {
                //TODO(HANDLE BLANK
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


    private fun showAlarmDialog(alarm: Alarm? = null) {
        addAlarmBtmSheetDialog =
            BottomSheetDialog(requireContext(), R.style.ThemeOverlay_DialogStyle)
        addAlarmBtmSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_add_alarm, bottom_sheet_add_alarm)

        addAlarmBtmSheetDialog.setContentView(view)
        addAlarmBtmSheetDialog.show()
        addAlarmBtmSheetDialog.setOnDismissListener {
            isShowingDialog = false
        }

        addAlarmBtmSheetDialog.onAttachedToWindow()
        view.chk_at_start.isChecked = alarm?.isAtStart ?: true

        view.chk_at_start.setOnClickListener {
            if (view.chk_at_start.isChecked) {
                requireActivity().hideKeyboard()
                view.edt_alarm_hour.apply {
                    setText("00")
                    isEnabled = false
                }
                view.edt_alarm_minute.apply {
                    setText("00")
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
                if (hasFocus) {
                    this.setText("")
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
                alarm.apply {
                    this.hourBefore = hour
                    this.minuteBefore = minute
                }
            }

            viewModel.setAlarm(alarmInEdit)
            addAlarmBtmSheetDialog.dismiss()
        }
    }

    private fun showPrograms() {
        Log.d(TAG, "showPrograms: ")
        chooseProgramBtmSheetDialog =
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_programs, bottom_sheet_programs)



        view.img_add_new_program.setOnClickListener {
            chooseProgramBtmSheetDialog.dismiss()
            val action=AddEditScheduleFragmentDirections.actionAddScheduleFragmentToAddProgramFragment(fromAddSchedule = true)
            findNavController().navigate(action)
           // showAddProgramDialog()
        }

        programsAdapter = SheetProgramAdapter()
        Log.d(TAG, "showPrograms: ")
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
            view.recycler_programs_sheet.adapter = null
            isShowingDialog = false
            programsAdapter = null
        }
    }

    override fun onAddTodoClick() {

    }

    override fun AddProgramClick(program: Program) {
        chooseProgramBtmSheetDialog.dismiss()
        viewModel.bufferChosenProgram(program)
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
                this.inEditMode = true
            }
        )
    }

    override fun onDestroyView() {
        recycler_alarms.adapter = null
        recycler_schedule_todo.adapter = null
        super.onDestroyView()
    }

    override fun swipeToDelete(todo: Todo,position: Int) {

        viewModel.deleteTodo(
            todo,
            undoCallback = object : SnackbarUndoCallback {
                override fun undo() {
                    viewModel.addTodo(todo).observeOnce(viewLifecycleOwner) {
                        //todoListAdapter?.submitList(it)
                    }
                }

            },
            onDismissCallback = object : TodoCallback {
                override fun execute() {
                    Log.d(TAG, "execute: todo delete snackbar dismissed")
                }

            }
        )
    }

    override fun updateTodos(todos: List<Todo>) {

    }

    override fun onEditTodoClick(todo: Todo,position: Int) {
        showAddTodo(todo.scheduleId, todo)
    }

    override fun onCheckChanged(todo: Todo,checked:Boolean,position: Int) {
//        viewModel.updateTodo(todo).observeOnce(viewLifecycleOwner) {
//         //   todoListAdapter?.submitList(it)
//        }
    }

    override fun swapItems(fromPosition: Todo, toPosition: Todo) {
        todoListAdapter?.let {

            val destTodo = fromPosition
            val sourceTodo = toPosition
            if (!sourceTodo.isDone) {
                val destPriorityIndex = destTodo.priorityIndex
                val sourcePriorityIndex = sourceTodo.priorityIndex
                destTodo.priorityIndex = sourcePriorityIndex
                sourceTodo.priorityIndex = destPriorityIndex

                viewModel.updateTodos(
                    listOf(destTodo, sourceTodo),
                    destTodo.scheduleId
                ).observeOnce(viewLifecycleOwner) {

                    todoListAdapter?.submitList(it)
                }
            }

        }

    }


    companion object{
        const val ADD_PROGRAM="add program"
    }

}
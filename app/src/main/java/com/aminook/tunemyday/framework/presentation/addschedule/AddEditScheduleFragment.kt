package com.aminook.tunemyday.framework.presentation.addschedule

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.business.domain.state.AreYouSureCallback
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.interactors.schedule.InsertSchedule.Companion.INSERT_SCHEDULE_SUCCESS
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_ADDED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_LIST_ADDED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_REMOVED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_END
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_START
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.framework.presentation.common.DaysAdapter
import com.aminook.tunemyday.framework.presentation.common.ToDoAdapter
import com.aminook.tunemyday.util.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.*
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.view.*
import kotlinx.android.synthetic.main.bottom_sheet_programs.*
import kotlinx.android.synthetic.main.bottom_sheet_programs.view.*
import kotlinx.android.synthetic.main.dialog_add_program.*
import kotlinx.android.synthetic.main.dialog_add_program.view.*
import kotlinx.android.synthetic.main.fragment_add_edit_schedule.*
import kotlinx.android.synthetic.main.layout_add_alarm.*
import kotlinx.android.synthetic.main.layout_add_alarm.view.*
import javax.inject.Inject


@AndroidEntryPoint
class AddEditScheduleFragment : BaseFragment(R.layout.fragment_add_edit_schedule),
    ProgramClickListener,

    OnColorClickListener, TimePickerDialog.OnTimeSetListener, AlarmListAdapter.AlarmClickListener,
    AreYouSureCallback, ToDoAdapter.ToDoRecyclerViewListener {
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
    private var toDoListAdapter: ToDoAdapter? = null
    private var programColorsAdapter: ProgramColorsAdapter? = null
    private var programsAdapter: SheetProgramAdapter? = null

    private lateinit var chooseProgramBtmSheetDialog: BottomSheetDialog
    private lateinit var addProgramBtmSheetDialog: BottomSheetDialog
    private lateinit var addAlarmBtmSheetDialog: BottomSheetDialog
    private lateinit var addTodoBtmSheetDialog: BottomSheetDialog


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        // (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        val args: AddEditScheduleFragmentArgs by navArgs()
        args.scheduleRequestType?.apply {
            viewModel.processRequest(this, args)
            if (this == SCHEDULE_REQUEST_EDIT){
                toolbar_add_schedule.title = "Edit Activity"
                toolbar_add_schedule.menu.findItem(R.id.action_delete).isVisible=true
            }else{
                toolbar_add_schedule.title = "New Activity"
                toolbar_add_schedule.menu.findItem(R.id.action_delete).isVisible=false
                layout_todo_group.isVisible=false
            }
        }



        toolbar_add_schedule.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        alarmAdapter = AlarmListAdapter()

        startTime = Time()
        endTime = Time()
        //showPrograms()
        setClickListeners()




        subscribeObservers()

        setTodoAdapter()



        recycler_alarms.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            viewModel
            adapter = alarmAdapter
        }
        alarmAdapter?.setOnClickListener(this)

    }



    private fun setTodoAdapter() {
        toDoListAdapter = ToDoAdapter()
        toDoListAdapter?.setListener(this)

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val dividerItemDecoration =
            DividerItemDecoration(requireContext(), layoutManager.orientation)
        recycler_schedule_todo.apply {
            this.layoutManager = layoutManager
            adapter = toDoListAdapter
            addItemDecoration(dividerItemDecoration)
        }
        toDoListAdapter?.let {
            val callback= DragManageAdapter(
                it,
                requireContext(),
                ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
                0
            )

            val helper= ItemTouchHelper(callback)

            helper.attachToRecyclerView(recycler_schedule_todo)
            viewModel.getTodos().observeOnce(viewLifecycleOwner) {
                toDoListAdapter?.submitList(it)
            }
        }


    }

    private fun setClickListeners() {

        toolbar_add_schedule.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> {

                    viewModel.validateSchedule(this)
                    true
                }
                R.id.action_delete -> {
                    onScheduleDeleteListener?.onScheduleDeleted(viewModel.addScheduleManager.buffSchedule)
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

        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                uiController?.onResponseReceived(stateMessage.response, null)
                if (event.peekContent()?.response?.message == INSERT_SCHEDULE_SUCCESS) {
                    Log.d(TAG, "add schedule subscribeObservers: INSERT_SCHEDULE_SUCCESS")
                    alarmController?.setupAlarms(viewModel.modifiedAlarmIndexes)
                    findNavController().popBackStack()
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

        viewModel.selectedProgram.observe(viewLifecycleOwner) {program->
            // Log.d(TAG, "subscribeObservers: selectedProgram $it")
            add_schedule_name.text = program.name
            val chosenColor=colors.filter { it.value== program.color}
           // add_schedule_name.setTextColor(chosenColor.first().matchedFontColor)
            txt_upper_label.setBackgroundColor(chosenColor.first().value)

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

    private fun showAddTodo(scheduleId: Long, toDoAdapter: ToDoAdapter, todo: Todo? = null) {
        addTodoBtmSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
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
                            toDoAdapter.submitList(it)
                        }
                } else {
                    viewModel.updateTodo(todo.copy(title = task.toString()))
                        .observeOnce(viewLifecycleOwner) {
                            toDoAdapter.submitList(it)
                            addTodoBtmSheetDialog.dismiss()
                        }
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
            BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_add_alarm, bottom_sheet_add_alarm)

        view.chk_at_start.isChecked = alarm?.isAtStart ?: true

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

        addAlarmBtmSheetDialog.setContentView(view)
        addAlarmBtmSheetDialog.show()
        addAlarmBtmSheetDialog.setOnDismissListener {
            isShowingDialog = false
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
            showAddProgramDialog()
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

    private fun showAddProgramDialog() {
        addProgramBtmSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val view = layoutInflater.inflate(R.layout.dialog_add_program, btm_sheet_add_program)


        addProgramBtmSheetDialog.setContentView(view)
        addProgramBtmSheetDialog.show()
        addProgramBtmSheetDialog.setOnDismissListener {
            view.recycler_program_colors.adapter = null
            programColorsAdapter = null
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
                this.inEditMode = true
            }
        )
    }

    override fun onDestroyView() {
        recycler_alarms.adapter = null
        recycler_schedule_todo.adapter = null
        super.onDestroyView()
    }

    override fun proceed() {

        viewModel.saveSchedule()

    }

    override fun cancel() {
        //do nothing
    }

    override fun setSubTodoAdapter(itemView: ToDoAdapter.ViewHolder, todo: Todo) {

    }

    override fun onDeleteTodoClick(todo: Todo, todoAdapter: ToDoAdapter) {
        viewModel.deleteTodo(
            todo,
            undoCallback = object : SnackbarUndoCallback {
                override fun undo() {
                    viewModel.addTodo(todo).observeOnce(viewLifecycleOwner) {
                        todoAdapter.submitList(it)
                    }
                }

            },
            onDismissCallback = object : TodoCallback {
                override fun execute() {
                    Log.d(TAG, "execute: todo delete snackbar dismissed")
                }

            }
        ).observeOnce(viewLifecycleOwner){
            todoAdapter.submitList(it)
        }
    }

    override fun onEditTodoClick(todo: Todo, todoAdapter: ToDoAdapter) {
        showAddTodo(todo.scheduleId,todoAdapter,todo)
    }

    override fun onCheckChanged(todo: Todo, todoAdapter: ToDoAdapter) {
        viewModel.updateTodo(todo).observeOnce(viewLifecycleOwner){
            todoAdapter.submitList(it)
        }
    }

    override fun swapItems(fromPosition: Int, toPosition: Int, toDoAdapter: ToDoAdapter) {
        val destTodo=toDoAdapter.currentList[toPosition].copy()
        val sourceTodo=toDoAdapter.currentList[fromPosition].copy()
        if(!sourceTodo.isDone){
            val destPriorityIndex=destTodo.priorityIndex
            val sourcePriorityIndex=sourceTodo.priorityIndex
            destTodo.priorityIndex=sourcePriorityIndex
            sourceTodo.priorityIndex=destPriorityIndex

            viewModel.updateTodos(
                listOf(destTodo,sourceTodo),
                destTodo.scheduleId
            ).observeOnce(viewLifecycleOwner){

                toDoAdapter.submitList(it)
            }
        }

    }


}
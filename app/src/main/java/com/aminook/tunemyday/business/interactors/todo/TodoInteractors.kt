package com.aminook.tunemyday.business.interactors.todo

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoInteractors @Inject constructor(
    val insertTodo: InsertTodo,
    val deleteTodo: DeleteTodo,
    val getScheduleTodos: GetScheduleTodos,
    val updateTodo: UpdateTodo,
    val updateTodos: UpdateTodos
) {
}
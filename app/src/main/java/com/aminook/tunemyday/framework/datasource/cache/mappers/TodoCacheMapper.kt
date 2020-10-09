package com.aminook.tunemyday.framework.datasource.cache.mappers

import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.util.EntityMapper
import com.aminook.tunemyday.framework.datasource.cache.model.FullTodo
import com.aminook.tunemyday.framework.datasource.cache.model.TodoEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoCacheMapper @Inject constructor(
    val subTodoCacheMapper: SubTodoCacheMapper
):EntityMapper<FullTodo,Todo> {
    override fun mapFromEntity(entity: FullTodo): Todo {
        return Todo(
            id = entity.todo.id,
            title = entity.todo.title,
            scheduleId = entity.todo.scheduleId,
            programId = entity.todo.programId,
            isDone = entity.todo.isDone,
            dateAdded = entity.todo.dateAdded,
            priorityIndex = entity.todo.priorityIndex,
            isOneTime = entity.todo.isOneTime,
            lastChecked = entity.todo.lastChecked
            ).apply {
            this.subTodos.addAll(entity.subTodos.map { subTodoCacheMapper.mapFromEntity(it) })
        }
    }

    override fun mapToEntity(domainModel: Todo): FullTodo {
        val todoEntity=TodoEntity(
            scheduleId = domainModel.scheduleId,
            programId = domainModel.programId,
            title = domainModel.title,
            priorityIndex = domainModel.priorityIndex,
            isDone = domainModel.isDone,
            dateAdded = domainModel.dateAdded,
            isOneTime = domainModel.isOneTime,
            lastChecked = domainModel.lastChecked
        ).apply {
            id=domainModel.id
        }

        val subTodoEntities=domainModel.subTodos.map { subTodoCacheMapper.mapToEntity(it) }

        return  FullTodo(
            todoEntity,
            subTodoEntities
        )
    }

}
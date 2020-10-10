package com.aminook.tunemyday.framework.datasource.cache.mappers

import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.util.EntityMapper
import com.aminook.tunemyday.framework.datasource.cache.model.TodoEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoCacheMapper @Inject constructor():EntityMapper<TodoEntity,Todo> {
    override fun mapFromEntity(entity: TodoEntity): Todo {
        return Todo(
            id = entity.id,
            title = entity.title,
            scheduleId = entity.scheduleId,
            programId = entity.programId,
            isDone = entity.isDone,
            dateAdded = entity.dateAdded,
            priorityIndex = entity.priorityIndex,
            isOneTime = entity.isOneTime,
            lastChecked = entity.lastChecked
            )
        }

    override fun mapToEntity(domainModel: Todo): TodoEntity {
        return TodoEntity(
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

    }

}
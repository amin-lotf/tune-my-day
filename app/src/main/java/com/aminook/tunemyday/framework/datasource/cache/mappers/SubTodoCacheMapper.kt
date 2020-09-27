package com.aminook.tunemyday.framework.datasource.cache.mappers

import com.aminook.tunemyday.business.domain.model.SubTodo
import com.aminook.tunemyday.business.domain.util.EntityMapper
import com.aminook.tunemyday.framework.datasource.cache.model.SubTodoEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubTodoCacheMapper @Inject constructor():EntityMapper<SubTodoEntity,SubTodo>{
    override fun mapFromEntity(entity: SubTodoEntity): SubTodo {
        return SubTodo(
            id = entity.id,
            title = entity.title,
            isDone = entity.isDone,
            dateAdded = entity.dateAdded,
            todoId = entity.todoId
        )
    }

    override fun mapToEntity(domainModel: SubTodo): SubTodoEntity {
        return  SubTodoEntity(
            title = domainModel.title,
            isDone = domainModel.isDone,
            dateAdded = domainModel.dateAdded,
            todoId = domainModel.todoId
        ).apply {
            this.id=domainModel.id
        }
    }
}
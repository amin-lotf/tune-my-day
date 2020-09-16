package com.aminook.tunemyday.framework.datasource.cache.mappers

import android.util.Log
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.util.EntityMapper
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramEntity
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramCacheMapper @Inject constructor() : EntityMapper<ProgramEntity, Program> {
    override fun mapFromEntity(entity: ProgramEntity): Program {

        return Program(entity.id, entity.name, entity.color)
    }

    override fun mapToEntity(domainModel: Program): ProgramEntity {
        return ProgramEntity(domainModel.name, domainModel.color).apply {
            if (domainModel.id != 0L) {
                this.id = domainModel.id
            }
        }
    }

}
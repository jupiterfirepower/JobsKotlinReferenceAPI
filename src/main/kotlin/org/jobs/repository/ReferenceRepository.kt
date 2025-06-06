package org.jobs.repository

import jakarta.enterprise.context.ApplicationScoped
import org.jobs.Category
import org.jobs.EmploymentType
import org.jobs.repository.IJobsDao
import org.jobs.WorkType

interface IReferenceRepository {
    fun getWorkTypeById(id: Int): WorkType?
    fun getAllWorkTypes(): List<WorkType>
    fun getEmploymentTypeById(id: Int): EmploymentType?
    fun getAllEmploymentTypes(): List<EmploymentType>
    fun getCategoryById(id: Int): Category?
    fun getAllCategories(): List<Category>
}

@ApplicationScoped
class ReferenceRepository(val dao: IJobsDao) : IReferenceRepository {
    override fun getWorkTypeById(id: Int): WorkType? = dao.getWorkTypeById(id)
    override fun getAllWorkTypes(): List<WorkType> = dao.getAllWorkTypes()
    override fun getEmploymentTypeById(id: Int): EmploymentType? = dao.getEmploymentTypeById(id)
    override fun getAllEmploymentTypes(): List<EmploymentType> = dao.getAllEmploymentTypes()
    override fun getCategoryById(id: Int): Category? = dao.getCategoryById(id)
    override fun getAllCategories(): List<Category> = dao.getAllCategories()
}
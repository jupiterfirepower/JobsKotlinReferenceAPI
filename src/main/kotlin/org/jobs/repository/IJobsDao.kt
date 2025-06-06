package org.jobs.repository

import org.jobs.Category
import org.jobs.EmploymentType
import org.jobs.WorkType

public interface IJobsDao {
    fun getWorkTypeById(id: Int): WorkType?
    fun getAllWorkTypes(): List<WorkType>
    fun getEmploymentTypeById(id: Int): EmploymentType?
    fun getAllEmploymentTypes(): List<EmploymentType>
    fun getCategoryById(id: Int): Category?
    fun getAllCategories(): List<Category>
}
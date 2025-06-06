package org.jobs.repository

import com.zaxxer.hikari.HikariDataSource
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import net.samyn.kapper.query
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jobs.Category
import org.jobs.EmploymentType
import org.jobs.WorkType
import org.postgresql.util.PSQLException
import kotlin.use

@ApplicationScoped
class JobsPostgresDao: IJobsDao {
    private lateinit var workTypes: List<WorkType>
    private lateinit var empTypes: List<EmploymentType>
    private lateinit var categories: List<Category>

    override fun getWorkTypeById(id: Int): WorkType? = workTypes.find { it.id == id }
    override fun getAllWorkTypes(): List<WorkType> = workTypes.toList()
    override fun getEmploymentTypeById(id: Int): EmploymentType? = empTypes.find { it.id == id }
    override fun getAllEmploymentTypes(): List<EmploymentType> = empTypes.toList()
    override fun getCategoryById(id: Int): Category? = categories.find { it.id == id }
    override fun getAllCategories(): List<Category> = categories.toList()

    @PostConstruct
    fun init() {
        val dataSource = getHikariDataSource()

        try {
            val data = dataSource.connection.use {
                it.query<WorkType>("select * from fn_get_work_types();")
            }

            workTypes = data.toList()

            val empData = dataSource.connection.use {
                it.query<EmploymentType>("select * from fn_get_emp_types();")
            }

            empTypes = empData.toList()

            val categoriesData = dataSource.connection.use {
                it.query<Category>("select * from fn_get_categories();")
            }

            categories = categoriesData.toList()
        }
        catch(ex: PSQLException) {
            throw ex
        }
        finally {
            dataSource.close()
        }
    }

    @ConfigProperty(name = "config.datasource.jdbc.url")
    lateinit var jdbcUrlValue: String

    @ConfigProperty(name = "config.datasource.username")
    lateinit var usernameValue: String

    @ConfigProperty(name = "config.datasource.password")
    lateinit var passwordValue: String

    fun getHikariDataSource(): HikariDataSource {

        // Create a DataSource object, for example using [HikariCP]
        val dataSource = HikariDataSource().apply {
            jdbcUrl = jdbcUrlValue
            username = usernameValue
            password = passwordValue
        }

        return dataSource
    }
}
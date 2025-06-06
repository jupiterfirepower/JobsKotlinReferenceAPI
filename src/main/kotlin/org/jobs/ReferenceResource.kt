package org.jobs

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.QueryParam

import jakarta.ws.rs.core.Response
import org.jobs.core.ISecurityHelper
import org.jobs.repository.IReferenceRepository
import org.jobs.utils.ISecretKeyStoreUtility

sealed class HttpStatuses {
    companion object {
        const val BAD_REQUEST = 400
        const val INTERNAL_SERVER_ERROR = 500
    }
}

sealed class HeadersKeys {
    companion object {
        const val X_API_KEY = "x-api-key"
        const val X_SECRET_KEY = "x-secret-key"
        const val X_S_NONCE = "x-s-nonce"
        const val X_APP_ID = "x-app-id"
    }
}

sealed class QueryParamKeys {
    companion object {
        const val LANG_ID = "langId"
    }
}

data class WorkType(val id: Int, val name: String, val parentId: Int?, val langId: Int, val created: java.time.Instant, val modified: java.time.Instant)
data class EmploymentType(val id: Int, val name: String, val parentId: Int?, val langId: Int, val created: java.time.Instant, val modified: java.time.Instant)
data class Category(val id: Int, val name: String, val parentId: Int?, val langId: Int, val created: java.time.Instant, val modified: java.time.Instant)

@Path("/api")
class ReferenceResource (val repository: IReferenceRepository,
                         val helper: ISecurityHelper,
                         val utility: ISecretKeyStoreUtility) {
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    fun hello() = "Hello from Quarkus REST"

    @GET
    @Path("/w_types")
    @Produces(MediaType.APPLICATION_JSON)
    fun getWorkTypes(@HeaderParam(HeadersKeys.X_API_KEY) apiKey: String?,
                     @HeaderParam(HeadersKeys.X_SECRET_KEY) secKey: String?,
                     @HeaderParam(HeadersKeys.X_S_NONCE) nonce: String?,
                     @HeaderParam(HeadersKeys.X_APP_ID) appId: String?,
                     @QueryParam(QueryParamKeys.LANG_ID) langId: Int): Response {
        try {
            println("langId: $langId")
            require(langId > 0) { "langId must be non-negative, was $langId" }

            if(helper.isNullOrEmpty(apiKey, secKey, nonce, appId))
              return Response.status(HttpStatuses.BAD_REQUEST).build()

            val valid = helper.isHeadersValid(apiKey!!, secKey!!, nonce!!, appId!!, true)

            //utility.genAppIdCrypto()

            if(!valid.result)
              return Response.status(HttpStatuses.BAD_REQUEST).build()

            val data = repository.getAllWorkTypes().filter { it.langId == langId }
            return Response.ok(data).build()
        }
        catch(e: IllegalArgumentException) {
            println(e)
            return Response.status(HttpStatuses.BAD_REQUEST).build()
        }
        catch(_: Exception) {
            return Response.status(HttpStatuses.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GET
    @Path("/e_types")
    @Produces(MediaType.APPLICATION_JSON)
    fun getEmploymentTypes(@HeaderParam(HeadersKeys.X_API_KEY) apiKey: String?,
                           @HeaderParam(HeadersKeys.X_SECRET_KEY) secKey: String?,
                           @HeaderParam(HeadersKeys.X_S_NONCE) nonce: String?,
                           @HeaderParam(HeadersKeys.X_APP_ID) appId: String?,
                           @QueryParam(QueryParamKeys.LANG_ID) langId: Int): Response {
        try {
            require(langId > 0) { "langId must be non-negative, was $langId" }

            if(helper.isNullOrEmpty(apiKey, secKey, nonce, appId))
                return Response.status(HttpStatuses.BAD_REQUEST).build()

            val valid = helper.isHeadersValid(apiKey!!, secKey!!, nonce!!, appId!!, true)

            if(!valid.result)
                return Response.status(HttpStatuses.BAD_REQUEST).build()

            val data = repository.getAllEmploymentTypes().filter { it.langId == langId }
            return Response.ok(data).build()
        }
        catch(e: IllegalArgumentException) {
            println(e)
            return Response.status(HttpStatuses.BAD_REQUEST).build()
        }
        catch(_: Exception) {
            return Response.status(HttpStatuses.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCategories(@HeaderParam(HeadersKeys.X_API_KEY) apiKey: String?,
                      @HeaderParam(HeadersKeys.X_SECRET_KEY) secKey: String?,
                      @HeaderParam(HeadersKeys.X_S_NONCE) nonce: String?,
                      @HeaderParam(HeadersKeys.X_APP_ID) appId: String?,
                      @QueryParam(QueryParamKeys.LANG_ID) langId: Int): Response {
        try {
            require(langId > 0) { "langId must be non-negative, was $langId" }

            if(helper.isNullOrEmpty(apiKey, secKey, nonce, appId))
                return Response.status(HttpStatuses.BAD_REQUEST).build()

            val valid = helper.isHeadersValid(apiKey!!, secKey!!, nonce!!, appId!!, true)

            if(!valid.result)
                return Response.status(HttpStatuses.BAD_REQUEST).build()

            val data = repository.getAllCategories().filter { it.langId == langId }
            return Response.ok(data).build()
        }
        catch(e: IllegalArgumentException) {
            println(e)
            return Response.status(HttpStatuses.BAD_REQUEST).build()
        }
        catch(e: Exception) {
            println(e)
            return Response.status(HttpStatuses.INTERNAL_SERVER_ERROR).build()
        }
    }
}
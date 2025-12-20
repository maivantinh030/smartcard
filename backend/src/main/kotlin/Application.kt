package com.example

import com.example.database.configureDatabase
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path
import io.ktor.server.response.*
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDatabase()
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unexpected error"))
            )
        }
    }
    configureSerialization()
    configureSecurity()
    configureRouting()
    configureCORS()
}

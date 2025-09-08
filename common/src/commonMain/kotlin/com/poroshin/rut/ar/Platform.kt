package com.poroshin.rut.ar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
package com.poroshin.rut.ar.common.core

sealed class NetworkError(message: String?, cause: Throwable? = null) : RuntimeException(message, cause) {
    class NoConnection(cause: Throwable? = null) : NetworkError("Нет соединения с сервером", cause)

    class RateLimited(val retryAfterSec: Long?, cause: Throwable? = null) :
        NetworkError("Слишком много запросов. Попробуйте позже", cause)

    class Server(val code: Int, cause: Throwable? = null) :
        NetworkError("Ошибка сервера (код $code)", cause)

    class Client(val code: Int, cause: Throwable? = null) :
        NetworkError("Ошибка запроса (код $code)", cause)

    class Unknown(cause: Throwable? = null) : NetworkError("Что-то пошло не так", cause)
}

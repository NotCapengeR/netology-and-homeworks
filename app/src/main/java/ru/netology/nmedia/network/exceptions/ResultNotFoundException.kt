package ru.netology.nmedia.network.exceptions

open class ResultNotFoundException(message: String? = null) : RuntimeException(message)

class PostNotFoundException(message: String? = null) : ResultNotFoundException(message)
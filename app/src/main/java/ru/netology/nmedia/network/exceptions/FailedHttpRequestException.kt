package ru.netology.nmedia.network.exceptions

import retrofit2.HttpException
import retrofit2.Response

class FailedHttpRequestException(response: Response<*>) : HttpException(response)


class EmptyBodyException(response: Response<*>): HttpException(response)
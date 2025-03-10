package com.example.myapplication.network

sealed class ServerResponse {
    data class Success(val allowed: Boolean): ServerResponse()
    data class Error(val error: String): ServerResponse()
}
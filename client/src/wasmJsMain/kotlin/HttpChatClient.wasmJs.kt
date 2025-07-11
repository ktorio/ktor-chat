package io.ktor.chat.client

import io.ktor.client.engine.js.Js

actual fun httpEngine(): io.ktor.client.engine.HttpClientEngineFactory<*> = Js
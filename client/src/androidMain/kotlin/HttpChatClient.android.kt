package io.ktor.chat.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun httpEngine(): HttpClientEngineFactory<*> = CIO
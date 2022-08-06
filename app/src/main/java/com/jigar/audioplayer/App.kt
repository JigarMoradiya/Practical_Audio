package com.jigar.audioplayer

import android.app.Application
import com.jigar.audioplayer.ui.model.AudioViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.provider

class App : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@App))

        bind() from provider { AudioViewModelFactory() }
    }
}
package io.stanwood.debugapp.ioc

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import io.stanwood.debugapp.StanwoodApp
import javax.inject.Singleton


@Singleton
@Component(modules = [AppModule::class, AndroidSupportInjectionModule::class,ServiceBuilderModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: StanwoodApp)
}
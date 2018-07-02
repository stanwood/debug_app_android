package io.stanwood.debugapp.ioc

import android.app.Application
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class AppModule {
    @Singleton
    @Provides
    fun provideResources(context: Application): Resources = context.resources

}




package io.stanwood.debugapp

import android.app.Activity
import android.app.Application
import android.app.Service
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import io.stanwood.debugapp.ioc.DaggerAppComponent
import javax.inject.Inject

class StanwoodApp : Application(), HasActivityInjector, HasServiceInjector {
    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>
    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder().application(this).build().inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityInjector
    override fun serviceInjector(): AndroidInjector<Service> = serviceInjector
}



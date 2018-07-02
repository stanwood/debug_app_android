package io.stanwood.debugapp.ioc;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import io.stanwood.debugapp.features.overlay.OverlayService;

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract OverlayService contributeDebugOverlayService();

}
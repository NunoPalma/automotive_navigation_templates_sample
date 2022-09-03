package com.revolution.navigationtemplatesdemo.core.car;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.car.app.Screen;
import androidx.car.app.Session;

import com.revolution.navigationtemplatesdemo.core.routes.RoutesProcessor;
import com.revolution.navigationtemplatesdemo.ui.menu.MenuScreen;
import com.revolution.navigationtemplatesdemo.ui.permissions.PermissionsScreen;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;

/**
 * Created by Nuno Palma on 30-08-2022
 **/
public class CoreSession extends Session {


    private RoutesProcessor routesProcessor;

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface DemoSessionEntryPoint {
        RoutesProcessor getRoutesProcessor();
    }

    @NonNull
    @Override
    public Screen onCreateScreen(@NonNull Intent intent) {
        provideEntryPoints();

        routesProcessor.acquireLocalData();

        if (getCarContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return new MenuScreen(getCarContext());
        } else {
            return new PermissionsScreen(getCarContext());
        }
    }

    private void provideEntryPoints() {
        CoreSession.DemoSessionEntryPoint demoScreenEntryPoint =
                EntryPointAccessors.fromApplication(getCarContext(), CoreSession.DemoSessionEntryPoint.class);

        this.routesProcessor = demoScreenEntryPoint.getRoutesProcessor();
    }

}

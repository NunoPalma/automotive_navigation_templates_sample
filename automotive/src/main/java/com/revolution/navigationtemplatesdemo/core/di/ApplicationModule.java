package com.revolution.navigationtemplatesdemo.core.di;

import android.content.Context;

import com.revolution.navigationtemplatesdemo.core.routes.RoutesProcessor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.ticofab.androidgpxparser.parser.GPXParser;

/**
 * Created by Nuno Palma on 31-08-2022
 **/

@Module
@InstallIn(SingletonComponent.class)
public class ApplicationModule {

    @Singleton
    @Provides
    public RoutesProcessor providesRoutesProcessor(GPXParser gpxParser,
                                                   @ApplicationContext Context context) {
        return new RoutesProcessor(gpxParser, context.getAssets());
    }

    @Singleton
    @Provides
    public GPXParser providesGPXParser() {
        return new GPXParser();
    }
}

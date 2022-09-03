package com.revolution.navigationtemplatesdemo.ui.tracks;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static androidx.car.app.model.Action.BACK;
import static com.revolution.navigationtemplatesdemo.ui.map.MapActivity.ROUTE_INDEX;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.constraints.ConstraintManager;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

import com.mapbox.geojson.Point;
import com.revolution.navigationtemplatesdemo.R;
import com.revolution.navigationtemplatesdemo.core.routes.RoutesProcessor;
import com.revolution.navigationtemplatesdemo.ui.map.MapActivity;

import java.util.List;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.ticofab.androidgpxparser.parser.domain.Track;

/**
 * Created by Nuno Palma on 30-08-2022
 **/
public class TracksScreen extends Screen {

    private static final int MAX_LIST_ITEMS = 100;
    private final String TAG = "TracksScreen";

    public TracksScreen(@NonNull CarContext carContext) {
        super(carContext);

        provideEntryPoints();

        observeTracksUpdates();
    }

    private RoutesProcessor routesProcessor;

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface DemoScreenEntryPoint {
        RoutesProcessor getRoutesProcessor();
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        Log.d(TAG, "Template updating.");

        ItemList.Builder listBuilder = new ItemList.Builder();
        List<Track> tracks = routesProcessor.getTracks();

        int listLimit =
                Math.min(MAX_LIST_ITEMS,
                        getCarContext().getCarService(ConstraintManager.class).getContentLimit(
                                ConstraintManager.CONTENT_LIMIT_TYPE_LIST));

        for (int i = 0; i < tracks.size(); i++) {
            int j = i;
            if (i >= listLimit) {
                break;
            }

            Track track = tracks.get(i);

            double traveledDistance = routesProcessor.getTraveledDistance(track.getTrackName());

            listBuilder.addItem(
                    new Row.Builder()
                            .setOnClickListener(() ->
                                    getCarContext()
                                            .getMainExecutor()
                                            .execute(() ->
                                                    onClickPlace(j)))
                            .setTitle(getCarContext().getString(R.string.track, (i + 1)))
                            .addText(track.getTrackName())
                            .setImage(CarIcon.PAN)
                            .addText(getCarContext()
                                    .getString(R.string.distance_traveled, traveledDistance))
                            .build());
        }

        return new ListTemplate.Builder()
                .setSingleList(listBuilder.build())
                .setTitle(getCarContext().getString(R.string.track_history))
                .setLoading(false)
                .setHeaderAction(BACK)
                .build();
    }

    private void onClickPlace(int index) {
        Intent intent = new Intent(getCarContext(), MapActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        Log.e(TAG, String.valueOf(index));
        intent.putExtra(ROUTE_INDEX, index);
        getCarContext().startActivity(intent);
    }

    private void provideEntryPoints() {
        DemoScreenEntryPoint demoScreenEntryPoint =
                EntryPointAccessors.fromApplication(getCarContext(), DemoScreenEntryPoint.class);

        this.routesProcessor = demoScreenEntryPoint.getRoutesProcessor();
    }

    private void navigateToPoint(Point point) {
        Uri uri = Uri.parse("geo:0,0?q=" + point.latitude() + "," + point.longitude());
        Intent intent = new Intent(CarContext.ACTION_NAVIGATE, uri);
        intent.setPackage("com.google.android.apps.maps");
        getCarContext().startCarApp(intent);
    }

    private void observeTracksUpdates() {
        Log.d(TAG, "Observing tracks updates.");
        routesProcessor.routesUpdatedObservable
                .doOnNext(shouldReload -> {
                    Log.d(TAG, "Routes update received.");
                    if (shouldReload)
                        onGetTemplate();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        routesProcessor.navigationRequestObservable
                .doOnNext(point -> {
                    navigateToPoint(point);
                })
                .observeOn(Schedulers.io())
                .subscribe();
    }
}

package com.revolution.navigationtemplatesdemo.ui.map;

/**
 * Created by Nuno Palma on 02-09-2022
 **/

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions;
import com.revolution.navigationtemplatesdemo.R;
import com.revolution.navigationtemplatesdemo.core.routes.RoutesProcessor;
import com.revolution.navigationtemplatesdemo.databinding.ActivityMapBinding;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class MapActivity extends AppCompatActivity implements MapContract.View {

    private ActivityMapBinding binding;

    @Inject
    RoutesProcessor routesProcessor;

    private final MapPresenter presenter = new MapPresenter();

    public static final String ROUTE_INDEX = "route_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        presenter.init(routesProcessor, this);

        int index = getIntent().getExtras().getInt(ROUTE_INDEX);
        presenter.onTrackReceived(index);
    }

    @Override
    public void setupMapView() {
        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
    }

    @Override
    public void setHeaderTitle(String text) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(text);
    }

    @Override
    public void setMapData(List<Point> dataPoints, Point centerPoint, Double zoomLevel) {
        binding.mapView.getMapboxMap()
                .setCamera(new CameraOptions
                        .Builder()
                        .zoom(zoomLevel)
                        .center(centerPoint)
                        .build());

        AnnotationPlugin annotationApi = AnnotationPluginImplKt.getAnnotations(binding.mapView);

        PolylineAnnotationOptions polylineAnnotationOptions =
                new PolylineAnnotationOptions()
                        .withPoints(dataPoints)
                        .withLineColor("#ee4e8b")
                        .withLineWidth(5.0);

        PolylineAnnotationManager polylineAnnotationManager =
                PolylineAnnotationManagerKt
                        .createPolylineAnnotationManager(annotationApi, new AnnotationConfig());

        polylineAnnotationManager.create(polylineAnnotationOptions);


        // Marker for the starting point

        Bitmap marker = drawableToBitmap(AppCompatResources.getDrawable(this, R.drawable.ic_marker));

        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withIconSize(0.2)
                .withPoint(dataPoints.get(0))
                .withIconImage(marker);
        PointAnnotationManager pointAnnotationManager =
                PointAnnotationManagerKt
                        .createPointAnnotationManager(annotationApi, new AnnotationConfig());
        pointAnnotationManager.create(pointAnnotationOptions);
        pointAnnotationManager.addClickListener(a -> {
            finish();
            routesProcessor.navigationRequestObservable.onNext(dataPoints.get(0));
            return false;
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
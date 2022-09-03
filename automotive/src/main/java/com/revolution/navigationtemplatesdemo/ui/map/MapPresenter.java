package com.revolution.navigationtemplatesdemo.ui.map;

import com.mapbox.geojson.Point;
import com.revolution.navigationtemplatesdemo.core.routes.RoutesProcessor;

import java.util.List;
import java.util.Locale;

/**
 * Created by Nuno Palma on 02-09-2022
 **/
public class MapPresenter implements MapContract.Presenter {

    private RoutesProcessor routesProcessor;
    private MapContract.View view;

    @Override
    public void init(RoutesProcessor routesProcessor, MapContract.View view) {
        this.routesProcessor = routesProcessor;
        this.view = view;
    }

    @Override
    public void onTrackReceived(int routeIndex) {
        List<Point> points = routesProcessor.getTrackPoint(routeIndex);

        view.setHeaderTitle(String.format(Locale.getDefault(), "Traveled: %.2f Km.",
                routesProcessor.getTraveledDistance(routeIndex)));

        view.setMapData(points,
                routesProcessor.getCenter(routeIndex),
                routesProcessor.getZoomLevel(routeIndex));
    }
}

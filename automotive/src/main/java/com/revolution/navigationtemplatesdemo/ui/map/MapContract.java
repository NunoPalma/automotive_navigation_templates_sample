package com.revolution.navigationtemplatesdemo.ui.map;

import com.mapbox.geojson.Point;
import com.revolution.navigationtemplatesdemo.core.routes.RoutesProcessor;

import java.util.List;

/**
 * Created by Nuno Palma on 02-09-2022
 **/
public interface MapContract {
    interface View {
        void setupMapView();

        void setMapData(List<Point> dataPoints, Point centerPoint, Double zoomLevel);

        void setHeaderTitle(String text);
    }

    interface Presenter {
        void init(RoutesProcessor routesProcessor, MapContract.View view);

        void onTrackReceived(int routeIndex);

    }
}

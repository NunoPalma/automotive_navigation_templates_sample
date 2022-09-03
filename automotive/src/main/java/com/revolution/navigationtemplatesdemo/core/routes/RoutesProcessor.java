package com.revolution.navigationtemplatesdemo.core.routes;

import android.content.res.AssetManager;
import android.util.Log;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Point;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

/**
 * Created by Nuno Palma on 31-08-2022
 **/
public class RoutesProcessor {

    private final String TAG = "RoutesProcessor";

    private final GPXParser gpxParser;
    private final AssetManager assetManager;

    private final List<Track> tracks = new ArrayList<>();

    private final List<List<Point>> trackPoints = new ArrayList<>();

    private final HashMap<String, Double> distanceTraveled = new HashMap<>();
    private static final double EARTH_RADIUS = 6371.0088;

    private final List<String> localGpxFiles = Arrays
            .asList(
                    "1.gpx",
                    "2.gpx",
                    "3.gpx",
                    "4.gpx",
                    "5.gpx",
                    "Australia.gpx");

    public PublishSubject<Boolean> routesUpdatedObservable = PublishSubject.create();

    public PublishSubject<Point> navigationRequestObservable = PublishSubject.create();

    public RoutesProcessor(GPXParser gpxParser, AssetManager assetsManager) {
        Log.d(TAG, "Initiating.");

        this.gpxParser = gpxParser;
        this.assetManager = assetsManager;
    }


    public Single<List<Track>> acquireTracks(String filePath) {
        Gpx parsedGpx = null;
        try {
            InputStream in = assetManager.open(filePath);
            parsedGpx = gpxParser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        if (parsedGpx != null) {
            return Single.just(parsedGpx.getTracks());
        } else {
            Log.e(TAG, "Error parsing gpx track!");

            return Single.just(Collections.emptyList());
        }
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public List<List<Point>> getTrackPoints() {
        return trackPoints;
    }

    public List<Point> getTrackPoint(int i) {
        Log.e(TAG, String.valueOf(i));
        return trackPoints.get(i);
    }

    public Double getTraveledDistance(String trackName) {
        return Optional
                .ofNullable(distanceTraveled.get(trackName))
                .map(Object::toString)
                .map(Double::parseDouble)
                .orElse(0.0);
    }

    public Double getTraveledDistance(int trackIndex) {
        return Optional
                .ofNullable(distanceTraveled.get(tracks.get(trackIndex).getTrackName()))
                .map(Object::toString)
                .map(Double::parseDouble)
                .orElse(0.0);
    }

    public void acquireLocalData() {
        for (String fileName : localGpxFiles) {
            acquireTracks(fileName)
                    .doOnSuccess(this.tracks::addAll)
                    .observeOn(Schedulers.io())
                    .subscribe();
        }

        routesUpdatedObservable.onNext(true);

        convertToPoints();
    }

    private void convertToPoints() {
        for (Track track : tracks) {
            List<TrackPoint> gpxTrackPoints = track.getTrackSegments().get(0).getTrackPoints();
            distanceTraveled.putIfAbsent(track.getTrackName(), 0.0);
            List<Point> points = new ArrayList<>();


            Point previousPoint = null;
            for (TrackPoint trackPoint : gpxTrackPoints) {
                Point point = Point.fromLngLat(trackPoint.getLongitude(), trackPoint.getLatitude());
                points.add(point);

                if (previousPoint != null) {
                    double previousDistance = Optional
                            .ofNullable(distanceTraveled.get(track.getTrackName()))
                            .map(Object::toString)
                            .map(Double::parseDouble)
                            .orElse(0.0);
                    double newDistance = previousDistance + calculateHaversineDistance(previousPoint, point);
                    distanceTraveled.put(track.getTrackName(), newDistance);
                }
                previousPoint = point;
            }
            trackPoints.add(points);
        }
    }

    /**
     * Distance is the angular distance between two points on the surface of a sphere - in this
     * scenario, earth.
     *
     * @param a First point.
     * @param b Second point.
     * @return Haversine distance between two points.
     */
    private Double calculateHaversineDistance(Point a, Point b) {

        double latitudeDistance = Math.toRadians(b.latitude() - a.latitude());
        double longitudeDistance = Math.toRadians(b.longitude() - a.longitude());

        double d = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2) +
                Math.cos(Math.toRadians(a.latitude())) * Math.cos(Math.toRadians(b.latitude())) *
                        Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(d), Math.sqrt(1 - d));
        return EARTH_RADIUS * c;
    }


    /**
     * Creates a bounding box that can be used to customize the map's camera.
     *
     * @param routeIndex Index of the stored route.
     * @return BoundingBox that encapsulates the given route.
     */
    public BoundingBox getBoundingBoxFromRoute(int routeIndex) {
        List<Point> points = trackPoints.get(routeIndex);
        double west, east, south, north;

        List<Double> latitudePoints = points.stream().map(Point::latitude).collect(Collectors.toList());
        List<Double> longitudePoints = points.stream().map(Point::longitude).collect(Collectors.toList());


        south = Collections.min(latitudePoints);
        north = Collections.max(latitudePoints);
        east = Collections.max(longitudePoints);
        west = Collections.min(longitudePoints);

        return BoundingBox.fromLngLats(west - 0.0002, south - 0.0002, east + 0.0002, north + 0.2);
    }


    /**
     * @param routeIndex Index of the stored route.
     * @return Center point based on the requested route.
     */
    public Point getCenter(int routeIndex) {
        List<Point> points = trackPoints.get(routeIndex);

        List<Double> latitudePoints = points.stream()
                .map(Point::latitude).collect(Collectors.toList());
        List<Double> longitudePoints = points.stream()
                .map(Point::longitude).collect(Collectors.toList());


        double centerLatitude = (Collections.min(latitudePoints) + Collections.max(latitudePoints)) / 2;
        double centerLongitude = (Collections.min(longitudePoints) + Collections.max(longitudePoints)) / 2;
        return Point.fromLngLat(centerLongitude, centerLatitude);

    }

    /**
     * Acquires the approximate best value based on traveled distance.
     *
     * @param routeIndex Index of the stored route.
     * @return Zoom value to be used within the map.
     */
    public Double getZoomLevel(int routeIndex) {
        Double distance = getTraveledDistance(tracks.get(routeIndex).getTrackName());

        if (distance < 3)
            return 15.0;
        else if (distance < 5)
            return 14.0;
        else if (distance < 15)
            return 12.0;
        else if (distance < 20)
            return 11.0;
        else if (distance < 35)
            return 10.5;

        return 9.0;
    }
}

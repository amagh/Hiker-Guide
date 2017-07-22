package project.hikerguide.utilities;

import android.net.Uri;

import com.mapbox.mapboxsdk.annotations.Polyline;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticMeasurement;
import org.gavaghan.geodesy.GlobalPosition;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

/**
 * Created by Alvin on 7/21/2017.
 */

public class GpxUtils {

    public static GpxStats getGpxStats(File gpxFile) {
        try {
            // Create a FileInputStream from the Gpx File
            InputStream inStream = new FileInputStream(gpxFile);

            // Parse to a Gpx
            Gpx parsedGpx = new GPXParser().parse(inStream);

            // Cloes the FileInputStream
            inStream.close();

            if (parsedGpx != null) {
                // Get the TrackPoints from the Gpx
                List<TrackPoint> points = parsedGpx.getTracks().get(0).getTrackSegments().get(0).getTrackPoints();

                // Initialize the variables that will be used to calculate the distance and elevation
                double totalDistance = 0.0;
                double low = 0.0;
                double high = 0.0;

                // Init the Geodetic Calculater (Uses Vincenty's formula for higher accuracy)
                GeodeticCalculator calculator = new GeodeticCalculator();

                // Iterate through each pair of points and calculate the distance between them
                for (int i = 0; i < points.size() - 1; i++) {
                    // Ellipsoid.WGS84 is the commonly accepted model for Earth
                    GeodeticMeasurement measurement = calculator.calculateGeodeticMeasurement(Ellipsoid.WGS84,
                            // First point
                            new GlobalPosition(points.get(i).getLatitude(), points.get(i).getLongitude(), points.get(i).getElevation()),
                            // Second point
                            new GlobalPosition(points.get(i + 1).getLatitude(), points.get(i + 1).getLongitude(), points.get(i + 1).getElevation()));

                    // Add the distance between the two points to the total distance
                    totalDistance += measurement.getPointToPointDistance();

                    // Find the high and low elevation
                    if (i == 0) {
                        // For the first point, set the respective elevations for the low and high
                        boolean firstHigher = points.get(i).getElevation() > points.get(i + 1).getElevation();
                        if (firstHigher) {
                            low = points.get(i + 1).getElevation();
                            high = points.get(i).getElevation();
                        } else {
                            low = points.get(i).getElevation();
                            high = points.get(i + 1).getElevation();
                        }
                    } else {
                        // For all other iterations, set the high elevation if higher or low
                        // elevation if lower
                        double elevation = points.get(i + 1).getElevation();

                        if (elevation < low) {
                            low = elevation;
                        } else if (elevation > high) {
                            high = elevation;
                        }
                    }
                }

                // Create a GpxStats Object from the stats
                GpxStats gpxStats = new GpxStats();
                gpxStats.distance = totalDistance;
                gpxStats.elevation = high - low;

                return gpxStats;
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
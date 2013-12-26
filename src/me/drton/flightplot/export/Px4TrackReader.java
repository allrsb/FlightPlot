package me.drton.flightplot.export;

import me.drton.flightplot.FormatErrorException;
import me.drton.flightplot.PX4LogReader;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ada on 23.12.13.
 */
public class PX4TrackReader implements TrackReader {
    private static final String GPS_TIME = "GPS.GPSTime";
    private static final String GPS_LON = "GPS.Lon";
    private static final String GPS_LAT = "GPS.Lat";
    private static final String GPS_ALT = "GPS.Alt";
    private static final String GPS_FIXTYPE = "GPS.FixType";
    private static final int REQUIRED_FIXTYPE = 3;

    private final PX4LogReader reader;
    private long timeLast = 0;

    public PX4TrackReader(PX4LogReader reader) throws IOException, FormatErrorException {
        this.reader = reader;
        reset();
    }

    public void reset() throws IOException, FormatErrorException {
        reader.seek(0);
    }

    @Override
    public TrackPoint readNextPoint() throws IOException, FormatErrorException {
        Map<String, Object> data = new HashMap<String, Object>();
        while (true) {
            try {
                reader.readUpdate(data);
            } catch (EOFException e) {
                break;  // End of file
            }
            Long timeGPS = (Long) data.get(GPS_TIME);
            if (timeGPS != null) {
                long time = timeGPS / 1000000;
                Integer fixType = (Integer) data.get(GPS_FIXTYPE);
                Double lat = (Double) data.get(GPS_LAT);
                Double lon = (Double) data.get(GPS_LON);
                Float alt = (Float) data.get(GPS_ALT);
                if (time > timeLast && fixType != null && fixType >= REQUIRED_FIXTYPE &&
                        lat != null && lon != null && alt != null) {
                    timeLast = time;
                    return new TrackPoint(lat, lon, alt, time * 1000);
                }
            }
        }
        return null;
    }
}
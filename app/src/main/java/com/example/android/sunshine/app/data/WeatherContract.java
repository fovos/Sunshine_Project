/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.data;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * https://developer.android.com/reference/android/provider/BaseColumns.html
 * Database Contract --
 * It is an agreement between our Data Model and our Views, describing how our data are stored.
 * We use inner classes for the creation of Weather and Location Tables
 * We don't have to have already created the database. This will happen through the WeatherDbHelper class
 * which extends the SQLiteOpenHelper class.
 * Defines table and column names for the weather database.
 */
public class WeatherContract {

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /*
        Inner class that defines the table contents of the location table
        Students: This is where you will add the strings.  (Similar to what has been
        done for WeatherEntry)
     */
    public static final class LocationEntry implements BaseColumns {
        //constant table name
        public static final String TABLE_NAME = "location";

        //constant column names
        public static final String COLUMN_LOCATION_SETTING="location_Setting";
        public static final String COLUMN_COORD_LAT="coord_lat";
        public static final String COLUMN_COORD_LONG="coord_long";
        public static final String COLUMN_CITY_NAME="city_name";
    }

    /* Inner class that defines the table contents of the weather table
    * Prokeitai gia mia inner klasi i opoia exei ta xaraktiristika tou pinaka pou theloume
    * na dimiourgisoume, opws onoma pinaka kai onomata pediwn*/
    public static final class WeatherEntry implements BaseColumns {

        public static final String TABLE_NAME = "weather";

        // Column with the foreign key into the location table.
        //will be used as foreign key to locationentry COLUMN_LOC_SETTING
        public static final String COLUMN_LOC_KEY = "location_id";
        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE = "date";
        // Weather id as returned by API, to identify the icon to be used
        public static final String COLUMN_WEATHER_ID = "weather_id";

        // Short description and long description of the weather, as provided by API.
        // e.g "clear" vs "sky is clear".
        public static final String COLUMN_SHORT_DESC = "short_desc";

        // Min and max temperatures for the day (stored as floats)
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";

        // Humidity is stored as a float representing percentage
        public static final String COLUMN_HUMIDITY = "humidity";

        // Humidity is stored as a float representing percentage
        public static final String COLUMN_PRESSURE = "pressure";

        // Windspeed is stored as a float representing windspeed  mph
        public static final String COLUMN_WIND_SPEED = "wind";

        // Degrees are meteorological degrees (e.g, 0 is north, 180 is south).  Stored as floats.
        public static final String COLUMN_DEGREES = "degrees";
    }
}

package dev.sanskar.fileboi.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {

    public static String getFormattedDateTimeString(String dateTimeString) {

        // sample time from server currently : "2020-07-19T15:32:04.914116"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        Date date = null;
        try {
            date = format.parse(dateTimeString.replaceAll("Z$", "+0000")); ;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.setTime(date);
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        SimpleDateFormat sdfDate = new SimpleDateFormat("EEE, d MMM, ''yy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a");

        java.util.Date currenTimeZone = calendar.getTime();
        return sdfDate.format(currenTimeZone)+"-"+sdfTime.format(currenTimeZone);

    }

}

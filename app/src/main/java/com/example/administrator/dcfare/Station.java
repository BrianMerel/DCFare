package com.example.administrator.dcfare;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 4/6/2016.
 */
public class Station {
    public static final Map<Integer,String> STATION_MAP;

    static {
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(1, "Metro Center");
        map.put(4,"Farragut North");
        map.put(6,"Dupont Circle");
        map.put(7,"Woodley Park-Zoo/Adams Morgan");
        map.put(8,"Cleveland Park");
        map.put(9,"Van Ness-UDC");
        map.put(10,"Tenleytown-AU");
        map.put(11,"Friendship Heights");
        map.put(12,"Bethesda");
        map.put(13,"Medical Center");
        map.put(14,"Grosvenor-Strathmore");
        map.put(15,"White Flint");
        map.put(16,"Twinbrook");
        map.put(17,"Rockville");
        map.put(18,"Shady Grove");
        map.put(21,"Gallery Pl-Chinatown");
        map.put(23,"Judiciary Square");
        map.put(25,"Union Station");
        map.put(26,"Rhode Island Ave-Brentwood");
        map.put(27,"Brookland-CUA");
        map.put(28,"Fort Totten");
        map.put(29,"Takoma");
        map.put(31,"Silver Spring");
        map.put(32,"Forest Glen");
        map.put(33,"Wheaton");
        map.put(34,"Glenmont");
        map.put(36,"McPherson Square");
        map.put(38,"Farragut West");
        map.put(40,"Foggy Bottom-GWU");
        map.put(41,"Rosslyn");
        map.put(42,"Arlington Cemetery");
        map.put(43,"Pentagon");
        map.put(44,"Pentagon City");
        map.put(45,"Crystal City");
        map.put(47,"Braddock Road");
        map.put(48,"King St-Old Town");
        map.put(49,"Eisenhower Avenue");
        map.put(50,"Huntington");
        map.put(53,"Federal Triangle");
        map.put(54,"Smithsonian");
        map.put(58,"Federal Center SW");
        map.put(59,"Capitol South");
        map.put(60,"Eastern Market");
        map.put(61,"Potomac Ave");
        map.put(63,"Stadium-Armory");
        map.put(64,"Minnesota Ave");
        map.put(65,"Deanwood");
        map.put(66,"Cheverly");
        map.put(67,"Landover");
        map.put(68,"New Carrollton");
        map.put(70,"Mt Vernon Sq 7th St-Convention Center");
        map.put(72,"Shaw-Howard U");
        map.put(73,"U Street/African-Amer Civil War Memorial/Cardozo");
        map.put(75,"Columbia Heights");
        map.put(76,"Georgia Ave-Petworth");
        map.put(77,"West Hyattsville");
        map.put(78,"Prince George's Plaza");
        map.put(79,"College Park-U of MD");
        map.put(80,"Greenbelt");
        map.put(81,"Archives-Navy Memorial-Penn Quarter");
        map.put(82,"L'Enfant Plaza");
        map.put(83,"Waterfront");
        map.put(84,"Navy Yard-Ballpark");
        map.put(85,"Anacostia");
        map.put(86,"Congress Heights");
        map.put(87,"Naylor Road");
        map.put(88,"Suitland");
        map.put(89,"Branch Ave");
        map.put(90,"Benning Road");
        map.put(91,"Capitol Heights");
        map.put(92,"Addison Road-Seat Pleasant");
        map.put(93,"Ronald Reagan Washington National Airport");
        map.put(94,"Van Dorn Street");
        map.put(95,"Franconia-Springfield");
        map.put(96,"Court House");
        map.put(97,"Clarendon");
        map.put(98,"Virginia Square-GMU");
        map.put(99,"Ballston-MU");
        map.put(100,"East Falls Church");
        map.put(101,"West Falls Church-VT/UVA");
        map.put(102,"Dunn Loring-Merrifield");
        map.put(103,"Vienna/Fairfax-GMU");
        map.put(107,"Southern Avenue");
        map.put(108,"NoMa-Gallaudet U");
        map.put(109,"Largo Town Center");
        map.put(110,"Morgan Boulevard");
        map.put(111,"McLean");
        map.put(112,"Tysons Corner");
        map.put(113,"Greensboro");
        map.put(114,"Spring Hill");
        map.put(115,"Wiehle-Reston East");
        STATION_MAP = Collections.unmodifiableMap(map);
    }

    String name;
    int stationID;

    public static String getStationName(int ID)
    {
        return STATION_MAP.get(ID);
    }

    public static int getStationID(String stationName)
    {
        for(Object o : STATION_MAP.keySet() )
        {
            if(STATION_MAP.get(o).equals(stationName))
                return (int)o;
        }

        return -1;
    }

    //Scrap data to calculate fare
    public float calculateFare(int origin,int destination)
    {
        scrapFare(origin, destination);
        return -0.1f;
    }

    public float scrapFare(int origin, int destination)
    {
        String site = "http://www.wmata.com/rail/station_to_station_fares_inline.cfm?&src_station_id="+origin+"&dst_station_id="+destination;
        new DownloadWebpageTask().execute(site);

        return 1f;
    }





    public float calculateFare(int destination)
    {
        return calculateFare(stationID,destination);
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            InputStream is = null;
            BufferedReader br;
            String line;
            String complete="";
            try {
                URL url = new URL(params[0]);
                is = url.openStream();  // throws an IOException
                br = new BufferedReader(new InputStreamReader(is));

                while ((line = br.readLine()) != null) {
                    complete+=line;
                }
                Pattern pattern = Pattern.compile("<tr class=\"even\">[\\s\\S]*</tr>");
               Matcher matcher = pattern.matcher(complete);
                try {
                    Log.d("data", matcher.find() + "" + matcher.group(0));
                }
                catch (IllegalStateException e )
                {
                    e.printStackTrace();
                    Log.d("matcher", "could not find matching regex");
                }





            }
            catch(MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return null;
        }


        @Override
        public void onPostExecute(String result) {
        // here, you can update, and manipulate, your views

        }
    }



}


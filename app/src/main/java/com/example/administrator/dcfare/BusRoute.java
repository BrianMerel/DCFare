package com.example.administrator.dcfare;

import java.util.ArrayList;

/**
 * Created by Administrator on 4/15/2016.
 */
public class BusRoute {
    public String   name,
                    routeID;
    public Direction    direction0 =new Direction(),
                        direction1 = new Direction();

    public class Direction{

        public String   directionNum,
                        directionText,
                        tripHeadSign;
        public ArrayList<RoutePoint> routePoints = new ArrayList<RoutePoint>();
        public ArrayList<StopPoint>  stopPoints = new ArrayList<StopPoint>();




    }
}

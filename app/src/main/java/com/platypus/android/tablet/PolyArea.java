/**
 * Created by shenty on 2/13/16.
 */
package com.platypus.android.tablet;

import com.mapbox.mapboxsdk.geometry.LatLng;
import java.util.ArrayList;

//issues
//subtract method does subtraction in wrong order
//for bisect vectors it multiples -1 and for findInteriorAngles it
//switches order of parameters. Should fix this but no rush...
public class PolyArea
{

     final double MAXDISTFROMSIDE = .0000898; //distance between wayp
    // final double SUBTRACTDIST = MAXDISTFROMSIDE/2; //subtracted
    //final double MAXDISTFROMSIDE = .5; //distance between wayp
    final double SUBTRACTDIST = MAXDISTFROMSIDE/2; //subtracted

    // dist
    // final double MAXDISTFROMSIDE = .4; //distance between wayp
    // final double SUBTRACTDIST = .2; //subtracted dist

    private LatLng centroid;
    ArrayList<LatLng> vertices;
    ArrayList<LatLng> originalVerts;
    ArrayList<ArrayList<LatLng>> bisect = new ArrayList<ArrayList<LatLng>>();

    public ArrayList<LatLng> quickHull(ArrayList<LatLng> points)
    {
        originalVerts = new ArrayList<LatLng>(points);
        ArrayList<LatLng> convexHull = new ArrayList<LatLng>();
        if (points.size() < 3) {
            return points;
        }

        int minLatLng = -1, maxLatLng = -1;
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        for (int i = 0; i < points.size(); i++)
        {
            // System.out.println("max x " + maxX);
            // System.out.println(points.get(i).getLatitude());
            if (points.get(i).getLatitude() < minX)
            {
                minX = points.get(i).getLatitude();
                minLatLng = i;
            }
            //System.out.println("i lat: " +
            //points.get(i).getLatitude() + " max " + maxX);

            if (points.get(i).getLatitude() > maxX) //is this right?
            {
                maxX = points.get(i).getLatitude();
                maxLatLng = i;
            }
        }

        LatLng A = points.get(minLatLng);
        LatLng B = points.get(maxLatLng);
        convexHull.add(A);
        convexHull.add(B);
        points.remove(A);
        points.remove(B);
        originalVerts.remove(A);
        originalVerts.remove(B);

        ArrayList<LatLng> leftSet = new ArrayList<LatLng>();
        ArrayList<LatLng> rightSet = new ArrayList<LatLng>();

        for (int i = 0; i < points.size(); i++)
        {
            LatLng p = points.get(i);
            if (pointLocation(A, B, p) == -1)
                leftSet.add(p);
            else if (pointLocation(A, B, p) == 1)
                rightSet.add(p);
        }
        hullSet(A, B, rightSet, convexHull);
        hullSet(B, A, leftSet, convexHull);
        vertices = new ArrayList<LatLng>(convexHull);
        return convexHull;
    }

    public double distance(LatLng A, LatLng B, LatLng C)
    {
        double ABx = B.getLongitude() - A.getLongitude();
        double ABy = B.getLatitude() - A.getLatitude();
        double num = ABx * (A.getLatitude() - C.getLatitude()) - ABy * (A.getLongitude() - C.getLongitude());
        if (num < 0)
            num = -num;
        return num;
    }

    public void hullSet(LatLng A, LatLng B, ArrayList<LatLng> set,
                        ArrayList<LatLng> hull)
    {
        int insertPosition = hull.indexOf(B);
        if (set.size() == 0)
            return;
        if (set.size() == 1)
        {
            LatLng p = set.get(0);
            set.remove(p);
            hull.add(insertPosition, p);
            return;
        }
        double dist = Double.MIN_VALUE;
        int furthestLatLng = -1;
        for (int i = 0; i < set.size(); i++)
        {
            LatLng p = set.get(i);
            double distance = distance(A, B, p);
            if (distance > dist)
            {
                dist = distance;
                furthestLatLng = i;
            }
        }
        LatLng P = set.get(furthestLatLng);
        set.remove(furthestLatLng);
        hull.add(insertPosition, P);
        // Determine who's to the left of AP

        ArrayList<LatLng> leftSetAP = new ArrayList<LatLng>();
        for (int i = 0; i < set.size(); i++)
        {
            LatLng M = set.get(i);
            if (pointLocation(A, P, M) == 1)
            {
                leftSetAP.add(M);
            }
        }

        // Determine who's to the left of PB

        ArrayList<LatLng> leftSetPB = new ArrayList<LatLng>();
        for (int i = 0; i < set.size(); i++)
        {
            LatLng M = set.get(i);
            if (pointLocation(P, B, M) == 1)
            {
                leftSetPB.add(M);
            }
        }
        hullSet(A, P, leftSetAP, hull);
        hullSet(P, B, leftSetPB, hull);
    }

    public double pointLocation(LatLng A, LatLng B, LatLng P)
    {
        double cp1 = (B.getLongitude() - A.getLongitude()) * (P.getLatitude() - A.getLatitude()) - (B.getLatitude() - A.getLatitude()) * (P.getLongitude() - A.getLongitude());
        if (cp1 > 0)
            return 1;
        else if (cp1 == 0)
            return 0;
        else
            return -1;
    }



    public LatLng computeCentroid(ArrayList<LatLng> vertices)
    {
        double tempLat = 0;
        double tempLon = 0;

        for (LatLng i : vertices)
        {
            tempLat += i.getLatitude();
            tempLon += i.getLongitude();
        }
        return new LatLng(tempLat/vertices.size(),tempLon/vertices.size());
    }


    public ArrayList<ArrayList<LatLng>> createSmallerPolygons(ArrayList<LatLng> vertices) {

        centroid = computeCentroid(vertices);
        ArrayList<LatLng> pointToCenter = new ArrayList<LatLng>();
        for (LatLng i : vertices)
        {
            pointToCenter.add(new LatLng(i.getLatitude()-centroid.getLatitude(),i.getLongitude()-centroid.getLongitude()));
        }
        ArrayList<ArrayList<LatLng>> spirals = new ArrayList<ArrayList<LatLng>>();
        for (double i = 1; i >= 0; i-=.1)
        {
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            for (LatLng p : pointToCenter)
            {
                points.add(new LatLng(centroid.getLatitude()+p.getLatitude()*i,centroid.getLongitude()+p.getLongitude()*i));
            }
            spirals.add(points);
        }
        return spirals;
    }

    public LatLng getCentroid()
    {
        return centroid;
    }
    public double computeDistance(LatLng firstPoint, LatLng secondPoint)
    {
        double x = Math.pow((secondPoint.getLatitude() - firstPoint.getLatitude()),2);
        double y = Math.pow((secondPoint.getLongitude() - firstPoint.getLongitude()),2);
        return Math.sqrt(x+y);
    }

    public double calculateLength(LatLng vector)
    {
        return Math.sqrt(Math.pow(vector.getLatitude(),2) + Math.pow(vector.getLongitude(),2));
    }
    public LatLng normalizeVector(LatLng vector)
    {
        double distance = calculateLength(vector);
        return new LatLng(vector.getLatitude()/distance, vector.getLongitude()/distance);
    }

    public boolean isNonAdjacentLessThan10Meters(ArrayList<LatLng> verts) {

        //since all points in triangle are adjacent
        if (verts.size() == 3)
        {

            // System.out.println("points 0 and 1" + verts.get(0) + " " + verts.get(1) + " distance: " + comput// eDistance(verts.get(0),verts.get(1)));
            // // System.out.println("points 0 and 2" + verts.get(0) + " " + verts.get(2) + " distance: " + computeDistance(verts.get(0),verts.get(2)));
            // // System.out.println("points 1 and 2" +
            // verts.get(1) + " " + verts.get(2) + " distance: " +
            // computeDistance(verts.get(1),verts.get(2)));

            // System.out.println("distance between points 0 and 1: " + computeDistance(verts.get(0),verts.get(1)));
            // System.out.println("distance between points 0 and 2: " + computeDistance(verts.get(0),verts.get(2)));
            // System.out.println("distance between points 0 and 3: " + computeDistance(verts.get(1),verts.get(2)));


            //              System.out.println();
            if (computeDistance(verts.get(0),verts.get(1)) < MAXDISTFROMSIDE)
            {
                return true;
            }
            else if(computeDistance(verts.get(0),verts.get(2)) < MAXDISTFROMSIDE)
            {
                return true;
            }
            else if(computeDistance(verts.get(1),verts.get(2)) < MAXDISTFROMSIDE)
            {
                return true;
            }
            return false;
        }

        for (int i = 0; i < verts.size(); i++)
        {
            for (int p = i+2; p < verts.size(); p++)
            {
                //case where the first and last point (adjacent are
                //being compared

                //System.out.println("size" + (verts.size()-1));
                if (i == 0 && p == verts.size()-1)
                {
                    continue;
                }
                //  System.out.println(i + " " + p + "\n");
                //                      System.out.println(computeDistance(verts.get(i),verts.get(p)));
                if (computeDistance(verts.get(i),verts.get(p)) < 2*MAXDISTFROMSIDE)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<ArrayList<LatLng>> createSmallerPolygonsFlat(ArrayList<LatLng> vertices) {

        ArrayList<ArrayList<LatLng>> spirals = new ArrayList<ArrayList<LatLng>>();
        spirals.add(vertices);
        int i = 0;

        //ArrayList<LatLng> pointToCenter = new ArrayList<LatLng>();
        //normalized vectors from vertex to center
        // centroid = computeCentroid(vertices);
        // for (LatLng it : vertices)
        //     {
        //         pointToCenter.add(normalizeVector(new LatLng(it.getLatitude()-centroid.getLatitude(),it.getLongitude()-centroid.getLongitude())));
        //     }
        ArrayList<LatLng> centers = new ArrayList<LatLng>();
        while(!isNonAdjacentLessThan10Meters(spirals.get(spirals.size()-1)))
        {

            centroid = computeCentroid(spirals.get(spirals.size()-1));
            centers.add(centroid);
            ArrayList<LatLng> pointToCenter = new ArrayList<LatLng>();
            //normalized vectors from vertex to center
            for (LatLng it : spirals.get(spirals.size()-1))
            {
                pointToCenter.add(normalizeVector(new LatLng(it.getLatitude()-centroid.getLatitude(),it.getLongitude()-centroid.getLongitude())));
            }

            //the last layer added
            ArrayList<LatLng> previousPolygon = spirals.get(spirals.size()-1);
            //upcoming layer
            ArrayList<LatLng> nextPolygon = new ArrayList<LatLng>();

            for (LatLng p : previousPolygon)
            {
                LatLng temp = new LatLng(centroid.getLatitude() - p.getLatitude(),centroid.getLongitude()-p.getLongitude());
                //System.out.println("dist " + calculateLength(temp));
                if (calculateLength(temp) < MAXDISTFROMSIDE)
                {
//                    System.out.println("centroid");
//                    System.out.print("centx = [");
//                    for (LatLng t : centers)
//                    {
//                        System.out.print(t.getLatitude()+",");
//                    }

//                    System.out.print("]\n\ncenty=[");
//                    for (LatLng t : centers)
//                    {
////                        System.out.print(t.getLongitude()+",");
//                    }
//                    System.out.print("]\n\n");

                    return spirals;
                }
            }

            //for (LatLng p : pointToCenter)
            for (int t = 0; t < pointToCenter.size(); t++)
            {
                LatLng p = pointToCenter.get(t);
                LatLng temp = new LatLng(centroid.getLatitude() - p.getLatitude(),centroid.getLongitude()-p.getLongitude());
                if (calculateLength(temp) < MAXDISTFROMSIDE)
                {
                    continue;
                }

                nextPolygon.add(new LatLng(previousPolygon.get(t).getLatitude()-pointToCenter.get(t).getLatitude()*SUBTRACTDIST,previousPolygon.get(t).getLongitude()-pointToCenter.get(t).getLongitude()*SUBTRACTDIST));
            }
            //System.out.println(nextPolygon.size());
            spirals.add(nextPolygon);
            // System.out.println("New Polygon");
            // for (LatLng a : nextPolygon)
            //  {
            //      System.out.println(a);
            //  }
            // System.out.println("");
        }

        return spirals;
    }

    public static void main1(String args[])
    {
        System.out.println("\nQuick Hull Test \n");

        ArrayList<LatLng> points = new ArrayList<LatLng>();


        //these works
        // LatLng point0 = new LatLng(-2,-2);
        // LatLng point1 = new LatLng(-2,2);
        // LatLng point2 = new LatLng(2,2);
        // LatLng point3 = new LatLng(2,-2);


        //these dont
        LatLng point0 = new LatLng(-15 ,145);
        LatLng point1 = new LatLng(3,94);
        LatLng point2 = new LatLng(35,113);
        LatLng point3 = new LatLng(15,200);
        LatLng point4 = new LatLng(-10,200);

        points.add(point0);
        points.add(point1);
        points.add(point2);
        //points.add(point3);
        //points.add(point4);

        PolyArea qh = new PolyArea();
        ArrayList<LatLng> p = qh.quickHull(points);

        System.out.println(qh.isNonAdjacentLessThan10Meters(qh.vertices));

        // PolyArea qh = new PolyArea();
        // ArrayList<LatLng> p = qh.quickHull(points);
        //ArrayList<ArrayList<LatLng>> spirals = qh.createSmallerPolygonsFlat(qh.vertices);

        // System.out.print("x = [");
        // for (ArrayList<LatLng> i : spirals)
        //     {
        //         for (LatLng t : i)
        //             {
        //                 System.out.print(t.getLatitude()+",");
        //             }
        //     }
        // System.out.print("]\n\ny=[");
        // for (ArrayList<LatLng> i : spirals)
        //     {
        //         for (LatLng t : i)
        //             {
        //                 System.out.print(t.getLongitude()+",");
        //             }
        //     }
        // System.out.print("]\n\n");
        //        System.out.println(qh.centroid);
    }

    public double findInteriorAngle(LatLng a, LatLng b)
    {
        //System.out.println(180/Math.PI*Math.acos(dot(a,b)/(calculateLength(a)*calculateLength(b))));
        // System.out.println("a " + calculateLength(a));
        // System.out.println("b " + calculateLength(b));
        // System.out.println("a.b: " + dot(a,b));
        // System.out.println("dot(a,b)/lengtha*lengthb : " + Math.acos(dot(a,b)/(calculateLength(a)*calculateLength(b
        //                                                                                                                ))));
        double dot = dot(a,b);
        double cross = a.getLatitude()*b.getLongitude()-a.getLongitude()*b.getLatitude();
        double output = Math.atan2(dot,cross);
        if (output < 0)
        {
            output += Math.PI;
        }
        if (output == 0)
        {
            output = Math.PI/2;
        }
        // if (output > Math.PI/2)
        // {
        // 	output = Math.PI - output;
        // }
        //return output;
        return Math.acos(dot(a,b)/(calculateLength(a)*calculateLength(b)));
    }
    public LatLng findBisectNormal(LatLng a, LatLng b)
    {
        a = normalizeVector(a);
        b = normalizeVector(b);
        LatLng added = add(a,b);
        return normalizeVector(added);
    }
    public double dot(LatLng a, LatLng b)
    {
        return a.getLatitude()*b.getLatitude() + a.getLongitude()*b.getLongitude();
    }
    public LatLng add(LatLng a, LatLng b)
    {
        return new LatLng(a.getLatitude() + b.getLatitude(),a.getLongitude()+b.getLongitude());
    }
    public LatLng subtract(LatLng a, LatLng b)
    {
        return new LatLng(a.getLatitude() - b.getLatitude(),a.getLongitude() - b.getLongitude());
        //return new LatLng(b.getLatitude() - a.getLatitude(),b.getLongitude() - a.getLongitude());
    }

    public LatLng findVector(LatLng a, LatLng b)
    {
        return subtract(a,b);
    }
    public LatLng multiply(LatLng a, double amount)
    {
        return new LatLng(a.getLatitude()*amount, a.getLongitude()*amount);
    }

    /*This works by finding the bisecting vector of each angle and
     * moving along the secting vector of distance dist/sin(angle/2) */
    public ArrayList<ArrayList<LatLng>> computeSpiralsPolygonOffset(ArrayList<LatLng> polygon)
    {
        ArrayList<ArrayList<LatLng>> spirals = new ArrayList<ArrayList<LatLng>>();
        spirals.add(polygon); //add first polygon
        if (polygon.size() <= 2)
        {
            System.out.println("poly size < 2 is: " + polygon.size());
            return spirals;
        }


        //compute all of the bisecting vectors note these look wrong
        int counter = 0;
        //while(counter < 10)
        while(!isNonAdjacentLessThan10Meters(spirals.get(spirals.size()-1)))
        {
            counter++;
            //System.out.println(spirals.get(spirals.size()-1).size());
            //Last Polygon to be added
            ArrayList<LatLng> lastSpiral = spirals.get(spirals.size()-1);
            //Comput the centroid of the last spiral
            LatLng centroid = computeCentroid(lastSpiral);
            //Check to see if any points are less than
            //subtractdist from the centroid, if so return(this is
            //a stopping point)
            //oSystem.out.println(lastSpiral);
            for (LatLng p : lastSpiral)
            {
                LatLng temp = new LatLng(centroid.getLatitude() - p.getLatitude(),centroid.getLongitude()-p.getLongitude());
                //System.out.println("dist " + calculateLength(temp));
                //System.out.println(temp);
                if (calculateLength(temp) < SUBTRACTDIST)
                {
                    // System.out.println(lastSpiral);
                    // System.out.println(computeCentroid(spirals.get(spirals.size()-2)));
                    System.out.println("poly area center close");
                    return spirals;
                }
            }

            //compute all of the vectors that make the edges
            ArrayList<LatLng> edgeVectors = new ArrayList<LatLng>();
            for (int i = 0; i < lastSpiral.size()-1; i++)
            {
                edgeVectors.add(findVector(lastSpiral.get(i),lastSpiral.get(i+1)));
            }
            edgeVectors.add(findVector(lastSpiral.get(lastSpiral.size()-1),lastSpiral.get(0)));
            //System.out.println(edgeVectors);
            //            System.out.println(edgeVectors);
            //Compute all of the angles between these edges
            //System.out.println("points");
            //System.out.println(lastSpiral);
            ArrayList<Double> interiorAngles = new ArrayList<Double>();
            for (int i = 0; i < lastSpiral.size()-1; i++)
            {
                LatLng v;
                LatLng u;
                if (i == 0)
                {
                    v = subtract(lastSpiral.get(i),lastSpiral.get(lastSpiral.size()-1));
                    u = subtract(lastSpiral.get(i),lastSpiral.get(i+1));
                    interiorAngles.add(findInteriorAngle(v,u));
                    // System.out.println("u " + u);
                    // System.out.println("v " + v);
                    // System.out.println("0 Angle " + findInteriorAngle(v,u)*180/Math.PI);
                    continue;
                }
                v = subtract(lastSpiral.get(i),lastSpiral.get(i+1));
                u = subtract(lastSpiral.get(i),lastSpiral.get(i-1));
                interiorAngles.add(findInteriorAngle(v,u));
                // System.out.println("u " + u);
                // System.out.println("v " + v);
                // System.out.println(i + " Angle " + findInteriorAngle(v,u)*180/Math.PI);

            }
            LatLng v1 = subtract(lastSpiral.get(lastSpiral.size()-1),lastSpiral.get(0));
            LatLng u1 = subtract(lastSpiral.get(lastSpiral.size()-1),lastSpiral.get(lastSpiral.size()-2));
            interiorAngles.add(findInteriorAngle(v1,u1));
            // System.out.println("u " + u1);
            // System.out.println("v " + v1);
            // System.out.println("Angle last" + (findInteriorAngle(v1,u1)*180/Math.PI));


            // for (int i = 0; i < edgeVectors.size()-1; i++)
            //  {
            //      interiorAngles.add(findInteriorAngle(edgeVectors.get(i),edgeVectors.get(i+1)));
            //  }
            // interiorAngles.add(findInteriorAngle(edgeVectors.get(0),edgeVectors.get(edgeVectors.size()-1)));

            // System.out.println("points");
            // System.out.println(lastSpiral);
            // System.out.println("edges");
            // System.out.println(edgeVectors);
            // System.out.println("Angles");
            // for (Double i : interiorAngles)
            // {
            //     System.out.println(i*180/Math.PI);
            // }
            // System.out.println("");

            ArrayList<LatLng> bisectingVectors = new ArrayList<LatLng>();
            for (int i = 0; i < lastSpiral.size()-1; i++)
            {
                LatLng v;
                LatLng u;
                if (i == 0)
                {
                    v = subtract(lastSpiral.get(lastSpiral.size()-1),lastSpiral.get(i));
                    u = subtract(lastSpiral.get(i+1),lastSpiral.get(i));
                    bisectingVectors.add(findBisectNormal(v,u));
                    continue;
                }
                v = subtract(lastSpiral.get(i+1),lastSpiral.get(i));
                u = subtract(lastSpiral.get(i-1),lastSpiral.get(i));
                bisectingVectors.add(findBisectNormal(v,u));
            }
            LatLng v = subtract(lastSpiral.get(0),lastSpiral.get(lastSpiral.size()-1));
            LatLng u = subtract(lastSpiral.get(lastSpiral.size()-2),lastSpiral.get(lastSpiral.size()-1));
            bisectingVectors.add(findBisectNormal(v,u));

            //p = p- dist
            //finds vector between the point and the bisecting
            //vector with length dist/math.sin(angle/2)
            ArrayList<LatLng> nextPolygon = new ArrayList<LatLng>();
            for (int i = 0; i < lastSpiral.size(); i++)
            {
                //LatLng point =
                //spirals.get(spirals.size()-1).get(i);
                LatLng point = lastSpiral.get(i);
                //add the vector between the original point
                //and the point subtracted by the bisecting
                //vector with length
                //subtractdist/math.sin(interiorangle/2)
                //System.out.println("dist" +
                //SUBTRACTDIST/Math.sin(interiorAngles.get(i)/2));
                //System.out.println(interiorAngles.get(i)*180/Math.PI);
                LatLng nextPoint = multiply(bisectingVectors.get(i),-1*SUBTRACTDIST/Math.sin(interiorAngles.get(i)/2));

                //System.out.println(point + " " + nextPoint + " " + findVector(point,nextPoint));
                nextPolygon.add(findVector(point,nextPoint));
            }

            //check intersections
            //if so remove one points and set the other as the avergae
            //of both
            for (int i = 0; i < nextPolygon.size()-1; i++)
            {
                LatLng lastPoint0 = lastSpiral.get(i);
                LatLng nextPoint0 = nextPolygon.get(i);
                LatLng lastPoint1 = lastSpiral.get(i+1);
                LatLng nextPoint1 = nextPolygon.get(i+1);
                if( linesIntersect(lastPoint0.getLatitude(),lastPoint0.getLongitude(),nextPoint0.getLatitude(),nextPoint0.getLongitude(),lastPoint1.getLatitude(),lastPoint1.getLongitude(),nextPoint1.getLatitude(),nextPoint1.getLongitude()))
                {
                    System.out.println("Removed vertex : " + i + " from polygon: " + spirals.size());
                    LatLng average = new LatLng((nextPoint0.getLatitude() + nextPoint1.getLatitude())/2,(nextPoint0.getLongitude() + nextPoint1.getLongitude())/2);
                    nextPolygon.set(i+1,average);
                    nextPolygon.remove(i);

                }
            }
            //System.out.println(nextPolygon.size());
            //spirals.add(quickHull(nextPolygon));
            spirals.add((nextPolygon));
        }
        return  spirals;
    }

    // check for intersection between bisects
    // if the two will intersect in the next spira
    // meaning the points will "swap place"
    // merge points at average? or something
    // if not the shape changes and the angles change drastically
    // causing overlap
    //check if intersect is between area defined by old points and new points
    public static void main(String[] args)
    {
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        // LatLng point0 = new LatLng(-15 ,145);
        // LatLng point1 = new LatLng(3,94);
        // LatLng point2 = new LatLng(35,113);
        // LatLng point3 = new LatLng(15,200);
        //        LatLng point4 = new LatLng(-10,200);


        // LatLng point0 = new LatLng(-1,-2);
        // LatLng point1 = new LatLng(-1,1);
        // LatLng point2 = new LatLng(1,1);
        // LatLng point3 = new LatLng(1,-1);
        // LatLng point4 = new LatLng(2,4);

        // LatLng point0 = new LatLng(-1,0);
        // LatLng point1 = new LatLng(0,0);
        // LatLng point2 = new LatLng(0,1);
        //  LatLng point3 = new LatLng(1,-1);

        LatLng point0 = new LatLng(-2,-3); //2
        LatLng point1 = new LatLng(-3,2); //1
        LatLng point2 = new LatLng(2,4);
        LatLng point3 = new LatLng(1,-2); //3
        //LatLng point4 = new LatLng(2,4);

        // LatLng point0 = new LatLng(-2,-2); //2
        // LatLng point1 = new LatLng(-2,2); //1
        // LatLng point2 = new LatLng(2,2);
        // LatLng point3 = new LatLng(2,-2); //3
        // LatLng point4 = new LatLng(2,2);



        points.add(point0);
        points.add(point1);
        points.add(point2);
        points.add(point3);
        //points.add(point4);

        PolyArea qh = new PolyArea();
        // qh.test();


        // qh.test();

        ArrayList<LatLng> p = qh.quickHull(points);
        //1 0 3

        // LatLng u = qh.findVector(point1,point0);
        // LatLng v = qh.findVector(point0, point2);
        // System.out.println("Point 1 + " + point0);
        // System.out.println("Point 2 + " + point1);
        // System.out.println("Point 3 + " + point2);
        // System.out.println("U: " + u);
        // System.out.println("V:" + v);
        // System.out.println("U Length: " + qh.calculateLength(u));
        // System.out.println("V Length: " + qh.calculateLength(v));
        // System.out.println("U dot V: " + qh.dot(u,v));
        // System.out.println("U normalized: " + qh.normalizeVector(u));
        // System.out.println("U normalized: " + qh.normalizeVector(u));
        // double angle = qh.findInteriorAngle(u,v);
        // //System.out.println(Math.acos(qh.dot(u,v)/(qh.calculateLength(u)*qh.calculateLength(v)))*180/3.14);
        // System.out.println("Interior Angle: " + qh.findInteriorAngle(u,v)* 180/3.14 + " degrees");
        // LatLng bisect = qh.findBisectNormal(u,v);
        // System.out.println("Bisecting Vector: " + bisect);
        // // LatLng newpoint = qh.findVector(point0,qh.multiply(bisect,Math.sin(angle/2)));
        // // System.out.println("New Point: " + newpoint);
        // System.exit(0);

        ArrayList<ArrayList<LatLng>> spirals =  qh.computeSpiralsPolygonOffset(qh.vertices);
        ArrayList<LatLng> allpoints = new ArrayList<LatLng>();

        int counter = 0;
        for (ArrayList<LatLng> i : spirals)
        {
            System.out.print("x" + counter + " = [");
            for (LatLng t : i)
            {
                System.out.print(t.getLatitude()+",");
            }
            System.out.print(i.get(0).getLatitude()+",");
            System.out.println("]\n");
            counter++;
        }
        int counter2 = 0;
        for (ArrayList<LatLng> i : spirals)
        {
            System.out.print("y" + counter2 + " = [");
            for (LatLng t : i)
            {
                System.out.print(t.getLongitude()+",");
            }
            System.out.print(i.get(0).getLongitude()+",");
            System.out.println("]\n");
            counter2++;
        }

        for (int i = 0; i < counter; i++)
        {
            System.out.print("plot(x"+i+",y"+i+");" + "hold on; ");
        }
        System.out.println("\n");
        //for each polygon
        counter = 0;
        for (int i = 0; i < qh.bisect.size(); i++)
        {
            //for each vertex
            for (int t = 0; t < qh.bisect.get(i).size(); t++)
            {
                //qh.bisect.get(i).set(t,qh.normalizeVector(qh.bisect.get(i).get(t)));

                System.out.println("bx"+counter+"=["+spirals.get(i).get(t).getLatitude()+","+qh.subtract(spirals.get(i).get(t),qh.bisect.get(i).get(t)).getLatitude()+"]");
                counter++;
            }
        }

        counter2 = 0;
        for (int i = 0; i < qh.bisect.size(); i++)
        {
            //for each vertex
            for (int t = 0; t < qh.bisect.get(i).size(); t++)
            {
                System.out.println("by"+counter2+"=["+spirals.get(i).get(t).getLongitude()+","+qh.subtract(spirals.get(i).get(t),qh.bisect.get(i).get(t)).getLongitude()+"]");
                counter2++;
            }
        }
        for (int i = 0; i < counter; i++)
        {
            System.out.print("plot(bx"+i+",by"+i+",\"r\");" + "hold on; ");
        }
    }

    //http://www.java2s.com/Code/Android/Game/TestsifthelinesegmentfromX1nbspY1toX2nbspY2intersectsthelinesegmentfromX3nbspY3toX4nbspY4.htm
    public static boolean linesIntersect(final double X1, final double Y1, final double X2, final double Y2, final double X3, final double Y3, final double X4, final double Y4) {
        return ((relativeCCW(X1, Y1, X2, Y2, X3, Y3)
                * relativeCCW(X1, Y1, X2, Y2, X4, Y4) <= 0) && (relativeCCW(X3,
                Y3, X4, Y4, X1, Y1)
                * relativeCCW(X3, Y3, X4, Y4, X2, Y2) <= 0));
    }

    private static double relativeCCW(final double X1, final double Y1, double X2, double Y2, double PX,
                                      double PY) {
        X2 -= X1;
        Y2 -= Y1;
        PX -= X1;
        PY -= Y1;
        double ccw = PX * Y2 - PY * X2;
        if (ccw == 0) {
            // The point is colinear, classify based on which side of
            // the segment the point falls on. We can calculate a
            // relative value using the projection of PX,PY onto the
            // segment - a negative value indicates the point projects
            // outside of the segment in the direction of the particular
            // endpoint used as the origin for the projection.
            ccw = PX * X2 + PY * Y2;
            if (ccw > 0) {
                // Reverse the projection to be relative to the original X2,Y2
                // X2 and Y2 are simply negated.
                // PX and PY need to have (X2 - X1) or (Y2 - Y1) subtracted
                // from them (based on the original values)
                // Since we really want to get a positive answer when the
                // point is "beyond (X2,Y2)", then we want to calculate
                // the inverse anyway - thus we leave X2 & Y2 negated.
                PX -= X2;
                PY -= Y2;
                ccw = PX * X2 + PY * Y2;
                if (ccw < 0) {
                    ccw = 0;
                }
            }
        }
        return (ccw < 0) ? -1 : ((ccw > 0) ? 1 : 0);
    }
    public void doAdjacentLinesIntersect(ArrayList<LatLng> last, ArrayList<LatLng> next)
    {
        for (int i = 0; i < last.size()-1; i++)
        {
            LatLng lastPoint0 = last.get(i);
            LatLng nextPoint0 = next.get(i);
            LatLng lastPoint1 = last.get(i+1);
            LatLng nextPoint1 = next.get(i+1);
            if( linesIntersect(lastPoint0.getLatitude(),lastPoint0.getLongitude(),nextPoint0.getLatitude(),nextPoint0.getLongitude(),lastPoint1.getLatitude(),lastPoint1.getLongitude(),nextPoint1.getLatitude(),nextPoint1.getLongitude()))
            {
                System.out.println("intersecting bisect at: " + i + " " + (i+1));
            }
        }
    }

}
/*
 */

package com.platypus.android.tablet.Path;

import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.UTM;
import org.jscience.geography.coordinates.crs.ReferenceEllipsoid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

/**
 * Created by jason on 11/20/17.
 */

public class Region
{
		private ArrayList<LatLng> original_points = new ArrayList<>();
		private ArrayList<Double[]> convex_xy = new ArrayList<>();
		private ArrayList<Double[]> path_xy = new ArrayList<>();
		private ArrayList<LatLng> path_points = new ArrayList<>();
		private Double[] utm_centroid = new Double[2];
		private Double[] local_centroid = new Double[2];
		private UTM original_utm;
		private AreaType area_type;
		private double transect_distance = 10;
		private String logTag = "Region";
		private double last_min_dist_to_centroid = 9999999; // start as a huge number
		private double last_max_dist_to_centroid = 9999999; // start as a huge number

		public Region (ArrayList<LatLng> _original_points, AreaType _area_type, double _transect_distance) throws Exception
		{
				original_points = (ArrayList<LatLng>)_original_points.clone();
				convexHull();
				area_type = _area_type;
				transect_distance = _transect_distance;
				switch (area_type)
				{
						case SPIRAL:
								inwardNextHull(convex_xy);
								break;

						case LAWNMOWER:
								// TODO: fit rectangle over hull (rotated to have long axis sitting on hull diameter)
								// TODO: calculate equations for lines back and forth across this rectangle
								// TODO: calculate intersections between hull and the lawnmower lines
								// TODO: properly order the intersections to generate the lawnmower path
								break;

						default:
								Log.e(logTag, "Unknown region type");
								throw new Exception("Unknown region type");
				}
		}

		public Path convertToSimplePath()
		{
				// create a simple Path from path_points
				path_points.clear();
				for (Double[] p : path_xy)
				{
						// add back in the UTM offset
						p[0] += utm_centroid[0];
						p[1] += utm_centroid[1];
						LatLong latLong = UTM.utmToLatLong(UTM.valueOf(original_utm.longitudeZone(), original_utm.latitudeZone(), p[0], p[1], SI.METER), ReferenceEllipsoid.WGS84);
						path_points.add(new LatLng(latLong.latitudeValue(NonSI.DEGREE_ANGLE), latLong.longitudeValue(NonSI.DEGREE_ANGLE)));
				}
				return new Path(path_points);
		}

		private static double wrapToPi(double angle)
		{
				while (Math.abs(angle) > Math.PI)
				{
						angle -= 2*Math.PI*Math.signum(angle);
				}
				return angle;
		}

		private static <T> ArrayList<T> shiftArrayList(ArrayList<T> aL, int shift)
		{
				while (Math.abs(shift) > aL.size())
				{
						shift -= aL.size()*Math.signum(shift);
				}
				if (shift < 0)
				{
						shift = aL.size() + shift; // shifting backward is like shifting forward by more
				}
				Log.v("Region", String.format("Shifting forward by %d", shift));
				// https://stackoverflow.com/questions/29548488/shifting-in-arraylist
				ArrayList<T> aL_clone = (ArrayList<T>)aL.clone();
				if (aL.size() == 0)
						return aL_clone;

				T element = null;
				for(int i = 0; i < shift; i++)
				{
						// remove last element, add it to front of the ArrayList
						element = aL_clone.remove( aL_clone.size() - 1 );
						aL_clone.add(0, element);
				}

				return aL_clone;
		}

		private Double[] difference(Double[] a, Double[] b)
		{
				return new Double[]{b[0] - a[0], b[1] - a[1]};
		}

		private double distance(Double[] a, Double[] b)
		{
				Double[] diff = difference(a, b);
				return Math.sqrt(Math.pow(diff[0], 2.) + Math.pow(diff[1], 2.));
		}
		private double dot(Double[] a, Double[] b)
		{
				return a[0]*b[0] + a[1]*b[1];
		}

		private double distance_to_centroid(Double[] a)
		{
				return distance(a, local_centroid);
				// return Math.sqrt(a[0]*a[0] + a[1]*a[1]); // assuming centroid is 0, 0
		}

		private double min_distance_to_centroid(ArrayList<Double[]> points)
		{
				double min_distance = -1;
				for (int i = 0; i < points.size(); i++)
				{
						double d = distance_to_centroid(points.get(i));
						if (min_distance < 0 || d < min_distance) min_distance = d;
				}
				return min_distance;
		}

		private double max_distance_to_centroid(ArrayList<Double[]> points)
		{
				double max_distance = -1;
				for (int i = 0; i < points.size(); i++)
				{
						double d = distance_to_centroid(points.get(i));
						if (max_distance < 0 || d > max_distance) max_distance = d;
				}
				return max_distance;
		}

		private double[][] pairwiseDistances(ArrayList<Double[]> points)
		{
				double[][] result = new double[points.size()][points.size()];
				for (int i = 0; i < points.size(); i++)
				{
						for (int j = 0; j < points.size(); j++)
						{
								result[i][j] = distance(points.get(i), points.get(j));
						}
				}
				return result;
		}

		private int[] diameterPair(ArrayList<Double[]> points)
		{
				double[][] pairwise_distances = pairwiseDistances(points);
				double max_dist = -1.;
				int maxi = 0;
				int maxj = 0;
				for (int i = 0; i < points.size(); i++)
				{
						for (int j = 0; j < points.size(); j++)
						{
								if (pairwise_distances[i][j] > max_dist)
								{
										maxi = i;
										maxj = j;
										max_dist = pairwise_distances[i][j];
								}
						}
				}
				return new int[]{maxi, maxj};
		}

		private ArrayList<Double[]> mergeClosePoints(ArrayList<Double[]> points)
		{
				// merge one pair at a time, recurse until no pairs can be merged OR only 3 points remain OR all points would merge
				if (points.size() < 4) return points;
				ArrayList<Double[]> result = new ArrayList<>();
				double[][] pairwise_distances = pairwiseDistances(points);
				HashMap<Integer, Integer> merging_map = new HashMap<>();
				for (int i = 0; i < points.size(); i++)
				{
						for (int j = 0; j < points.size(); j++)
						{
								if ((i != j) && (pairwise_distances[i][j] < transect_distance))
								{
										if (!merging_map.containsKey(j)) merging_map.put(i, j);
										break;
								}
						}
				}
				if (merging_map.size() == 0) return points;
				Log.w(logTag, String.format("There are %d points to merge", merging_map.size()));

				// extract only the first merge pair
				int a = -1; // indices
				int b = -1;
				for (Map.Entry<Integer, Integer> entry : merging_map.entrySet())
				{
						a = entry.getKey();
						b = entry.getValue();
						Log.w(logTag, String.format("Merging index %d and %d", a, b));
						break;
				}

				for (int i = 0; i < points.size(); i++)
				{
						if (i == a)
						{
								// merge these two
								Double[] pa = points.get(a);
								Double[] pb = points.get(b);
								result.add(new Double[]{0.5*pa[0]+0.5*pb[0], 0.5*pa[1]+0.5*pb[1]});
						}
						else if (i != b)
						{
								result.add(points.get(i));
						}
				}
				return mergeClosePoints(result);
		}

		private Double[] calculateCentroid(ArrayList<Double[]> points)
		{
				Double[] result = new Double[]{0.0, 0.0};
				for (Double[] p : points)
				{
						result[0] += p[0]/points.size();
						result[1] += p[1]/points.size();
				}
				return result;
		}

		private ArrayList<Double[]> lineSegmentDifferences(ArrayList<Double[]> points)
		{
				ArrayList<Double[]> rolled_points = shiftArrayList(points, 1);
				ArrayList<Double[]> result = new ArrayList<>();
				for (int i = 0; i < points.size(); i++)
				{
						Double[] a = points.get(i);
						Double[] b = rolled_points.get(i);
						result.add(new Double[]{b[0]-a[0], b[1]-a[1]});
				}
				return result;
		}
		private ArrayList<Double> lineSegmentNormalAngles(ArrayList<Double[]> points)
		{
				ArrayList<Double[]> rolled_points = shiftArrayList(points, 1);

				/*
				StringBuilder sb0, sb1;
				sb0 = new StringBuilder();
				sb1 = new StringBuilder();
				for (int i = 0; i < points.size(); i++)
				{
						String a = String.format("[%.0f, %.0f]\n", points.get(i)[0], points.get(i)[1]);
						String b = String.format("[%.0f, %.0f]\n", rolled_points.get(i)[0], rolled_points.get(i)[1]);
						sb0.append(a);
						sb1.append(b);
				}
				Log.v(logTag, "points = \n" + sb0.toString());
				Log.v(logTag, "rolled_points = \n" + sb1.toString());
				*/

				ArrayList<Double> result = new ArrayList<>();
				for (int i = 0; i < points.size(); i++)
				{
						Double[] a = rolled_points.get(i);
						Double[] b = points.get(i);
						//Log.v(logTag, String.format("a = [%.0f, %.0f], b = [%.0f, %.0f]", a[0], a[1], b[0], b[1]));
						double raw_angle = Math.atan2(b[1]-a[1], b[0]-a[0]) + Math.PI/2.0;
						result.add(wrapToPi(raw_angle));
				}
				return result;
		}

		private Double[] intersection(Double[] line1_point1, Double[] line1_point2, Double[] line2_point1, Double[] line2_point2)
		{
				Double[] d1 = difference(line1_point1, line1_point2);
				Double[] d2 = difference(line2_point1, line2_point2);
				Double[] dp = difference(line2_point1, line1_point1);
				Double[] d1p = new Double[]{-d1[1], d1[0]};
				double denominator = dot(d1p, d2);
				double numerator = dot(d1p, dp);
				double c = numerator/denominator;
				return new Double[]{c*d2[0] + line2_point1[0], c*d2[1] + line2_point1[1]};
		}

		private void inwardNextHull(ArrayList<Double[]> previous_hull)
		{
				// recursive. Given the previous set of points, calculate the next set of points. Append them to path_xy until the centroid is reached.
				// the first points (i.e. path_xy is empty) are the first convex hull
				// the final point is the centroid
				Log.d(logTag, String.format("inwardNextHull called. path_xy has %d points", path_xy.size()));
				if (path_xy.size() < 1)
				{
						// first call: just add convex hull to path and recurse
						path_xy.addAll(previous_hull);
						path_xy.add(path_xy.get(0).clone());
						inwardNextHull(previous_hull);
						last_min_dist_to_centroid = 9999999;
						last_max_dist_to_centroid = 9999999;
						return; // recursion stack unravels here, so return, now with path_xy fully populated
				}

				// find the normal angles of the line segments (make sure it points inward)
				ArrayList<Double> normal_angles = lineSegmentNormalAngles(previous_hull);
				ArrayList<Double> rolled_angles = shiftArrayList(normal_angles, -1);

				// find the points if they were moved inward along their normal by the transect distance
				ArrayList<Double[]> new_segments_point_1 = new ArrayList<>();
				ArrayList<Double[]> new_segments_point_2 = new ArrayList<>();
				for (int i = 0; i < previous_hull.size(); i++)
				{
						Double[] p = previous_hull.get(i);
						double angle = normal_angles.get(i);
						double rolled_angle = rolled_angles.get(i);
						Log.v(logTag, String.format("Normal angle: %f", angle));
						Log.v(logTag, String.format("Rolled normal angle: %f", rolled_angle));
						Double[] new_1 = new Double[]{
										p[0] + transect_distance*Math.cos(angle),
										p[1] + transect_distance*Math.sin(angle)};
						Double[] new_2 = new Double[]{
										p[0] + transect_distance*Math.cos(rolled_angle),
										p[1] + transect_distance*Math.sin(rolled_angle)};
						new_segments_point_1.add(new_1);
						new_segments_point_2.add(new_2);
				}

				// shift new_segments_point_2 so that the new lines have their indices aligned properly
				new_segments_point_2 = shiftArrayList(new_segments_point_2, 1);

				// find intersections between the new lines
				ArrayList<Double[]> new_segments_point_1_rolled = shiftArrayList(new_segments_point_1, 1);
				ArrayList<Double[]> new_segments_point_2_rolled = shiftArrayList(new_segments_point_2, 1);
				ArrayList<Double[]> intersections = new ArrayList<>();
				for (int i = 0; i < new_segments_point_1.size(); i++)
				{
						intersections.add(intersection(
										new_segments_point_1.get(i),
										new_segments_point_2.get(i),
										new_segments_point_1_rolled.get(i),
										new_segments_point_2_rolled.get(i)));
				}

				// to compare old to new for overshoot, need to shift back by one (2nd must become 1st)
				intersections = shiftArrayList(intersections, -1);

				// check for overshoot, ending early if you find it
				local_centroid = calculateCentroid(intersections);
				for (int i = 0; i < previous_hull.size(); i++)
				{
						Double[] p_old = previous_hull.get(i);
						Double[] p_new = intersections.get(i);
						Double[] diff_old = difference(p_old, local_centroid);
						Double[] diff_new = difference(p_new, local_centroid);
						double old_coord = 0;
						double new_coord = 0;
						String which_coordinate;

						// only use the larger of the two coordinates to avoid false positives
						if (Math.abs(diff_old[0]) > Math.abs(diff_old[1]))
						{
								old_coord = diff_old[0];
								new_coord = diff_new[0];
								which_coordinate = "x";
						}
						else
						{
								old_coord = diff_old[1];
								new_coord = diff_new[1];
								which_coordinate = "y";
						}
						if (old_coord*new_coord <= 0)
						{
								Log.i(logTag, String.format("inwardNextHull detected overshoot: index %d, %s", i, which_coordinate));
								path_xy.add(local_centroid);
								last_min_dist_to_centroid = 9999999;
								last_max_dist_to_centroid = 9999999;
								return;
						}
				}

				// to create a *closed-hull* spiral we need to shift back by one again (3rd must become 1st)
				intersections = shiftArrayList(intersections, -1);

				// merge any points that are too close together to track
				intersections = mergeClosePoints(intersections);

				local_centroid = calculateCentroid(intersections);
				/*
				double min_d_to_c = min_distance_to_centroid(intersections);
				double max_d_to_c = max_distance_to_centroid(intersections);
				if (min_d_to_c <= transect_distance)
				{
						Log.i(logTag, "inwardNextHull detected a point < transect_distance from the centroid.");
						path_xy.add(local_centroid);
						last_min_dist_to_centroid = 9999999;
						return;
				}
				Log.d(logTag, String.format("old min dist = %.0f, new min dist = %.0f", last_min_dist_to_centroid, min_d_to_c));
				Log.d(logTag, String.format("old max dist = %.0f, new max dist = %.0f", last_max_dist_to_centroid, max_d_to_c));
				if (min_d_to_c < (last_min_dist_to_centroid + transect_distance))
				{
						// include that extra buffer of + transect_distance. Only trigger when the algorithm is exploding
						last_min_dist_to_centroid = min_d_to_c;
				}
				else
				{
						Log.i(logTag, "inwardNextHull detected an increase in min distance to centroid");
						// reduction in minimum distance should be monotonic
						path_xy.add(local_centroid);
						last_min_dist_to_centroid = 9999999;
						last_max_dist_to_centroid = 9999999;
						return;
				}
				if (max_d_to_c < (last_max_dist_to_centroid + transect_distance))
				{
						last_max_dist_to_centroid = max_d_to_c;
				}
				else
				{
						Log.i(logTag, "inwardNextHull detected an increase in max distance to centroid");
						// reduction in minimum distance should be monotonic
						path_xy.add(local_centroid);
						last_min_dist_to_centroid = 9999999;
						last_max_dist_to_centroid = 9999999;
						return;
				}
				*/

				// TODO: check that the spiral always turns the same way, point by point.
				// TODO:     if it ever deviates, truncate with the centroid and end
				Double[] p0 = path_xy.get(path_xy.size()-2);
				Double[] p1 = path_xy.get(path_xy.size()-1);
				double old_angle = Math.atan2(p1[1]-p0[1], p1[0]-p0[0]);
				for (int i = 0; i < intersections.size(); i++)
				{
						Double[] p2 = intersections.get(i);
						double angle = Math.atan2(p2[1]-p1[1], p2[0]-p1[0]);
						if (wrapToPi(angle - old_angle) > -Math.PI/8) // angle should usually increase, certainly no sharp drops
						{
								path_xy.add(p2);
								p1 = p2;
								old_angle = angle;
						}
						else
						{
								Log.i(logTag, "inwardNextHull detected wrong spiral direction. Truncating");
								Log.d(logTag, String.format("wrong spiral, old_angle = %.6f, angle = %.6f", old_angle, angle));
								path_xy.add(local_centroid);
								last_min_dist_to_centroid = 9999999;
								last_max_dist_to_centroid = 9999999;
								return;
						}
				}
				path_xy.add(intersections.get(0).clone()); // close the hull before moving to inward hull
				inwardNextHull(intersections);
		}

		private void convexHull() throws Exception
		{
				// set convex_xy based on original_points
				ArrayList<Double[]> utm_points = latLngToUTM(original_points);
				ArrayList<Integer> convex_indices = GrahamScan.getConvexHull(utm_points);
				ArrayList<Double[]> utm_hull = new ArrayList<>();
				for (Integer i : convex_indices)
				{
						utm_hull.add(utm_points.get(i));
				}
				convex_xy = zeroCentroid(utm_hull);
				// Double[] should_be_zero_centroid = calculateCentroid(convex_xy);
		}

		private ArrayList<Double[]> latLngToUTM(ArrayList<LatLng> points)
		{
				ArrayList<Double[]> result = new ArrayList<>();
				for (LatLng wp : points)
				{
						UTM utm = UTM.latLongToUtm(LatLong.valueOf(wp.getLatitude(), wp.getLongitude(), NonSI.DEGREE_ANGLE), ReferenceEllipsoid.WGS84);
						result.add(new Double[]{utm.eastingValue(SI.METER), utm.northingValue(SI.METER)});
				}
				original_utm = UTM.latLongToUtm(LatLong.valueOf(points.get(0).getLatitude(), points.get(0).getLongitude(), NonSI.DEGREE_ANGLE), ReferenceEllipsoid.WGS84);
				return result;
		}

		private ArrayList<Double[]> zeroCentroid(ArrayList<Double[]> points)
		{
				utm_centroid = calculateCentroid(points);
				local_centroid = new Double[]{0., 0.};
				ArrayList<Double[]> result = new ArrayList<>();
				for (int i = 0; i < points.size(); i++)
				{
						Double[] p = points.get(i);
						result.add(new Double[]{p[0] - utm_centroid[0], p[1] - utm_centroid[1]});
				}
				return result;
		}

}

package org.mtransit.parser.ca_regina_transit_bus;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// http://open.regina.ca/
// http://open.regina.ca/dataset/transit-network
// https://opengis.regina.ca/reginagtfs/google_transit.zip
public class ReginaTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-regina-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new ReginaTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		MTLog.log("Generating Regina Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		MTLog.log("Generating Regina Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // using route short name as route ID
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = routeLongName.toLowerCase(Locale.ENGLISH);
		routeLongName = INDUSTRIAL.matcher(routeLongName).replaceAll(INDUSTRIAL_REPLACEMENT);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_BLUE = "0AB0DE"; // LIGHT BLUE (from PDF schedule)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		map2.put(1L, new RouteTripSpec(1L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Dieppe", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Broad North") //
				.addTripSort(0, //
						Arrays.asList(//
								"0060", // xx <> 7TH AVE N @ SMITH ST (EB)
								"0061", // !=
								"0069", // STURDY ST @ 12TH AVE N (NB)
								"0087", // !=
								"0059", // <>
								"0060", // xx <> 7TH AVE N @ SMITH ST (EB)
								"0089", // !=
								"1434", // ==
								"1272", // !=
								"1435", // !=
								"0096", // ==
								"0002" // COURTNEY ST @ DEWDNEY AVE (NB)
						)) //
				.addTripSort(1, //
						Arrays.asList(//
								"0002", // COURTNEY ST @ DEWDNEY AVE (NB)
								"1544", // 11TH AVE @ CORNWALL ST (EB)
								"0052", // 2ND AVE N @ BROAD ST (WB)
								"0058", // !=
								"0059", // <>
								"0060" // <> 7TH AVE N @ SMITH ST (EB)
						)) //
				.compileBothTripSort());
		map2.put(5L, new RouteTripSpec(5L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Uplands", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Downtown") //
				.addTripSort(0, //
						Arrays.asList(//
								"1545", // != 11TH AVE @ LORNE ST (WB) <=
								"0486", // !=
								"0488", // ARGYLE ST N @ 6TH AVE N (NB) <=
								"0161", // !=
								"1536", // == 6TH AVE N @ GARNET ST (EB)
								"0069" // STURDY ST @ 12TH AVE N (NB)
						)) //
				.addTripSort(1, //
						Arrays.asList(//
								"0069", // STURDY ST @ 12TH AVE N (NB)
								"0088", // 7TH AVE N @ SMITH ST (WB)
								"1455", // == 6TH AVE N @ SHEPHERD ST (WB)
								"0487", // != !=
								"0488", // <> != ARGYLE ST N @ 6TH AVE N (NB)
								"0160", // != !=
								"0161", // !=
								"0470", // ==
								"1545" // 11TH AVE @ LORNE ST (WB)
						)) //
				.compileBothTripSort());
		map2.put(15L, new RouteTripSpec(15L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(//
								"1537", // 11TH AVE @ SCARTH ST (EB)
								"1182", // WINNIPEG ST @ 13TH AVE (SB)
								"1189" // ROSE ST @ 13TH AVE (NB)
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(//
								"1189", // ROSE ST @ 13TH AVE (NB)
								"0691", // ++
								"1537" // 11TH AVE @ SCARTH ST (EB)
						)) //
				.compileBothTripSort());
		map2.put(16L, new RouteTripSpec(16L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(//
								"0202", // ROCHDALE BLVD @ SHERWOOD MALL (EB)
								"0204", // ++
								"1432" // STOCKTON ST @ CHILD AVE (SB)
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(//
								"1432", // STOCKTON ST @ CHILD AVE (SB)
								"1574", // MCEACHERN DR @ MAZURAK CRES (WB)
								"1423", // STOCKTON ST @ CHILD AVE (NB)
								"0202" // ROCHDALE BLVD @ SHERWOOD MALL (EB)
						)) //
				.compileBothTripSort());
		map2.put(17L, new RouteTripSpec(17L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(//
								"1450", // MAPLERIDGE DR @ MAPLETON BAY (SB)
								"0204", // == !=
								"1423", // != <> STOCKTON ST @ CHILD AVE (NB) => WEST
								"1451", // != !=
								"1589" // != VANSTONE ST @ BIG BEAR BLVD (SB)
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(//
								"1589", // VANSTONE ST @ BIG BEAR BLVD (SB)
								"0153", // !=
								"1423", // <> STOCKTON ST @ CHILD AVE (NB)
								"1424", // !=
								"0323", // ROCHDALE BLVD @ LAKEWOOD CRT (WB)
								"1450" // MAPLERIDGE DR @ MAPLETON BAY (SB)
						)) //
				.compileBothTripSort());
		map2.put(18L, new RouteTripSpec(18L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "University", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Harbour Lndg") //
				.addTripSort(0, //
						Arrays.asList(//
								"1566", // HARVARD WAY @ GRASSLANDS DR
								"0269", // !=
								"0270", // <> UNIVERSITY DR W @ RIDDELL CENTRE (NB)
								"0271", // !=
								"0382" // UNIVERSITY DR E @ FIRST NATIONS WAY (NB)
						)) //
				.addTripSort(1, //
						Arrays.asList(//
								"0382", // UNIVERSITY DR E @ FIRST NATIONS WAY (NB)
								"1228", // !=
								"0270", // <> UNIVERSITY DR W @ RIDDELL CENTRE (NB)
								"0250", // !=
								"0584", // RAE ST @ GOLDEN MILE (SB)
								"1566" // HARVARD WAY @ GRASSLANDS DR
						)) //
				.compileBothTripSort());
		map2.put(40L, new RouteTripSpec(40L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(//
								"1566", // HARVARD WAY @ GRASSLANDS DR (NB)
								"1423" // STOCKTON ST @ CHILD AVE (NB)
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(//
								"1423", // STOCKTON ST @ CHILD AVE (NB)
								"1429", // DIEFENBAKER DR @ BLAKE RD (EB)
								"1566" // HARVARD WAY @ GRASSLANDS DR (NB)
						)) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		if (StringUtils.isEmpty(mTrip.getHeadsignValue())) {
			mTrip.setHeadsignString(mTripToMerge.getHeadsignValue(), mTrip.getHeadsignId());
			return true;
		} else if (StringUtils.isEmpty(mTripToMerge.getHeadsignValue())) {
			mTrip.setHeadsignString(mTrip.getHeadsignValue(), mTrip.getHeadsignId());
			return true;
		}
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 4L) {
			if (Arrays.asList( //
					"Downtown", //
					"Walsh Acres" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Walsh Acres", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 7L) {
			if (Arrays.asList( //
					WHITMORE, // <>
					"Glencairn" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Glencairn", mTrip.getHeadsignId());
				return true;
			}
		}
		MTLog.log("Unexpected trips to merge %s & %s", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern EXPRESS = Pattern.compile("((^|\\W)(express)(\\W|$))", Pattern.CASE_INSENSITIVE);

	private static final String INDUSTRIAL_SHORT = "Ind";

	private static final Pattern INDUSTRIAL = Pattern.compile("((^|\\W)(industrial|indust)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String INDUSTRIAL_REPLACEMENT = "$2" + INDUSTRIAL_SHORT + "$4";

	private static final String WHITMORE = "Whitmore";

	private static final Pattern WHITMORE_PARK_ = Pattern.compile("((^|\\W)(whitmore|whitmore park|whitmore pk)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String WHITMORE_PARK_REPLACEMENT = "$2" + WHITMORE + "$4";

	private static final Pattern ARCOLA_VICTORIA_DOWNTOWN_EAST_ = Pattern.compile("((^|\\W)((arcola|victoria) (downtown|east))(\\W|$))",
			Pattern.CASE_INSENSITIVE);
	private static final String ARCOLA_VICTORIA_DOWNTOWN_EAST_REPLACEMENT = "$2" + "$5 $4 " + "$6";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		if (Utils.isUppercaseOnly(tripHeadsign, true, true)) {
			tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		}
		tripHeadsign = EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = INDUSTRIAL.matcher(tripHeadsign).replaceAll(INDUSTRIAL_REPLACEMENT);
		tripHeadsign = WHITMORE_PARK_.matcher(tripHeadsign).replaceAll(WHITMORE_PARK_REPLACEMENT);
		tripHeadsign = ARCOLA_VICTORIA_DOWNTOWN_EAST_.matcher(tripHeadsign).replaceAll(ARCOLA_VICTORIA_DOWNTOWN_EAST_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public String cleanStopName(String gStopName) {
		if (Utils.isUppercaseOnly(gStopName, true, true)) {
			gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		}
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = INDUSTRIAL.matcher(gStopName).replaceAll(INDUSTRIAL_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@NotNull
	@Override
	public String getStopCode(GStop gStop) {
		if (StringUtils.isEmpty(gStop.getStopCode())) {
			return gStop.getStopId(); // stop # used by real-time API
		}
		return super.getStopCode(gStop);
	}
}

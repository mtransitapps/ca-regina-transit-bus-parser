package org.mtransit.parser.ca_regina_transit_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
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
		System.out.printf("\nGenerating Regina Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Regina Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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

	private static final String ARCOLA_EAST = "Arcola East";
	private static final String ARGYLE_PARK = "Argyle Pk";
	private static final String EAST = "East";
	private static final String DOWNTOWN = "Downtown";
	private static final String HARBOUR_LANDING = "Harbour Lndg";
	private static final String UNIVERSITY = "University";
	private static final String UPLANDS = "Uplands";
	private static final String WOODLAND_GROVE = "Woodland Grv";
	private static final String GLENCAIRN = "Glencairn";

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(1L, new RouteTripSpec(1L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Dieppe", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Broad North") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"0060", // xx <> 7TH AVE N @ SMITH ST (EB)
								"0061", // !=
								"0069", // STURDY ST @ 12TH AVE N (NB)
								"0087", // !=
								"0059", // <>
								"0060", // xx <> 7TH AVE N @ SMITH ST (EB)
								"0089", // !=
								"0002", // COURTNEY ST @ DEWDNEY AVE (NB)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"0002", // COURTNEY ST @ DEWDNEY AVE (NB)
								"1544", // 11TH AVE @ CORNWALL ST (EB)
								"0052", // 2ND AVE N @ BROAD ST (WB)
								"0058", // !=
								"0059", // <>
								"0060", // <> 7TH AVE N @ SMITH ST (EB)
						})) //
				.compileBothTripSort());
		map2.put(2L, new RouteTripSpec(2L, //
				0, MTrip.HEADSIGN_TYPE_STRING, WOODLAND_GROVE, //
				1, MTrip.HEADSIGN_TYPE_STRING, ARGYLE_PARK) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"1432", // STOCKTON ST @ CHILD AVE (SB)
								"0807", // 14TH AVE @ COCHRANE HIGH (EB)
								"0819", // ==
								"1032", // !=
								"1442", // !=
								"0521", // !=
								"1453", // !=
								"0528", // == WESTFAIR RD @ SUPERSTORE (WB)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"0528", // WESTFAIR RD @ SUPERSTORE (WB)
								"1545", // 11TH AVE @ LORNE ST (WB)
								"0144", // SANGSTER BLVD @ STERN BAY (EB)
								"1432", // STOCKTON ST @ CHILD AVE (SB)
						})) //
				.compileBothTripSort());
		map2.put(5L, new RouteTripSpec(5L, //
				0, MTrip.HEADSIGN_TYPE_STRING, UPLANDS, //
				1, MTrip.HEADSIGN_TYPE_STRING, DOWNTOWN) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"1545", // 11TH AVE @ LORNE ST (WB)
								"1536", // != 6TH AVE N @ GARNET ST (EB)
								"0488", // <> != ARGYLE ST N @ 6TH AVE N (NB)
								"1454", // == 6TH AVE N @ ANGUS RD (EB)
								"0069", // STURDY ST @ 12TH AVE N (NB)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"0069", // STURDY ST @ 12TH AVE N (NB)
								"0088", // 7TH AVE N @ SMITH ST (WB)
								"1455", // == 6TH AVE N @ SHEPHERD ST (WB)
								"0488", // <> != ARGYLE ST N @ 6TH AVE N (NB)
								"0471", // == GARNET ST @ WOODWARD AVE (SB)
								"1545", // 11TH AVE @ LORNE ST (WB)
						})) //
				.compileBothTripSort());
		map2.put(7L, new RouteTripSpec(7L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Glencairn", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Whitmore Pk") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"0602", // SOUTHLAND MALL @ PLAINSVIEW RD (WB)
								"0653", // ++
								"0528", // WESTFAIR RD @ SUPERSTORE (WB)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"0528", // WESTFAIR RD @ SUPERSTORE (WB)
								"0558", // DEWDNEY AVE @ CAVENDISH ST (WB)
								"0602", // SOUTHLAND MALL @ PLAINSVIEW RD (WB)
						})) //
				.compileBothTripSort());
		map2.put(15L, new RouteTripSpec(15L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"1537", // 11TH AVE @ SCARTH ST (EB)
								"1182", // WINNIPEG ST @ 13TH AVE (SB)
								"1189", // ROSE ST @ 13TH AVE (NB)
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"1189", // ROSE ST @ 13TH AVE (NB)
								"0691", // ++
								"1537", // 11TH AVE @ SCARTH ST (EB)
						})) //
				.compileBothTripSort());
		map2.put(16L, new RouteTripSpec(16L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"0202", // ROCHDALE BLVD @ SHERWOOD MALL (EB)
								"0204", // ++
								"1432", // STOCKTON ST @ CHILD AVE (SB)
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"1432", // STOCKTON ST @ CHILD AVE (SB)
								"1574", // MCEACHERN DR @ MAZURAK CRES (WB)
								"1423", // STOCKTON ST @ CHILD AVE (NB)
								"0202", // ROCHDALE BLVD @ SHERWOOD MALL (EB)
						})) //
				.compileBothTripSort());
		map2.put(17L, new RouteTripSpec(17L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"1450", // MAPLERIDGE DR @ MAPLETON BAY (SB)
								"0204", // == !=
								"1423", // != <> STOCKTON ST @ CHILD AVE (NB) => WEST
								"1451", // != !=
								"1589", // != VANSTONE ST @ BIG BEAR BLVD (SB)
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"1589", // VANSTONE ST @ BIG BEAR BLVD (SB)
								"0153", // !=
								"1423", // <> STOCKTON ST @ CHILD AVE (NB)
								"1424", // !=
								"0323", // ROCHDALE BLVD @ LAKEWOOD CRT (WB)
								"1450", // MAPLERIDGE DR @ MAPLETON BAY (SB)
						})) //
				.compileBothTripSort());
		map2.put(18L, new RouteTripSpec(18L, //
				0, MTrip.HEADSIGN_TYPE_STRING, UNIVERSITY, //
				1, MTrip.HEADSIGN_TYPE_STRING, HARBOUR_LANDING) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"1566", // HARVARD WAY @ GRASSLANDS DR
								"0269", // !=
								"0270", // <> UNIVERSITY DR W @ RIDDELL CENTRE (NB)
								"0271", // !=
								"0382", // UNIVERSITY DR E @ FIRST NATIONS WAY (NB)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"0382", // UNIVERSITY DR E @ FIRST NATIONS WAY (NB)
								"1228", // !=
								"0270", // <> UNIVERSITY DR W @ RIDDELL CENTRE (NB)
								"0250", // !=
								"0584", // RAE ST @ GOLDEN MILE (SB)
								"1397", // PARLIAMENT AVE @ HARBOUR LANDING DRIVE (WB)
								"1566", // HARVARD WAY @ GRASSLANDS DR
						})) //
				.compileBothTripSort());
		map2.put(21L, new RouteTripSpec(21L, //
				0, MTrip.HEADSIGN_TYPE_STRING, UNIVERSITY, //
				1, MTrip.HEADSIGN_TYPE_STRING, GLENCAIRN) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"0752", // CAVENDISH ST @ 9TH AVE (NB)
								"0520", // UNIVERSITY PARK DR @ VIC SQ MALL (SB)
								"0249", // UNIVERSITY DR W @ RIDDELL CENTRE (SB)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"0249", // UNIVERSITY DR W @ RIDDELL CENTRE (SB)
								"0383", // ==
								"0384", // != SASKATCHEWAN POLYTECHNIC @ MAIN CAMPUS (NB)
								"0838", // ==
								"0752", // CAVENDISH ST @ 9TH AVE (NB)
						})) //
				.compileBothTripSort());
		map2.put(22L, new RouteTripSpec(22L, //
				0, MTrip.HEADSIGN_TYPE_STRING, UNIVERSITY, //
				1, MTrip.HEADSIGN_TYPE_STRING, ARCOLA_EAST) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"0830", // WOODHAMS RD@SS LEISURE CENTRE (EB)
								"0831", // ASSINIBOINE AVE @ PRINCE OF WALES (WB)
								"0270", // UNIVERSITY DR W @ RIDDELL CENTRE (NB)
								"0587", // MASSEY RD @ PARKER AVE (SB)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"0587", // MASSEY RD @ PARKER AVE (SB)
								"0249", // UNIVERSITY DR W @ RIDDELL CENTRE (SB)
								"0830", // WOODHAMS RD@SS LEISURE CENTRE (EB)
						})) //
				.compileBothTripSort());
		map2.put(40L, new RouteTripSpec(40L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"1566", // HARVARD WAY @ GRASSLANDS DR (NB)
								"1423", // STOCKTON ST @ CHILD AVE (NB)
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"1423", // STOCKTON ST @ CHILD AVE (NB)
								"1429", // DIEFENBAKER DR @ BLAKE RD (EB)
								"1566", // HARVARD WAY @ GRASSLANDS DR (NB)
						})) //
				.compileBothTripSort());
		map2.put(50L, new RouteTripSpec(50L, //
				0, MTrip.HEADSIGN_TYPE_STRING, EAST, //
				1, MTrip.HEADSIGN_TYPE_STRING, DOWNTOWN) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"1544", // 11TH AVE @ CORNWALL ST (EB)
								"1420", // DEWDNEY AVE @ PRINCE OF WALES DR (EB)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"1420", // DEWDNEY AVE @ PRINCE OF WALES DR (EB)
								"1544", // 11TH AVE @ CORNWALL ST (EB)
						})) //
				.compileBothTripSort());
		map2.put(60L, new RouteTripSpec(60L, //
				0, MTrip.HEADSIGN_TYPE_STRING, EAST, //
				1, MTrip.HEADSIGN_TYPE_STRING, DOWNTOWN) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"1333", // 11TH AVE @ SCARTH ST (WB)
								"0829", // !=
								"0830", // <> WOODHAMS RD@SS LEISURE CENTRE (EB)
								"1518", // !=
								"1614", // GREEN BANK RD @ GREEN PINE GATE (NB)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"1614", // GREEN BANK RD @ GREEN PINE GATE (NB)
								"1517", // !=
								"0830", // <> WOODHAMS RD@SS LEISURE CENTRE (EB)
								"1023", // !=
								"1333", // 11TH AVE @ SCARTH ST (WB)
						})) //
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
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (StringUtils.isEmpty(mTrip.getHeadsignValue())) {
			mTrip.setHeadsignString(mTripToMerge.getHeadsignValue(), mTrip.getHeadsignId());
			return true;
		} else if (StringUtils.isEmpty(mTripToMerge.getHeadsignValue())) {
			mTrip.setHeadsignString(mTrip.getHeadsignValue(), mTrip.getHeadsignId());
			return true;
		}
		if (mTrip.getRouteId() == 4L) {
			if (Arrays.asList( //
					"Downtown", //
					"Walsh Acres" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Walsh Acres", mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexpected trips to merge %s & %s\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern EXPRESS = Pattern.compile("((^|\\W){1}(express)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

	private static final String INDUSTRIAL_SHORT = "Ind";

	private static final Pattern INDUSTRIAL = Pattern.compile("((^|\\W){1}(industrial)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String INDUSTRIAL_REPLACEMENT = "$2" + INDUSTRIAL_SHORT + "$4";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		tripHeadsign = EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = INDUSTRIAL.matcher(tripHeadsign).replaceAll(INDUSTRIAL_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern ENDS_WITH_BOUND = Pattern.compile("([\\s]*\\([\\s]*[s|e|w|n]b\\)$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern AT_SIGN = Pattern.compile("([\\s]*@[\\s]*)", Pattern.CASE_INSENSITIVE);
	private static final String AT_SIGN_REPLACEMENT = " / ";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		gStopName = ENDS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = AT_SIGN.matcher(gStopName).replaceAll(AT_SIGN_REPLACEMENT);
		gStopName = INDUSTRIAL.matcher(gStopName).replaceAll(INDUSTRIAL_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}

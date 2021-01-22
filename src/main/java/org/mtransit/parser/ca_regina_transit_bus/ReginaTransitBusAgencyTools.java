package org.mtransit.parser.ca_regina_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.StringUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// http://open.regina.ca/
// http://open.regina.ca/dataset/transit-network
// https://opengis.regina.ca/reginagtfs/google_transit.zip
public class ReginaTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-regina-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new ReginaTransitBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Regina Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Regina Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // using route short name as route ID
	}

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongNameOrDefault();
		routeLongName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, routeLongName, getIgnoredWord());
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_BLUE = "0AB0DE"; // LIGHT BLUE (from PDF schedule)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("Unexpected trips to merge %s & %s", mTrip, mTripToMerge);
	}

	private static final Pattern EXPRESS = Pattern.compile("((^|\\W)(express)(\\W|$))", Pattern.CASE_INSENSITIVE);

	private static final String WHITMORE = "Whitmore";
	private static final Pattern WHITMORE_PARK_ = Pattern.compile("((^|\\W)(whitmore|whitmore park|whitmore pk)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String WHITMORE_PARK_REPLACEMENT = "$2" + WHITMORE + "$4";

	private static final Pattern ARCOLA_VICTORIA_DOWNTOWN_EAST_ = Pattern.compile("((^|\\W)((arcola|victoria) (downtown|east))(\\W|$))",
			Pattern.CASE_INSENSITIVE);
	private static final String ARCOLA_VICTORIA_DOWNTOWN_EAST_REPLACEMENT = "$2" + "$5 $4 " + "$6";

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWord());
		tripHeadsign = CleanUtils.cleanBounds(tripHeadsign);
		tripHeadsign = EXPRESS.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = WHITMORE_PARK_.matcher(tripHeadsign).replaceAll(WHITMORE_PARK_REPLACEMENT);
		tripHeadsign = ARCOLA_VICTORIA_DOWNTOWN_EAST_.matcher(tripHeadsign).replaceAll(ARCOLA_VICTORIA_DOWNTOWN_EAST_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private String[] getIgnoredWord() {
		return new String[]{
				"EB", "WB", "NB", "SB"
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWord());
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		if (StringUtils.isEmpty(gStop.getStopCode())) {
			//noinspection deprecation
			return gStop.getStopId(); // stop # used by real-time API
		}
		return super.getStopCode(gStop);
	}
}

package com.wbd.distribute.workflowsyncservice.constants;

import java.util.Arrays;
import java.util.List;

public final class AttributeConstants {


    /**
     * Constants for the attributes in plea message - metadatachanged
     */
    public static final String METADATA_ANTICIPATED_LAUNCH_DATE = "playlist.anticipatedLaunch";
    public static final String METADATA_SHOW_SCRID = "playlist.showScrid";
    public static final String METADATA_SHOW_UUID = "playlist.showUUID";
    public static final String METADATA_SERIES_SCRID = "playlist.seriesScrid";
    public static final String METADATA_SERIES_UUID = "playlist.seriesUUID";
    public static final String METADATA_BRAND_ABBR = "playlist.brandAbbreviation";
    public static final String METADATA_PLAYLIST_FORMAT = "playlist.assetFormat";
    public static final String METADATA_PLAYLIST_SEASON_ID = "playlist.playlistSeasonId";
    public static final String METADATA_PLAYLIST_SERIES_ID = "playlist.seriesId";
    public static final String METADATA_SEASON_NUMBER = "playlist.seasonNumber";
    public static final String METADATA_TRANSCODE_NEEDED = "asset.transcodeNeeded";
    public static final String METADATA_PARTNER_SEASON_ID = "partner.partnerSeasonId";
    public static final String METADATA_PARTNER_SERIES_ID = "partner.seriesId";
    public static final String METADATA_MARKET_LEVEL_BRAND_ABBR = "playlist.marketLevelBrandAbbreviations";
    public static final String TASK_ATTR_PLAYLIST_FORMAT = "playlist.playlistFormat";
    public static final String TASK_ATTR_EDIT_UUID = "editUUID";

    public static final List<String> EXPECTED_PLAYLIST_ATTS = Arrays.asList(METADATA_ANTICIPATED_LAUNCH_DATE, METADATA_SHOW_SCRID, METADATA_SERIES_SCRID,
            METADATA_BRAND_ABBR, METADATA_PLAYLIST_SERIES_ID, METADATA_PLAYLIST_SEASON_ID, METADATA_PLAYLIST_FORMAT, METADATA_SEASON_NUMBER);
    public static final List<String> EXPECTED_PARTNER_PLAYLIST_ATTS = Arrays.asList(METADATA_PARTNER_SEASON_ID, METADATA_PARTNER_SERIES_ID);
    public static final List<String> EXPECTED_PLAYLIST_ASSET_ATTS = Arrays.asList(METADATA_TRANSCODE_NEEDED);
    public static final List<String> EXPECTED_PLAYLIST_MARKET_ATTS = Arrays.asList(METADATA_MARKET_LEVEL_BRAND_ABBR);
    public static final List<String> EXPECTED__ATTRIBUTES = Arrays.asList(
            METADATA_ANTICIPATED_LAUNCH_DATE, METADATA_SHOW_SCRID, METADATA_SERIES_SCRID,
            METADATA_BRAND_ABBR, METADATA_PLAYLIST_SERIES_ID, METADATA_PLAYLIST_SEASON_ID, METADATA_PLAYLIST_FORMAT,
            METADATA_SEASON_NUMBER, METADATA_TRANSCODE_NEEDED, METADATA_PARTNER_SEASON_ID,
            METADATA_PARTNER_SERIES_ID, METADATA_MARKET_LEVEL_BRAND_ABBR, TASK_ATTR_EDIT_UUID
    );
}
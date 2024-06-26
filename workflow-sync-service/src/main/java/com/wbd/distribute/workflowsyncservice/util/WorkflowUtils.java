package com.wbd.distribute.workflowsyncservice.util;

import com.scrippsnetworks.nonlinear.planning.api.*;
import com.wbd.distribute.workflowsyncservice.constants.WorkflowSyncServiceConstants;

import java.util.*;

import static com.wbd.distribute.workflowsyncservice.constants.AttributeConstants.*;
import static com.wbd.distribute.workflowsyncservice.constants.WorkflowSyncServiceConstants.TASK_ID_PLANNING;
import static com.wbd.distribute.workflowsyncservice.constants.WorkflowSyncServiceConstants.TASK_ID_PLANNING_DV;

public class WorkflowUtils {

    public static String getStatus(Playlist playlist, String taskId) {
        return switch (taskId) {
            case TASK_ID_PLANNING -> getNLCDPlanTaskStatus(playlist);
            case TASK_ID_PLANNING_DV -> getDVPlanningTaskStatus(playlist);
            default -> "";
        };
    }

    public static String getNLCDPlanTaskStatus(Playlist playlist) {
        PlaylistAsset firstAsset = playlist.getAssets().stream().findFirst().orElse(null);
        if (firstAsset == null || firstAsset.getLaunchDates() == null || firstAsset.getLaunchDates().isEmpty()) {
            return WorkflowSyncServiceConstants.TASK_VALUE_UNSCHEDULED;
        }

        boolean anyScheduled = firstAsset.getLaunchDates().values().stream()
                .flatMap(List::stream)
                .anyMatch(market -> market.getLaunchDate() != null);

        return anyScheduled ? WorkflowSyncServiceConstants.TASK_VALUE_SCHEDULED : WorkflowSyncServiceConstants.TASK_VALUE_UNSCHEDULED;
    }

    public static String getDVPlanningTaskStatus(Playlist playlist) {
        PlaylistAsset playlistAsset = playlist.getAssets().stream().findFirst().orElse(null);
        if (playlistAsset == null) {
            return WorkflowSyncServiceConstants.TASK_VALUE_NOT_APPLICABLE;
        }
        return determineDVStatus(playlistAsset);
    }

    private static String determineDVStatus(PlaylistAsset asset) {
        if (asset.getDistributionValues() == null || asset.getDistributionValues().isEmpty() || asset.getEdits() == null || asset.getEdits().isEmpty()) {
            return WorkflowSyncServiceConstants.TASK_VALUE_NOT_APPLICABLE;
        } else if (!areLaunchDatesPresent(asset.getLaunchDates())) {
            return WorkflowSyncServiceConstants.TASK_VALUE_MISSING;
        } else if (isExpirationDateInPast(asset.getExpirationDates())) {
            return WorkflowSyncServiceConstants.TASK_VALUE_EXPIRED;
        } else if (hasValidationError(asset.getEdits())) {
            return WorkflowSyncServiceConstants.TASK_VALUE_UNCONFIRMED;
        } else {
            return WorkflowSyncServiceConstants.TASK_VALUE_CONFIRMED;
        }
    }

    private static boolean areLaunchDatesPresent(Map<UUID, List<PlaylistAssetPartnerMarket>> launchDates) {
        return launchDates != null && !launchDates.isEmpty() &&
                launchDates.values().stream()
                        .flatMap(List::stream)
                        .map(PlaylistAssetPartnerMarket::getLaunchDate)
                        .allMatch(Objects::nonNull);
    }

    private static boolean hasValidationError(Map<UUID, List<PlaylistAssetPartnerMarket>> edits) {
        return edits.values().stream()
                .flatMap(List::stream)
                .map(PlaylistAssetPartnerMarket::getAttributes)
                .anyMatch(attributes -> attributes != null && false /* attributes.getValidationError() != null && attributes.getValidationError() */);
    }

    private static boolean isExpirationDateInPast(Map<UUID, List<PlaylistAssetPartnerMarket>> expirationDates) {
        if (expirationDates == null || expirationDates.isEmpty()) {
            return false; // Assuming if there are no expiration dates, it can't be in the past
        }
        return expirationDates.values().stream()
                .flatMap(List::stream)
                .map(PlaylistAssetPartnerMarket::getExpirationDate)
                .filter(Objects::nonNull)
                .anyMatch(expirationDate -> expirationDate.isBefore(java.time.Instant.now()));
    }

//    public static Map<String, String> getAttributes(Playlist playlist, List<String> expectedAttributes, String taskId) {
//        return switch (taskId) {
//            case TASK_ID_PLANNING -> Collections.emptyMap();
//            case TASK_ID_PLANNING_DV -> getDVPlanningTaskAttributes(playlist,expectedAttributes);
//            default -> Collections.emptyMap();
//        };
//    }

    public static Map<String, String> getAttributes(Playlist playlist, List<String> expectedAttributes) {
        Map<String, String> attributes = new HashMap<>();

        if (playlist == null || playlist.getAssets() == null || playlist.getAssets().isEmpty()) {
            return attributes;
        }

        PlaylistAsset playlistAsset = playlist.getAssets().stream().findFirst().orElse(null);
        if (playlistAsset == null || playlistAsset.getEdits() == null || playlistAsset.getEdits().isEmpty()) {
            return attributes;
        }

        PartnerPlaylist partnerPlaylist = playlist.getPartners().getFirst();

        UUID partnerId = playlistAsset.getEdits().keySet().stream().findFirst().orElse(null);
        if (partnerId == null) {
            return attributes;
        }

        UUID marketId = playlistAsset.getEdits().values().stream()
                .findFirst()
                .flatMap(papmList -> papmList.stream().findFirst().map(PlaylistAssetPartnerMarket::getMarketId))
                .orElse(null);

        expectedAttributes.forEach(metaDataChange -> {
            switch (metaDataChange) {
                case METADATA_ANTICIPATED_LAUNCH_DATE:
                    attributes.put(metaDataChange, Optional.ofNullable(playlist.getAnticipatedLaunch()).map(Object::toString).orElse(null));
                    break;
                case METADATA_SERIES_SCRID:
                    attributes.put(metaDataChange, playlist.getSeriesScrid());
                    attributes.put(METADATA_SERIES_UUID, Optional.ofNullable(playlist.getSeasonUUID()).map(UUID::toString).orElse(null));
                    break;
                case METADATA_SHOW_SCRID:
                    attributes.put(metaDataChange, playlist.getShowScrid());
                    attributes.put(METADATA_SHOW_UUID, Optional.ofNullable(playlist.getShowUUID()).map(UUID::toString).orElse(null));
                    break;
                case METADATA_SEASON_NUMBER:
                    attributes.put(metaDataChange, playlist.getSeasonNumber());
                    break;
                case METADATA_PLAYLIST_FORMAT:
                    attributes.put(TASK_ATTR_PLAYLIST_FORMAT, playlist.getPlaylistFormat());
                    break;
                case METADATA_PLAYLIST_SEASON_ID:
                    attributes.put(metaDataChange, playlist.getPlaylistSeasonId());
                    break;
                case METADATA_PLAYLIST_SERIES_ID:
                    attributes.put(metaDataChange, playlist.getSeriesId());
                    break;
                case METADATA_BRAND_ABBR:
                    String brandAbbreviation = Optional.ofNullable(playlist.getBrandAbbreviation())
                            .map(Brand::getAbbreviation)
                            .filter(abbr -> !abbr.isEmpty())
                            .orElseGet(() -> Optional.ofNullable(playlistAsset.getAssetMetaInfo())
                                    .map(AssetMetaInfo::getAsset)
                                    .map(Asset::getBrand)
                                    .orElse(null));
                    attributes.put(metaDataChange, brandAbbreviation);
                    break;
                case METADATA_PARTNER_SEASON_ID:
                    attributes.put(metaDataChange, Optional.ofNullable(partnerPlaylist).map(PartnerPlaylist::getPartnerSeasonId).orElse(null));
                    break;
                case METADATA_PARTNER_SERIES_ID:
                    attributes.put(metaDataChange, Optional.ofNullable(partnerPlaylist).map(PartnerPlaylist::getSeriesId).orElse(null));
                    break;
                case METADATA_TRANSCODE_NEEDED:
                    String transcodeNeeded = Optional.ofNullable(playlistAsset.getTranscodeNeeded())
                            .map(transcodeNeededMap -> transcodeNeededMap.get(partnerId))
                            .flatMap(papmList -> papmList.stream()
                                    .filter(papm -> {
                                        assert marketId != null;
                                        return marketId.equals(papm.getMarketId());
                                    })
                                    .map(papm -> String.valueOf(papm.getTranscodeNeeded()))
                                    .findFirst())
                            .orElse(null);
                    attributes.put(metaDataChange, transcodeNeeded);
                    break;
                case METADATA_MARKET_LEVEL_BRAND_ABBR:
                    String marketBrand = Optional.ofNullable(playlist.getMarketLevelBrandAbbreviations())
                            .map(marketBrandMap -> marketBrandMap.get(partnerId))
                            .flatMap(marketBrandList -> marketBrandList.stream()
                                    .filter(market -> {
                                        assert marketId != null;
                                        return marketId.equals(market.getMarketId());
                                    })
                                    .map(PlaylistPartnerMarket::getBrand)
                                    .map(Brand::getAbbreviation)
                                    .findFirst())
                            .orElse(null);
                    attributes.put(metaDataChange, marketBrand);
                    break;
                case TASK_ATTR_EDIT_UUID:
                    attributes.put(metaDataChange, playlistAsset.getEdits().values().stream()
                            .flatMap(List::stream)
                            .map(PlaylistAssetPartnerMarket::getEditUUID)
                            .filter(Objects::nonNull)
                            .map(UUID::toString)
                            .findFirst()
                            .orElse(null));
                    break;
            }
        });
        return attributes;
    }
}
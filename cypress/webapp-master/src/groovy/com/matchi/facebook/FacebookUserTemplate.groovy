package com.matchi.facebook

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.social.facebook.api.GraphApi
import org.springframework.social.facebook.api.ImageType
import org.springframework.social.facebook.api.PagedList
import org.springframework.social.facebook.api.Permission
import org.springframework.social.facebook.api.PlaceTag
import org.springframework.social.facebook.api.Reference
import org.springframework.social.facebook.api.User
import org.springframework.social.facebook.api.UserIdForApp
import org.springframework.social.facebook.api.UserOperations
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

class FacebookUserTemplate implements UserOperations {
    private final GraphApi graphApi

    private final RestTemplate restTemplate
    String[] newFields = ["id", "email"]

    /*
    String[] newFields = ["id", "about", "age_range", "birthday", "cover", "currency", "devices", "education", "email", "favorite_athletes", "favorite_teams", "first_name", "gender", "hometown", "inspirational_people", "installed", "install_type", "is_verified", "languages", "last_name", "link", "locale", "location", "meeting_for", "middle_name", "name", "name_format", "political", "quotes", "payment_pricepoints", "relationship_status", "religion", "security_settings", "significant_other", "sports", "test_group", "timezone", "third_party_id", "updated_time", "verified", "video_upload_limits", "viewer_can_send_gift", "website", "work"]
     */
    FacebookUserTemplate(GraphApi graphApi, RestTemplate restTemplate) {
        this.graphApi = graphApi
        this.restTemplate = restTemplate
    }

    User getUserProfile() {
        return getUserProfile("me")
    }

    User getUserProfile(String facebookId) {
        return graphApi.fetchObject(facebookId, User.class, this.newFields)
    }

    byte[] getUserProfileImage() {
        return getUserProfileImage("me", ImageType.NORMAL)
    }

    byte[] getUserProfileImage(String userId) {
        return getUserProfileImage(userId, ImageType.NORMAL)
    }

    byte[] getUserProfileImage(ImageType imageType) {
        return getUserProfileImage("me", imageType)
    }

    byte[] getUserProfileImage(String userId, ImageType imageType) {
        return graphApi.fetchImage(userId, "picture", imageType)
    }

    byte[] getUserProfileImage(Integer width, Integer height) {
        return getUserProfileImage("me", width, height)
    }

    byte[] getUserProfileImage(String userId, Integer width, Integer height) {
        return graphApi.fetchImage(userId, "picture", width, height)
    }

    List<Permission> getUserPermissions() {
        JsonNode responseNode = restTemplate.getForObject(graphApi.getBaseGraphApiUrl() + "me/permissions", JsonNode.class);
        return deserializePermissionsNodeToList(responseNode)
    }

    List<UserIdForApp> getIdsForBusiness() {
        return graphApi.fetchConnections("me", "ids_for_business", UserIdForApp.class)
    }

    List<PlaceTag> getTaggedPlaces() {
        return graphApi.fetchConnections("me", "tagged_places", PlaceTag.class)
    }

    PagedList<Reference> search(String query) {
        MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<String, String>()
        queryMap.add("q", query)
        queryMap.add("type", "user")
        return graphApi.fetchConnections("search", null, Reference.class, queryMap)
    }

    private List<Permission> deserializePermissionsNodeToList(JsonNode jsonNode) {
        JsonNode dataNode = jsonNode.get("data")
        List<Permission> permissions = new ArrayList<Permission>()
        for (Iterator<JsonNode> elementIt = dataNode.elements(); elementIt.hasNext(); ) {
            JsonNode permissionsElement = elementIt.next()
            String name = permissionsElement.get("permission").asText()
            String status = permissionsElement.get("status").asText()
            permissions.add(new Permission(name, status))
        }
        return permissions
    }
}

package io.github.wellpereiradevs.quarkussocial.rest;

import io.github.wellpereiradevs.quarkussocial.domain.model.Follower;
import io.github.wellpereiradevs.quarkussocial.domain.model.User;
import io.github.wellpereiradevs.quarkussocial.domain.repository.FollowerRepository;
import io.github.wellpereiradevs.quarkussocial.domain.repository.UserRepository;
import io.github.wellpereiradevs.quarkussocial.rest.dto.CreateFollowerRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp() {
        //Standard test user
        var user = new User();
        user.setAge(30);
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();

        //Follower
        var follower = new User();
        follower.setAge(30);
        follower.setName("Fulano");
        userRepository.persist(follower);
        followerId = follower.getId();

        //Create a follower
        var followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);
    }

    @Test
    @DisplayName("should return 409 when followerId is equal to userId")
    public void sameUserAsFollowerTest(){

        var body = new CreateFollowerRequest();
        body.setFollowerId(userId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
                .when()
                .put()
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode())
                .body(Matchers.is("You can't follow yourself"));
    }

    @Test
    @DisplayName("should return 404 on follow a user when userId doesn't exist")
    public void userNotFoundWhenTryingToFollowTest(){

        var body = new CreateFollowerRequest();
        body.setFollowerId(userId);

        var nonExistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", nonExistentUserId)
                .when()
                .put()
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should follow a user")
    public void followUserTest(){

        var body = new CreateFollowerRequest();
        body.setFollowerId(followerId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
                .when()
                .put()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @DisplayName("should return 404 on list user followers and userId doesn't exist")
    public void userNotFoundWhenListingFollowersTest(){

        var nonExistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", nonExistentUserId)
                .when()
                .get()
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should list a user's followers")
    public void listingFollowersTest(){
        var response =
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId)
                .when()
                .get()
                .then()
                .extract().response();
        var followersCount = response.jsonPath().get("followersCount");
        var followersContent = response.jsonPath().getList("content");
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followersCount);
        assertEquals(1, followersContent.size());
    }

    @Test
    @DisplayName("should return 404 on unfollow user and userId doesn't exist")
    public void userNotFoundWhenUnfollowAUserTest(){

        var nonExistentUserId = 999;

        given()
                .pathParam("userId", nonExistentUserId)
                .queryParam("followerId", followerId)
                .when()
                .delete()
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should unfollow an user")
    public void unfollowUserTest(){

        given()
                .pathParam("userId", userId)
                .queryParam("followerId", followerId)
                .when()
                .delete()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}
package io.github.wellpereiradevs.quarkussocial.rest;

import io.github.wellpereiradevs.quarkussocial.domain.model.User;
import io.github.wellpereiradevs.quarkussocial.domain.repository.UserRepository;
import io.github.wellpereiradevs.quarkussocial.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
@Transactional
class PostResourceTest {

    @Inject
    UserRepository userRepository;
    Long userId;

    @BeforeEach
    public void setUP() {
        var user = new User();
        user.setAge(30);
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();
    }

    @Test
    @DisplayName("should create a post for a user")
    public void createPostTest() {
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");
        var userID = 1;

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParams("userId", userID)
                .when()
                .post()
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("should return 404 when trying to make a post for an nonexistent user")
    public void postForAnNonexistentUserTest(){
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        var nonexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParam("userId", nonexistentUserId)
                .when()
                .post()
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("should return 404 when user doesn't exist")
    public void listPostUserNotFoundTest() {
        var nonexistentUserId = 999;
        given()
                .pathParam("userId", nonexistentUserId)
                .when()
                .get()
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("should return 400 when followerId is not present")
    public void listPostFollowerHeaderNotSendTest() {
        given()
                .pathParam("userId", userId)
                .when()
                .get()
                .then()
                .statusCode(400)
                .body(Matchers.is("You forgot the header: followerId"));
    }

    @Test
    @DisplayName("should return 400 when follower doesn't exist")
    public void listPostFollowerNotFoundTest() {
        var NonexistentFollowerId = 999;
        given()
                .pathParam("userId", userId)
                .header("followerId", NonexistentFollowerId)
                .when()
                .get()
                .then()
                .statusCode(400)
                .body(Matchers.is("Nonexistent followerId"));
    }

    @Test
    @DisplayName("should return 403 when follower isn't a follower")
    public void listPostNotAFollowerNotFoundTest() {

    }

    @Test
    @DisplayName("should return posts")
    public void listPostsTest() {

    }
}
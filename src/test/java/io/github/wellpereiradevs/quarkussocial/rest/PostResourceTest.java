package io.github.wellpereiradevs.quarkussocial.rest;

import io.github.wellpereiradevs.quarkussocial.domain.model.Follower;
import io.github.wellpereiradevs.quarkussocial.domain.model.Post;
import io.github.wellpereiradevs.quarkussocial.domain.model.User;
import io.github.wellpereiradevs.quarkussocial.domain.repository.FollowerRepository;
import io.github.wellpereiradevs.quarkussocial.domain.repository.PostRepository;
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

    @Inject
    FollowerRepository followerRepository;

    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    public void setUP() {

        //Standard test user
        var user = new User();
        user.setAge(30);
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();

        //Post created for the user
        Post post = new Post();
        post.setText("Hello");
        post.setUser(user);
        postRepository.persist(post);

        //User who doesn't follow anyone
        var userNotFollower = new User();
        userNotFollower.setAge(33);
        userNotFollower.setName("Ciclano");
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        //Follower user
        var userFollower = new User();
        userFollower.setAge(31);
        userFollower.setName("Beltrano");
        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);
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
        var nonexistentFollowerId = 999;
        given()
                .pathParam("userId", userId)
                .header("followerId", nonexistentFollowerId)
                .when()
                .get()
                .then()
                .statusCode(400)
                .body(Matchers.is("Nonexistent followerId"));
    }

    @Test
    @DisplayName("should return 403 when follower ins't a follower")
    public void listPostNotAFollower() {
        given()
                .pathParam("userId", userId)
                .header("followerId", userNotFollowerId)
                .when()
                .get()
                .then()
                .statusCode(403)
                .body(Matchers.is("You can't see these posts."));
    }

    @Test
    @DisplayName("should list posts")
    public void listPostsTest() {
        given()
                .pathParam("userId", userId)
                .header("followerId", userFollowerId)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }
}
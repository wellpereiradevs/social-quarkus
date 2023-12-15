package io.github.wellpereiradevs.quarkussocial.rest;

import io.github.wellpereiradevs.quarkussocial.domain.model.Follower;
import io.github.wellpereiradevs.quarkussocial.domain.repository.FollowerRepository;
import io.github.wellpereiradevs.quarkussocial.domain.repository.UserRepository;
import io.github.wellpereiradevs.quarkussocial.rest.dto.CreateFollowerRequest;
import io.github.wellpereiradevs.quarkussocial.rest.dto.FollowerResponse;
import io.github.wellpereiradevs.quarkussocial.rest.dto.FollowersPerUseResponse;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {

    private UserRepository userRepository;
    private FollowerRepository followerRepository;

    @Inject
    public FollowerResource(UserRepository userRepository, FollowerRepository followerRepository) {
        this.userRepository = userRepository;
        this.followerRepository = followerRepository;
    }

    @PUT
    @Transactional
    public Response followUser(@PathParam("userId") Long userId, CreateFollowerRequest followerRequest) {

        if (userId.equals(followerRequest.getFollowerId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("You can't follow yourself")
                    .build();
        }

        var user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var follower = userRepository.findById(followerRequest.getFollowerId());

        boolean follows = followerRepository.follows(follower, user);

        if (!follows) {
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);

            followerRepository.persist(entity);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    public Response listFollowers(@PathParam("userId") Long userId) {
        var user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var list = followerRepository.findByUser(userId);
        FollowersPerUseResponse responseObject = new FollowersPerUseResponse();
        responseObject.setFollowersCount(list.size());

        var followerList = list.stream().map(FollowerResponse::new)
                .collect(Collectors.toList());

        responseObject.setContent(followerList);
        return Response.ok(responseObject).build();
    }

    @DELETE
    @Transactional
    public Response unFollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId) {
        var user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        followerRepository.deleteByFollowAndUser(followerId, userId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}

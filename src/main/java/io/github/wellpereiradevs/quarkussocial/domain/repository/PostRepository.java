package io.github.wellpereiradevs.quarkussocial.domain.repository;

import io.github.wellpereiradevs.quarkussocial.domain.model.Post;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PostRepository implements PanacheRepository<Post> {
}

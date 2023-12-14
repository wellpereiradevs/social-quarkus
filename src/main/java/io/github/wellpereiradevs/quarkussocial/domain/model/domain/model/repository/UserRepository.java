package io.github.wellpereiradevs.quarkussocial.domain.model.domain.model.repository;

import io.github.wellpereiradevs.quarkussocial.domain.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
}

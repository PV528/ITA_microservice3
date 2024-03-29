package org.acme.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.User;

@ApplicationScoped
public class UserRepository implements ReactivePanacheMongoRepository<User> {

}

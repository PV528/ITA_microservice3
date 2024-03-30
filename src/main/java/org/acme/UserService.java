package org.acme;


import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.User;
import org.acme.repository.UserRepository;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    @Channel("Users") // Ime kanala na rabbitu
    Emitter<String> userEmitter; // S tem pošiljam sporočila na rabbit
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);//za logiranje

    @POST
    @Path("/add")
    public Uni<User> createUser(User user) {
        String hashedPassword = hashPassword(user.getPassword());
        user.setPassword(hashedPassword);
        Uni<User> persistedUser = userRepository.persist(user);
        String message = "Metoda createUser klicana. Podrobnosti uporabnika:" + user.toString();
        userEmitter.send(message);
        LOGGER.info("Uporabnik vstavljen");
        return persistedUser;
    }

    // Method to hash a password using BCrypt
    private String hashPassword(String password) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(password, salt);
    }
    @GET
    @Path("/all")
    public Multi<User> getAllUsers() {
        String message = "Metoda getAllUsers klicana.";
        userEmitter.send(message);
        LOGGER.info("Metoda getAllUsers klicana.");
        return Multi.createFrom().publisher(userRepository.findAll().stream());
    }

    @GET
    @Path("/{id}")
    public Uni<User> getUser(@PathParam("id") String id) {
        ObjectId objectId = new ObjectId(id);
        return userRepository.findById(objectId)
                .onItem().transformToUni(user -> {
                    if (user == null) {
                        NotFoundException notFoundException = new NotFoundException("Uporabnik ni bil najden");
                        LOGGER.error("Uporabnik ni bil najden");
                        throw notFoundException;
                    } else {
                        String message = "Metoda getUser klicana za uporabnika z ID: " + id;
                        userEmitter.send(message);
                        LOGGER.info("Uporabnik z id: " + id + " pridobljen");
                        return Uni.createFrom().item(user);
                    }
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Object> deleteUserById(@PathParam("id") String id) {
        ObjectId objectId = new ObjectId(id);
        String message = "Uporabnik z ID: " + id + " je bil uspesno izbrisan.";
        return userRepository.deleteById(objectId)
                .onItem().transform(ignored -> {
                    userEmitter.send(message);
                    LOGGER.info("Uporabnik izbrisan");
                    return null;
                })
                .replaceWith("Uporabnik izbrisan");
    }

    @PUT
    @Path("/{id}")
    public Uni<Object> updateUser(@PathParam("id") String id, User updatedUser) {
        ObjectId objectId = new ObjectId(id);

        return userRepository.findById(objectId)
                .onItem().ifNotNull().transformToUni(existingUser -> {
                    existingUser.setEmail(updatedUser.getEmail());
                    String hashedPassword = hashPassword(updatedUser.getPassword());
                    existingUser.setPassword(hashedPassword);
                    existingUser.setRentalId(updatedUser.getRentalId());
                    return userRepository.update(existingUser)
                            .onItem().transform(ignored -> {
                                String message = "Uporabnik z ID: " + id + " je bil uspesno posodobljen.";
                                userEmitter.send(message);
                                LOGGER.info("Uporabnik posodobljen");
                                return updatedUser;
                            });
                })
                .onItem().ifNull().continueWith(() -> {
                    String message = "Uporabnik z ID: " + id + " ne obstaja.";
                    userEmitter.send(message);
                    LOGGER.warn(message);
                    return null;
                })
                .replaceWith("Uporabnik posodobljen");
    }
}



package org.acme;


import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.smallrye.mutiny.Multi;
import org.acme.model.User;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import io.smallrye.mutiny.Uni;
import org.acme.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    @Channel("Users")
    Emitter<String> userEmitter;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRentalId("12345");
        when(userRepository.persist(any(User.class))).thenReturn(Uni.createFrom().item(user));
        Uni<User> responseUni = userService.createUser(user);
        verify(userRepository, times(1)).persist(user);
        String expectedMessage = "Metoda createUser klicana. Podrobnosti uporabnika:" + user;
        verify(userEmitter, times(1)).send(expectedMessage);
        responseUni.subscribe().with(createdUser -> {
            assertEquals("test@example.com", createdUser.getEmail());
            assertEquals("password", createdUser.getPassword());
            assertEquals("12345", createdUser.getRentalId());
        });
    }

    @Test
    void testGetAllUsers() {
        String expectedMessage = "Metoda getAllUsers klicana.";
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setRentalId("rentalId1");
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setRentalId("rentalId2");
        ReactivePanacheQuery<User> query = mock(ReactivePanacheQuery.class);
        when(userRepository.findAll()).thenReturn(query);
        Multi<User> multi = Multi.createFrom().items(user1, user2);
        when(query.stream()).thenReturn(multi);
        Multi<User> resultMulti = userService.getAllUsers();
        List<User> users = resultMulti.collect().asList().await().indefinitely();
        assertEquals(2, users.size()); // Assert that 2 users were emitted
        assertEquals("user1@example.com", users.get(0).getEmail());
        assertEquals("password1", users.get(0).getPassword());
        assertEquals("rentalId1", users.get(0).getRentalId());
        assertEquals("user2@example.com", users.get(1).getEmail());
        assertEquals("password2", users.get(1).getPassword());
        assertEquals("rentalId2", users.get(1).getRentalId());
        verify(userEmitter, times(1)).send(expectedMessage);
    }

    @Test
    void testGetUser() {
        String id = "66053cbae7de0f4c82c51f43";
        ObjectId objectId = new ObjectId(id);
        User expectedUser = new User();
        when(userRepository.findById(objectId)).thenReturn(Uni.createFrom().item(expectedUser));
        Uni<User> userUni = userService.getUser(id);
        User retrievedUser = userUni.await().indefinitely();
        assertEquals(expectedUser, retrievedUser);
        verify(userRepository, times(1)).findById(objectId);
        String expectedMessage = "Metoda getUser klicana za uporabnika z ID: " + id;
        verify(userEmitter, timeout(1000).times(1)).send(expectedMessage);
    }

    @Test
    void testDeleteUserById() {
        String id = "66053cbae7de0f4c82c51f43";
        ObjectId objectId = new ObjectId(id);
        String expectedMessage = "Uporabnik z ID: " + id + " je bil uspesno izbrisan.";
        when(userRepository.deleteById(objectId)).thenReturn(Uni.createFrom().nullItem());
        Uni<Object> resultUni = userService.deleteUserById(id);
        assertEquals("Uporabnik izbrisan", resultUni.await().indefinitely());
        verify(userRepository, times(1)).deleteById(objectId);// preverim, če je bil klican userRepository.deleteById() z ustreznim argumentom
        verify(userEmitter, times(1)).send(expectedMessage);//preverim, če se je sporočilo poslalo na sporočilnega posrednika
    }

    @Test
    void testUpdateUser() {
        String id = "66053cbae7de0f4c82c51f43";
        ObjectId objectId = new ObjectId(id);
        User updatedUser = new User(); // podatki uporabnika
        updatedUser.setEmail("newEmail@example.com");
        updatedUser.setPassword("newPassword");
        updatedUser.setRentalId("newRentalId");
        String expectedMessageUpdated = "Uporabnik z ID: " + id + " je bil uspesno posodobljen.";
        when(userRepository.findById(objectId)).thenReturn(Uni.createFrom().item(new User()));// Ponarejanje obnašanja userRepository.findById()
        when(userRepository.update(any(User.class))).thenReturn(Uni.createFrom().item(updatedUser));// Ponarejanje obnašanja userRepository.update()
        Uni<Object> resultUni = userService.updateUser(id, updatedUser);
        assertEquals("Uporabnik posodobljen", resultUni.await().indefinitely());
        verify(userRepository, times(1)).findById(objectId);// preverim, če je bil klican userRepository.findById() z ustreznim argumentom
        verify(userRepository, times(1)).update(any(User.class));//tukaj enako samo za userRepository.update
        verify(userEmitter, times(1)).send(expectedMessageUpdated);
    }
}








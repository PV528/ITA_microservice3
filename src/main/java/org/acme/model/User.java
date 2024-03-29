package org.acme.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

@MongoEntity(database="users", collection = "users")
public class User extends PanacheMongoEntity {
    private String email;
    private String password;
    private String rentalId;
    public User() {
    }

    @Override
    public String toString() {
        return "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"rentalId\": \"" + rentalId + "\"}";
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    // Getter and Setter methods for password
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    // Getter and Setter methods for rentalId
    public String getRentalId() {
        return rentalId;
    }
    public void setRentalId(String rentalId) {
        this.rentalId = rentalId;
    }
}


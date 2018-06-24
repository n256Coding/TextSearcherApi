package com.n256coding.Controllers;

import com.n256coding.Common.Environments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.User;
import com.n256coding.Interfaces.DatabaseConnection;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user")
public class UserController {
    private DatabaseConnection db = new MongoDbConnection(Environments.MONGO_DB_HOSTNAME, Environments.MONGO_DB_PORT);

    @PostMapping(consumes = "application/json")
    public void addUser(@RequestBody User user) {
        db.addUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return db.getAllUsers();
    }
}

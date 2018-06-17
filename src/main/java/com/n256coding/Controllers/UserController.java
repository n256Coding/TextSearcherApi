package com.n256coding.Controllers;

import com.n256coding.Common.Enviorenments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.User;
import com.n256coding.Interfaces.DatabaseConnection;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user")
public class UserController {
    private DatabaseConnection db = new MongoDbConnection(Enviorenments.MONGO_DB_HOSTNAME);

    @PostMapping(consumes = "application/json")
    public void addUser(@RequestBody User user) {
        db.addUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return db.getAllUsers();
    }
}

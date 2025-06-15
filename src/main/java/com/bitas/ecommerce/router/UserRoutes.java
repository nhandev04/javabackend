package com.bitas.ecommerce.router;

import com.bitas.ecommerce.controller.UserController;
import com.bitas.ecommerce.repository.UserRepository;
import com.bitas.ecommerce.service.UserService;
import java.util.ArrayList;
import java.util.List;

public class UserRoutes {
    private static final UserRepository userRepository = new UserRepository();
    private static final UserService userService = new UserService(userRepository);
    private static final UserController userController = new UserController(userService);

    public static List<Router.Route> getRoutes() {
        List<Router.Route> routes = new ArrayList<>();
        routes.add(new Router.Route("GET", "/users", (path, body, headers) -> userController.getAllUsers()));
        routes.add(new Router.Route("GET", "/users/:id", (path, body, headers) -> userController.getUser(body)));
        routes.add(new Router.Route("POST", "/users", (path, body, headers) -> userController.createUser(body)));
        routes.add(new Router.Route("PUT", "/users/:id", (path, body, headers) -> {
            String id = path.split("/")[2];
            return userController.updateUser(id, body);
        }));
        routes.add(new Router.Route("DELETE", "/users/:id", (path, body, headers) -> {
            String id = path.split("/")[2];
            return userController.deleteUser(id);
        }));
        routes.add(new Router.Route("POST", "/users/authenticate", (path, body, headers) -> userController.authenticateUser(body)));
        return routes;
    }
}

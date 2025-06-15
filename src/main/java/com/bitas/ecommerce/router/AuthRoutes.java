package com.bitas.ecommerce.router;

import com.bitas.ecommerce.controller.AuthController;
import com.bitas.ecommerce.repository.UserRepository;
import com.bitas.ecommerce.service.UserService;
import com.bitas.ecommerce.service.AuthService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthRoutes {
    private static final UserRepository userRepository = new UserRepository();
    private static final UserService userService = new UserService(userRepository);
    private static final AuthService authService = new AuthService(userService);
    private static final AuthController authController = new AuthController(authService);

    public static List<Router.Route> getRoutes() {
        List<Router.Route> routes = new ArrayList<>();
        routes.add(new Router.Route("POST", "/auth/login", (path, body, headers) -> authController.login(body)));
        routes.add(new Router.Route("GET", "/auth/me", (path, body, headers) -> authController.getMe(headers)));
        routes.add(new Router.Route("POST", "/auth/logout", (path, body, headers) -> authController.logout(headers)));
        return routes;
    }
}

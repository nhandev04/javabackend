package com.bitas.ecommerce.router;

import com.bitas.ecommerce.controller.ProductController;
import com.bitas.ecommerce.repository.ProductRepository;
import com.bitas.ecommerce.service.ProductService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductRoutes {
    private static final ProductRepository productRepository = new ProductRepository();
    private static final ProductService productService = new ProductService(productRepository);
    private static final ProductController productController = new ProductController(productService);

    public static List<Router.Route> getRoutes() {
        List<Router.Route> routes = new ArrayList<>();
        routes.add(new Router.Route("GET", "/products", (path, body, headers) -> productController.getAllProducts()));
        routes.add(new Router.Route("GET", "/products/:id", (path, body, headers) -> productController.getProduct(body)));
        routes.add(new Router.Route("POST", "/products", (path, body, headers) -> productController.createProduct(body)));
        routes.add(new Router.Route("PUT", "/products/:id", (path, body, headers) -> {
            String id = path.split("/")[2];
            return productController.updateProduct(id, body);
        }));
        routes.add(new Router.Route("DELETE", "/products/:id", (path, body, headers) -> {
            String id = path.split("/")[2];
            return productController.deleteProduct(id);
        }));
        routes.add(new Router.Route("PATCH", "/products/:id/stock", (path, body, headers) -> {
            String id = path.split("/")[2];
            return productController.updateStockQuantity(id, body);
        }));
        routes.add(new Router.Route("PATCH", "/products/:id/price", (path, body, headers) -> {
            String id = path.split("/")[2];
            return productController.updatePrice(id, body);
        }));
        routes.add(new Router.Route("GET", "/products/category/:category", (path, body, headers) -> {
            String[] parts = path.split("/");
            String category = parts.length > 3 ? parts[3] : "";
            return productController.getProductsByCategory(category);
        }));
        return routes;
    }
}

package com.ecommerce.catalogservice.controller;

import com.ecommerce.catalogservice.model.Catalog;
import com.ecommerce.catalogservice.model.CatalogDTO;
import com.ecommerce.catalogservice.model.ProductDTO;
import com.ecommerce.catalogservice.model.CatalogItem;
import com.ecommerce.catalogservice.repository.CatalogRepository;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/")
public class HomeController {
    @Autowired
    private Environment env;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CatalogRepository catalogRepository;

    @RequestMapping("/")
    public String home() {
        // This is useful for debugging
        // When having multiple instance of gallery service running at different ports.
        // We load balance among them, and display which instance received the request.
        return "Hello from Catalog Service running at port: " + env.getProperty("local.server.port");
    }

    @GetMapping("/catalogs")
    public List<Catalog> getCatalog() {
        return catalogRepository.findAll();
    }

    //@HystrixCommand(fallbackMethod = "fallback")
    //poner escalera de llamadas a todos los servicios
    @GetMapping("/catalogs/{id}")
    public CatalogDTO getCatalog(@PathVariable final String id) {
        Optional<Catalog> catalog = catalogRepository.findById(id);
        CatalogDTO result = new CatalogDTO(catalog.get().getId());
        List<ProductDTO> productDTOs = new ArrayList<>();

        List<CatalogItem> ids = catalog.get().getProductIdentifiers();
        List<ProductDTO> products = new ArrayList<ProductDTO>();
        for (CatalogItem productIdentifier: ids) {
            System.out.println("http://product-service/products/" + productIdentifier.getProductId());
            ResponseEntity<ProductDTO> res = restTemplate.exchange("http://product-service/products/" + productIdentifier.getProductId(),
                    HttpMethod.GET, null, new ParameterizedTypeReference<ProductDTO>() {
                    });
            ProductDTO product = res.getBody();
            productDTOs.add(product);
        }
        result.setProductDTOs(productDTOs);
        return result;
    }

    @PostMapping("/catalogs")
    public CatalogDTO createCatalog() {
        Catalog catalog = new Catalog();
        catalogRepository.save(catalog);
        return new CatalogDTO(catalog.getId(), new ArrayList<>());
    }

    // a fallback method to be called if failure happened
    public CatalogDTO fallback(String catalogId, Throwable hystrixCommand) {
        return new CatalogDTO();
    }

    @PutMapping("/catalogs/{id}")
    public ProductDTO addProductToCatalog(@PathVariable final String id, @RequestBody CatalogItem productIdentifier) {
        Optional<Catalog> catalog = catalogRepository.findById(id);
        catalog.get().addProductIdentifier(new CatalogItem(productIdentifier.getProductId()));
        ResponseEntity<ProductDTO> res = restTemplate.exchange("http://product-service/products/" + productIdentifier.getProductId(),
                HttpMethod.GET, null, new ParameterizedTypeReference<ProductDTO>() {
                });
        catalogRepository.save(catalog.get());
        return  res.getBody();
    }

    @GetMapping("/catalogs/{catalogId}/products/{productId}")
    public ProductDTO getCatalogProduct(@PathVariable final String catalogId, @PathVariable final String productId) {
        Optional<Catalog> catalog = catalogRepository.findById(catalogId);
        List<CatalogItem> ids = catalog.get().getProductIdentifiers();
        System.out.println(catalogId);
        List<ProductDTO> products = new ArrayList<ProductDTO>();
        for (CatalogItem productIdentifier: ids) {
            System.out.println("http://product-service/products/" + productIdentifier.getProductId());
            if (productIdentifier.getProductId().equals(productId)) {
                ResponseEntity<ProductDTO> res = restTemplate.exchange("http://product-service/products/" + productIdentifier.getProductId(),
                        HttpMethod.GET, null, new ParameterizedTypeReference<ProductDTO>() {
                        });
                return res.getBody();
            }
        }
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Product not found"
        );
    }

    // -------- Admin Area --------
    // This method should only be accessed by users with role of 'admin'
    @RequestMapping("/admin")
    public String homeAdmin() {
        return "This is the admin area of Catalog service running at port: " + env.getProperty("local.server.port");
    }
}

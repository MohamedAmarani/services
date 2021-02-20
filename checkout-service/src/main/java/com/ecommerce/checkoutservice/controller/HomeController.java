package com.ecommerce.checkoutservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("")
public class HomeController {
    @Autowired
    private Environment env;

    @Autowired
    private RestTemplate restTemplate;

    //@Autowired
    //private DeliveryRepository deliveryRepository;

    /*@RequestMapping("/")
    public String home() {
        // This is useful for debugging
        // When having multiple instance of gallery service running at different ports.
        // We load balance among them, and display which instance received the request.
        return "Hello from Checkout Service running at port: " + env.getProperty("local.server.port");
    }*/

    //@HystrixCommand(fallbackMethod = "fallback")
    @GetMapping("/")
    public void getDelivery(@PathVariable final String id) {
        return deliveryRepository.findById(id).get();
    }

    // a fallback method to be called if failure happened
    public List<Delivery> fallback(String catalogId, Throwable hystrixCommand) {
        return new ArrayList<>();
    }

    // -------- Admin Area --------
    // This method should only be accessed by users with role of 'admin'
    // We'll add the logic of role based auth later
    @RequestMapping("/admin")
    public String homeAdmin() {
        return "This is the admin area of Checkout service running at port: " + env.getProperty("local.server.port");
    }
}

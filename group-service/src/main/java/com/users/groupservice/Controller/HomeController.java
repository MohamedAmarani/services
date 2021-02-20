package com.users.groupservice.Controller;

import com.users.groupservice.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/")
public class HomeController {
    @Autowired
    private Environment env;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/")
    public String home() {
        // This is useful for debugging
        // When having multiple instance of gallery service running at different ports.
        // We load balance among them, and display which instance received the request.
        return "Hello from Group Service running at port: " + env.getProperty("local.server.port");
    }


    @RequestMapping("/{id}")
    public Group getGallery(@PathVariable final int id) {
        // create gallery object
        Group group = new Group();
        group.setId(id);
        // get list of available images
        // @SuppressWarnings("unchecked")    // we'll throw an exception from image service to simulate a failure
        List<Object> accounts = restTemplate.getForObject("http://account-service/accounts/", List.class);
        group.setAccounts(accounts);

        return group;
    }

    // a fallback method to be called if failure happened
    public Group fallback(int groupId, Throwable hystrixCommand) {
        return new Group(groupId);
    }

    // -------- Admin Area --------
    // This method should only be accessed by users with role of 'admin'
    // We'll add the logic of role based auth later
    @RequestMapping("/admin")
    public String homeAdmin() {
        return "This is the admin area of Group service running at port: " + env.getProperty("local.server.port");
    }
}
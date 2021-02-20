package com.users.accountservice.Controller;

import com.google.gson.Gson;
import com.users.accountservice.Model.Account;
import com.users.accountservice.repository.UserRepository;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/")
@Service
public class HomeController {
    @Autowired
    private Environment env;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/")
    public String home() {
        // This is useful for debugging
        // When having multiple instance of gallery service running at different ports.
        // We load balance among them, and display which instance received the request.
        return "Hello from Account Service running at port: " + env.getProperty("local.server.port");
    }

    @GetMapping("/accounts")
    public List<Account> getAccounts() {
        List<Account> accounts = userRepository.findAll();
        return accounts;
    }

    @PostMapping("/accounts")
    public Account postAccount(@RequestBody Account account) {

        //encriptar contraseña
        account.setPassword(new BCryptPasswordEncoder().encode(account.getPassword()));
        Account account1 = userRepository.save(account);
        //crear carrito
        JSONObject obj = new JSONObject();
        obj.put("id", account1.getId());
        //nventario por defecto
        obj.put("inventoryId", "602a579546e2bb4b088f721c");
        // set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(obj.toString(), headers);
        restTemplate.exchange("http://cart-service/carts/",
                HttpMethod.POST, entity, new ParameterizedTypeReference<String>() {
                });
        return account1;
    }

    @PatchMapping("/accounts/{accountId}/deliveryAddress")
    public Account changeDeliveryAddress(@PathVariable final String accountId, @RequestBody Map<String, String> myJsonRequest) {
        Optional<Account> account = userRepository.findById(accountId);
        try {
            account.get().setDeliveryAddress(myJsonRequest.get("deliveryAddress"));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Could not change deliveryAddress"
            );
        }
        return userRepository.save(account.get());
    }

    @PatchMapping("/accounts/{accountId}/deposit")
    public Account depositCredit(@PathVariable final String accountId, @RequestBody Map<String, Integer> myJsonRequest) {
        Optional<Account> account = userRepository.findById(accountId);
        try {
            account.get().incrementCredit(myJsonRequest.get("credit"));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Could not add credit"
            );
        }
        return userRepository.save(account.get());
    }

    @PutMapping("/accounts/{accountId}/buy")
    public Account makeBuy(@PathVariable final String accountId, @RequestBody Map<String, Integer> myJsonRequest) {
        Optional<Account> account = userRepository.findById(accountId);
        try {
            account.get().decrementCredit(myJsonRequest.get("totalPrice"));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Not enough credits"
            );
        }
        return userRepository.save(account.get());
    }

    // -------- Admin Area --------
    // This method should only be accessed by users with role of 'admin'
    // We'll add the logic of role based auth later
    @RequestMapping("/admin")
    public String homeAdmin() {
        return "This is the admin area of Gallery service running at port: " + env.getProperty("local.server.port");
    }
}
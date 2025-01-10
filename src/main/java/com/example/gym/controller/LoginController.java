package com.example.gym.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.gym.model.User;
import com.example.gym.repository.UserRepository;

@Controller
public class LoginController {
	
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
	LoginController(UserRepository u) {
		userRepository = u;
	}
	
	@GetMapping("/data")
    public ResponseEntity<String> getData() {
        // Prepare the response body
        String responseBody = "Hello, this is a GET response!";

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
	
	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, Object> loginRequest) {
	    System.out.println(loginRequest);
	    String username = (String) loginRequest.get("username");
	    String pass = (String) loginRequest.get("password");
	    long password = 0;

	    for (int i = 0; i < pass.length(); i++) {
	        int l = pass.charAt(i);
	        password = mul(password, 256);
	        password = add(password, l);
	    }

	    Optional<User> userOptional = userRepository.findByUsername(username);
	    Optional<User> opt = userRepository.findByEmail(username);

	    if (userOptional.isPresent()) {
	        User user = userOptional.get();
	        if (user.getPassword() == (password)) {
	            // Create a response map to include the message and username
	            Map<String, String> response = new HashMap<>();
	            response.put("message", "Login successful");
	            response.put("username", user.getUsername());
	            return new ResponseEntity<>(response, HttpStatus.OK);
	        } else {
	        	Map<String, String> response = new HashMap<>();
	            response.put("message", "Invalid password");
	            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	        }
	    } else if(opt.isPresent()) {
	    	User user = opt.get();
	        if (user.getPassword() == (password)) {
	            // Create a response map to include the message and username
	            Map<String, String> response = new HashMap<>();
	            response.put("message", "Login successful");
	            response.put("username", user.getUsername());
	            return new ResponseEntity<>(response, HttpStatus.OK);
	        } else {
	        	Map<String, String> response = new HashMap<>();
	            response.put("message", "Invalid password");
	            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	        }
	    } else {
	    	Map<String, String> response = new HashMap<>();
            response.put("message", "User not found password");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	    }
	}


	@PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, Object> registerRequest) {
        String username = (String) registerRequest.get("username");
        String email = (String) registerRequest.get("email");
        String pass = (String) registerRequest.get("password");
        long password = 0;
        
        for(int i=0;i<pass.length();i++) {
        	int l = pass.charAt(i);
        	password = mul(password, 256);
        	password = add(password, l);
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return new ResponseEntity<>("Username already exists", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setSecret(0L);

        userRepository.save(user);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }
	
	long mod = (long)1e18;
	long add(long a, long b) {return (((a + mod) % mod + (b + mod) % mod) % mod);}
    long mul(long a, long b) {return ((a % mod * b % mod) % mod);}   
}

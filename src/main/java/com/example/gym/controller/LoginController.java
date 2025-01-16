package com.example.gym.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.gym.model.User;
import com.example.gym.repository.DataCache;
import com.example.gym.repository.UserRepository;

@Controller
public class LoginController {
	
	@Autowired
    private UserRepository userRepository;
	@Autowired
	JavaMailSender mailSender;
	
	@Autowired
	LoginController(UserRepository u) {
		userRepository = u;
	}
	
	@GetMapping("/data")
    public ResponseEntity<String> getData() {
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

	@PostMapping("/otp")
	public ResponseEntity<Map<String, String>> otp(@RequestBody Map<String, Object> registerRequest) {
		System.out.println(registerRequest);
		int otp = Integer.parseInt(((String) registerRequest.get("otppp")).trim());
        String username = ((String) registerRequest.get("username")).trim();
        String email = ((String) registerRequest.get("email")).trim();
        String pass = (String) registerRequest.get("password");
        
        long password = 0;
        
        for(int i=0;i<pass.length();i++) {
        	int l = pass.charAt(i);
        	password = mul(password, 256);
        	password = add(password, l);
        }
        
        if(DataCache.otp.containsKey(email)) {
        	int num = DataCache.otp.get(email);
        	DataCache.otp.remove(email);
        	if(num != otp) {
        		send(email);
        		Map<String, String> response = new HashMap<>();
                response.put("message", "Wrong OTP Entered");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        	}
        	else {
        		User user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(password);
                user.setSecret(0L);
                userRepository.save(user);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Done");
                response.put("username", user.getUsername());
	            return new ResponseEntity<>(response, HttpStatus.OK);
        	}
        }
        else {
    		Map<String, String> response = new HashMap<>();
            response.put("message", "Some issue, Please try Registering Again");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

    }

	@PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, Object> registerRequest) {
        String username = ((String) registerRequest.get("username")).trim();
        String email = ((String) registerRequest.get("email")).trim();
        String pass = (String) registerRequest.get("password");
        long password = 0;
        
        for(int i=0;i<pass.length();i++) {
        	int l = pass.charAt(i);
        	password = mul(password, 256);
        	password = add(password, l);
        }
        
        

        if (userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
        	Map<String, String> response = new HashMap<>();
            response.put("message", "User already exists");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setSecret(0L);
        try {
        	send(email);
        }
        catch(Exception e) {
        	Map<String, String> response = new HashMap<>();
            response.put("message", "invalid email");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP");
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
	public void send(String email) {
		Random rand = new Random();
		int n = 0;
        for(int i=0;i<6;i++) {
        	int num = rand.nextInt(10);
        	if(n == 0 && num == 0) num = 1;
        	n *= 10;
        	n += num;
        }
		DataCache.otp.put(email, n);
        SimpleMailMessage message = new SimpleMailMessage();
    	message.setTo(email);
    	message.setSubject("Welcome to Gym App");
    	message.setText("Confirm your registration to the app with OTP = " + n);
    	mailSender.send(message);
	}
	
	long mod = (long)1e18;
	long add(long a, long b) {return (((a + mod) % mod + (b + mod) % mod) % mod);}
    long mul(long a, long b) {return ((a % mod * b % mod) % mod);}   
}

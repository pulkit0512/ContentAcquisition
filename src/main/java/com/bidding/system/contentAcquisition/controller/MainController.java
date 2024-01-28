package com.bidding.system.contentAcquisition.controller;

import com.bidding.system.contentAcquisition.authentication.AuthenticationService;
import com.bidding.system.contentAcquisition.dto.AuctionData;
import com.bidding.system.contentAcquisition.exception.InvalidRequestException;
import com.bidding.system.contentAcquisition.service.AuctionListingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class MainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    private static final String TOKEN_NOT_VALID = "Token Not Valid";
    private AuctionListingService auctionListingService;
    private AuthenticationService authenticationService;

    @GetMapping("/")
    public String hello() {
        return "Service for acquiring content for auction items.";
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        String modifiedUserName = username.replace('@','_').replace('.','-');
        try {
            String jwtToken = authenticationService.login(modifiedUserName, password);
            return new ResponseEntity<>(jwtToken, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/createAuction")
    public ResponseEntity<String> createAuction(@RequestBody AuctionData auctionData, @RequestHeader("Authorization") String token) {
        try {
            LOGGER.info("Creating auction");
            String userEmail = authenticationService.getUserNameFromValidToken(token);
            if (userEmail == null) {
                LOGGER.warn(TOKEN_NOT_VALID);
                return new ResponseEntity<>(TOKEN_NOT_VALID, HttpStatus.UNAUTHORIZED);
            } else {
                userEmail = userEmail.replace('_', '@').replace('-','.');
                auctionData.setUserEmail(userEmail);
            }
            auctionListingService.enlistAuction(auctionData);
            return new ResponseEntity<>("Auction will be created in few minutes", HttpStatus.ACCEPTED);
        } catch (InvalidRequestException ex) {
            LOGGER.error("Invalid Request: {}", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            LOGGER.error("Unable to create auction with message: {}.", ex.getMessage());
            return new ResponseEntity<>("Unable to create auction, " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Autowired
    public void setAuctionListingService(AuctionListingService auctionListingService) {
        this.auctionListingService = auctionListingService;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}



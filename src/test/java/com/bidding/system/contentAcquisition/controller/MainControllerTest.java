package com.bidding.system.contentAcquisition.controller;

import com.bidding.system.contentAcquisition.authentication.AuthenticationService;
import com.bidding.system.contentAcquisition.dto.AuctionData;
import com.bidding.system.contentAcquisition.exception.InvalidRequestException;
import com.bidding.system.contentAcquisition.service.impl.AuctionListingServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class MainControllerTest {

    private MainController mainController;

    @Mock
    private AuctionListingServiceImpl auctionListingService;

    @Mock
    private AuthenticationService authenticationService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mainController = new MainController();
        mainController.setAuctionListingService(auctionListingService);
        mainController.setAuthenticationService(authenticationService);
    }

    @Test
    public void testHello() {
        String response = mainController.hello();
        Assert.assertEquals("Service for acquiring content for auction items.", response);
    }

    @Test
    public void testSuccessLogin() throws Exception {
        String user = "username@gmail.com";
        String modifiedUser = "username_gmail-com";
        String password = "password";
        Mockito.when(authenticationService.login(modifiedUser, password)).thenReturn("token");
        ResponseEntity<String> response = mainController.login(user, password);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testFailLogin() throws Exception {
        String user = "username@gmail.com";
        String modifiedUser = "username_gmail-com";
        String password = "password";
        Mockito.when(authenticationService.login(modifiedUser, password)).thenThrow(new Exception("INVALID_CREDENTIALS"));
        ResponseEntity<String> response = mainController.login(user, password);
        Assert.assertEquals(401, response.getStatusCode().value());
    }

    @Test
    public void testSuccessCreateAuction() throws InvalidRequestException, IOException {
        AuctionData auctionData = new AuctionData();
        Mockito.when(authenticationService.getUserNameFromValidToken("token")).thenReturn("username_gmail-com");
        Mockito.doNothing().when(auctionListingService).enlistAuction(auctionData);
        ResponseEntity<String> response = mainController.createAuction(auctionData, "token");
        Assert.assertEquals(202, response.getStatusCode().value());
    }

    @Test
    public void testFailureCreateAuctionTokenNotValid() {
        AuctionData auctionData = new AuctionData();
        Mockito.when(authenticationService.getUserNameFromValidToken("token")).thenReturn(null);
        ResponseEntity<String> response = mainController.createAuction(auctionData, "token");
        Assert.assertEquals(401, response.getStatusCode().value());
    }

    @Test
    public void testFailureCreateAuction() throws InvalidRequestException, IOException {
        AuctionData auctionData = new AuctionData();
        Mockito.when(authenticationService.getUserNameFromValidToken("token")).thenReturn("username_gmail-com");
        Mockito.doThrow(new InvalidRequestException("Not a valid request")).when(auctionListingService).enlistAuction(auctionData);
        ResponseEntity<String> response = mainController.createAuction(auctionData, "token");
        Assert.assertEquals(400, response.getStatusCode().value());

        Mockito.doThrow(new Exception("Internal Error")).when(auctionListingService).enlistAuction(auctionData);
        response = mainController.createAuction(auctionData, "token");
        Assert.assertEquals(500, response.getStatusCode().value());
    }
}
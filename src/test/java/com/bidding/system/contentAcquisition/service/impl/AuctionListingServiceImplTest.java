package com.bidding.system.contentAcquisition.service.impl;

import com.bidding.system.contentAcquisition.dto.AuctionData;
import com.bidding.system.contentAcquisition.service.PubSubHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AuctionListingServiceImplTest {
    private AuctionListingServiceImpl auctionListingService;

    @Mock
    private PubSubHelper pubSubHelper;
    private AuctionData auctionData;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        auctionListingService = new AuctionListingServiceImpl();
        auctionData = new AuctionData();
        auctionListingService.setPubSubHelper(pubSubHelper);
        Mockito.doNothing().when(pubSubHelper).publishMessage(auctionData);
    }

    @Test
    public void testEnlistAuction() {
        auctionData.setBasePrice(-1.02);
        try {
            auctionListingService.enlistAuction(auctionData);
        } catch (Exception ex) {
            Assert.assertEquals("Base Price can't be less then 0", ex.getMessage());
        }

        auctionData.setBasePrice(1.02);
        try {
            auctionListingService.enlistAuction(auctionData);
        } catch (Exception ex) {
            Assert.assertEquals("Currency Code is mandatory.", ex.getMessage());
        }

        auctionData.setCurrencyCode("USD");
        try {
            auctionListingService.enlistAuction(auctionData);
        } catch (Exception ex) {
            Assert.assertEquals("Item Category is mandatory", ex.getMessage());
        }

        auctionData.setItemCategory("Car");
        auctionData.setCurrencyCode("XYZ");
        try {
            auctionListingService.enlistAuction(auctionData);
        } catch (Exception ex) {
            Assert.assertEquals("Unknown currency", ex.getMessage());
        }

        auctionData.setItemCategory("Car");
        auctionData.setCurrencyCode("USD");
        auctionData.setExpirationTime("2022-10-20");
        try {
            auctionListingService.enlistAuction(auctionData);
        } catch (Exception ex) {
            Assert.assertEquals("Date parse Exception, please check expiration time for auction valid format is YYYY-MM-DDThh:mm:ss.sss", ex.getMessage());
        }

        auctionData.setExpirationTime("2023-10-20T13:02");
        try {
            auctionListingService.enlistAuction(auctionData);
        } catch (Exception ex) {
            Assert.assertEquals("Auction Expiration can't be in Past", ex.getMessage());
        }

        auctionData.setExpirationTime("2024-10-20T13:02");
        try {
            auctionListingService.enlistAuction(auctionData);
        } catch (Exception ex) {
            Assert.assertEquals("Auction Expiration can't be in Past", ex.getMessage());
        }
    }
}
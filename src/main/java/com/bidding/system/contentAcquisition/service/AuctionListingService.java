package com.bidding.system.contentAcquisition.service;

import com.bidding.system.contentAcquisition.dto.AuctionData;

public interface AuctionListingService {

    void enlistAuction(AuctionData auctionData) throws Exception;
}

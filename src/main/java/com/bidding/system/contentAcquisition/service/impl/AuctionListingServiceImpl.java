package com.bidding.system.contentAcquisition.service.impl;

import com.bidding.system.contentAcquisition.dto.AuctionData;
import com.bidding.system.contentAcquisition.exception.InvalidRequestException;
import com.bidding.system.contentAcquisition.service.AuctionListingService;
import com.bidding.system.contentAcquisition.service.PubSubHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.money.UnknownCurrencyException;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Component
public class AuctionListingServiceImpl implements AuctionListingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionListingServiceImpl.class);
    private PubSubHelper pubSubHelper;
    private static final ExchangeRateProvider exchangeRateProvider = MonetaryConversions.getExchangeRateProvider("ECB");
    @Override
    public void enlistAuction(AuctionData auctionData) throws IOException, InvalidRequestException {
        try {
            if (auctionData.getBasePrice() < 0) {
                throw new InvalidRequestException("Base Price can't be less then 0");
            }
            if (StringUtils.isEmpty(auctionData.getCurrencyCode())) {
                throw new InvalidRequestException("Currency Code is mandatory.");
            }
            if (StringUtils.isEmpty(auctionData.getItemCategory())) {
                throw new InvalidRequestException("Item Category is mandatory");
            }

            updateAuctionData(auctionData);
            if (auctionData.getExpirationInSeconds() < Instant.now().getEpochSecond()) {
                throw new InvalidRequestException("Auction Expiration can't be in Past");
            }

            pubSubHelper.publishMessage(auctionData);
        } catch (UnknownCurrencyException ex) {
            throw new InvalidRequestException("Unknown currency");
        } catch (DateTimeParseException ex) {
            throw new InvalidRequestException("Date parse Exception, please check expiration time for auction");
        }
    }

    private void updateAuctionData(AuctionData auctionData) {
        LOGGER.info("Updating auction data.");

        ExchangeRate rate = exchangeRateProvider.getExchangeRate(auctionData.getCurrencyCode().toUpperCase(), "USD");
        double convertedBasePrice = auctionData.getBasePrice() * rate.getFactor().doubleValueExact();
        auctionData.setConvertedBasePrice(convertedBasePrice);

        ZonedDateTime zonedDateTime = LocalDateTime.parse(auctionData.getExpirationTime()).atZone(ZoneId.of("America/Chicago"));
        auctionData.setConvertedExpirationTime(zonedDateTime.toString());
        auctionData.setExpirationInSeconds(zonedDateTime.toEpochSecond());
        LOGGER.info("Auction data updated.");
    }

    @Autowired
    public void setPubSubHelper(PubSubHelper pubSubHelper) {
        this.pubSubHelper = pubSubHelper;
    }
}

package com.bidding.system.contentAcquisition.service;

import com.bidding.system.contentAcquisition.dto.AuctionData;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PubSubHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(PubSubHelper.class);

    private static final Gson gson = new Gson();

    @Value("${projectId}")
    private String projectId;

    @Value("${pubsub.topicId}")
    private String topicId;

    private Publisher publisher = null;

    public void publishMessage(AuctionData auctionData) throws IOException {
        LOGGER.info("Inside pubsub helper, publish message method");

        if (publisher == null) {
            TopicName topicName = TopicName.of(projectId, topicId);
            publisher = Publisher.newBuilder(topicName).build();
        }


        String jsonData = gson.toJson(auctionData);
        LOGGER.info(jsonData);

        ByteString data = ByteString.copyFromUtf8(jsonData);
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

        publisher.publish(pubsubMessage);
        LOGGER.info("Message Published.");
    }

}

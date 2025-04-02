// Load the AWS SDK for Node.js
var AWS = require("aws-sdk");

const { SQS } = require("@aws-sdk/client-sqs");

// Set the region
// JS SDK v3 does not support global configuration.
// Codemod has attempted to pass values to each service client in this file.
// You may need to update clients outside of this file, if they use global config.
AWS.config.update({ region: "us-east-1" });

// Create an SQS service object
var sqs = new SQS({
  region: "us-east-1",
});

const object = {
  id: "123456789",
  name: "productManagement",
  description: "This is a test message",
  price: 100,
  quantity: 10,
}

var params = {
  // Remove DelaySeconds parameter and value for FIFO queues
  DelaySeconds: 10,
  MessageAttributes: {
    Title: {
      DataType: "String",
      StringValue: "Test",
    },
    Author: {
      DataType: "String",
      StringValue: "productManagement",
    },
    WeeksOn: {
      DataType: "Number",
      StringValue: "6",
    },
  },
  MessageBody:
  JSON.stringify(object),
  QueueUrl: "https://sqs.us-east-1.amazonaws.com/450559935187/BootcAppQueue",
};

sqs.sendMessage(params, function (err, data) {
  if (err) {
    console.log("Error", err);
  } else {
    console.log("Success", data.MessageId);
  }
});

const express = require("express");
const app = express();
const fs = require("fs");
const WebSocket = require("ws");

app.use(express.static(__dirname + '/build'));

const server = app.listen(8080, function() {
  const host = server.address().address;
  const port = server.address().port;
  console.log("Test server app listening at http://%s:%s", host, port);
});

app.get("/messages", (req, res) => {
  return res.send(readFile("messages.json"));
});

app.delete("/messages", () => {
  console.log("I should clear all messages");
});

app.post("/messages", (req, res) => {
  let body = "";
  req.on("data", chunk => {
    body += chunk.toString();
  });
  req.on("end", () => {
    console.log("Hey I received a message: ", body);
    res.end('{"result": "success"}');
  });
});

//initialize the WebSocket server instance
const wss = new WebSocket.Server({ server });

wss.on('connection', ws => {

    //connection is up, let's add a simple simple event
    ws.on('message', message => {

        //log the received message and send it back to the client
        console.log('received: %s', message);
        ws.send(`Hello, you sent -> ${message}`);
    });

    //send immediatly a feedback to the incoming connection
    ws.send('Hi there, I am a WebSocket server');
});

function readFile(fileName) {
  return JSON.parse(fs.readFileSync("./__mocks__/examples/" + fileName));
}
/**
 * Imports
 */


const express = require('express')
const bodyparser = require('body-parser')
const http = require('http');
const bcrypt = require('bcrypt');
const path = require("path");
const axios = require('axios')
const bodyParser = require('body-parser');
const users = [{ id: 1633879548206, username: 'test', email: 'test@test.com', password: '$2b$10$h00XzLXzs9Xlpk987qFXSeODI9SAJx8lSlUBfloCLAK6oVfUOmPv.' }];
var serviceAccount = require("./firebase-admin.json");


var admin = require("firebase-admin");
admin.initializeApp({credential: admin.credential.cert(serviceAccount)})

//First port with an endpoint to read posts from phones

const app2 = express()
app2.use(bodyparser.json())
const port2 = 3000
app2.listen(port2, () =>{
    console.log("Listening on port " + port2)
})  

app2.post('/test', (req, res)=>{
    res.status(200).send("success");
    console.log("Doszedł post z komóry");
    axios.post('http://127.0.0.1:3001/test',{a: 'a'}).then(res => console.log("Post do drugiego OK"))
    res.end()
})

app2.post('/token', (req, res)=>{
    console.log(req.body);
    res.status(200).send("success");
    res.end()
})



const app = express()
app.use(bodyparser.json())

const server = http.createServer(app);

const port = 3001


const notification_options = {
    priority: "high",
    timeToLive: 60 * 60 * 24
  };

app.listen(port, () =>{
    console.log("Listening on port " + port)
})  

app.post('/test', (req, res)=>{
    res.status(200).send("success");
    console.log("Doszedł post do drugiego");
    res.end()
})


app.post('/firebase/notification1', (req, res)=>{    
    const message_notification = {
        notification: {
            title: "Powiadomienie",
            body: "Samsung J5 - reaktorNFC"
        },
        token: "dGcFhhO8StGH37OBfrIn7z:APA91bHKD9fIXI2HL358zP1rhD4gc8VRxNSC77Ps46OVwVJzukrlXWK8DfyA98h9MjW9asTDYh7QHHQnsiP5wCukDWsPs96v_48A0f8AN_KtacruQfRoRy2SVfGJ2lLo0y5-9RidxRnG"
    };
    sendPushNotification(message_notification, req, res)
})

app.post('/firebase/notification2', (req, res)=>{    
    const message_notification = {
        notification: {
            title: "Powiadomienie",
            body: "Huawei"
        },
        token: "fbYjh-1tQgi9-ROiwzyoDa:APA91bEwHewA8opYTpuN0MdmBofhd0HiX5BDbEoAnEmtRB4kSgdl4wBk8ZD9J_HucZohIGU2qN00dTNzhr6GpXcUwXBcVk0DJ0VFhzY2ZWDVsq5mqbPTEa8XHgGQ5TjL_778FqFeuVqN"
    };
    sendPushNotification(message_notification, req, res)
})

app.post('/firebase/notification3', (req, res)=>{    
    const message_notification = {
        notification: {
            title: "Powiadomienie",
            body: "Samsung A - reaktorNFC"
        },
        token: "dsJMMKhTR6uSuTTUpaI4bE:APA91bH43EV3JVmnJsLfms-ztASdhFIgcc-KluZO1B8F0BJs8Wh__q7UrHQV9cNQ8ZMrKZbAxOfF1Kgd4dXFjz07l5d2wY9MyAudzBzwi6KSRHeT_dkvaRrUk2mq9ZemRlE8cd8GTccv"
    };
    sendPushNotification(message_notification, req, res)
})

function sendPushNotification(message, req, res) {
    admin.messaging().send(message)
      .then( response => {

       res.status(200).send("Notification sent successfully"+response)
       res.end()
        
      })
      .catch( error => {
          console.log(error);
      });
}


//WWW part

app.use(bodyParser.urlencoded({extended: false}));
app.use(express.static(path.join(__dirname,'./public')));


app.get('/',(req,res) => {
    res.sendFile(path.join(__dirname,'./public/index.html'));
});


app.post('/register', async (req, res) => {
    try{
        let foundUser = users.find((data) => req.body.email === data.email);
        if (!foundUser) {
    
            let hashPassword = await bcrypt.hash(req.body.password, 10);
    
            let newUser = {
                id: Date.now(),
                username: req.body.username,
                email: req.body.email,
                password: hashPassword,
            };
            users.push(newUser);
            console.log('User list', users);
    
            res.send("<div align ='center'><h2>Registration successful.</h2></div><br><br><div align='center'><a href='./login.html'>login</a></div><br><br><div align='center'><a href='./registration.html'>Register another user?</a></div>");
        } else {
            res.send("<div align ='center'><h2>Email already used.</h2></div><br><br><div align='center'><a href='./registration.html'>Register again.</a></div>");
        }
    } catch{
        res.send("Internal server error");
    }
});

app.post('/login', async (req, res) => {
    try{
        let foundUser = users.find((data) => req.body.email === data.email);
        if (foundUser) {
    
            let submittedPass = req.body.password; 
            let storedPass = foundUser.password; 
    
            const passwordMatch = await bcrypt.compare(submittedPass, storedPass);
            if (passwordMatch) {
                let usrname = foundUser.username;
                res.send(`<!DOCTYPE html>
                <html lang = "en">
                <head>
                    <meta charset = "UTF-8">
                    <title> My Form </title>
                    <style>
                        #mylink{
                            font-size: 25px;
                        }
                    </style>
                    <script type="text/javascript">
                        function postToNode() {
                        console.log("posted");
                        var xhr = new XMLHttpRequest();
                        xhr.open("POST", "/firebase/notification1", true);
                        xhr.setRequestHeader('Content-Type', 'application/json');
                        xhr.send();
                        xhr.onload = function() {
                            var data = JSON.parse(this.responseText);
                            console.log(data);
                        };
                    }
                    </script>	
                </head>
                <body align='center'>
                    <div align ='center'>
                        <h2>Login successful.</h2>
                    </div>
                    <br><br><br>
                    <div align ='center'>
                        <button onclick="postToNode()">Send notification</button>
                    </div>
                    <br><br>
                    <div align='center'>
                        <a href='./login.html'>Log out</a>
                    </div>`);
            } else {
            res.send(`<div align ='center'><h2>Invalid email or password.</h2></div><br><br><div align ='center'><a href='./login.html'>Log in again:</a></div>`);
            }
        }
        else {
    
            let fakePass = `$2b$$10$ifgfgfgfgfgfgfggfgfgfggggfgfgfga`;
            await bcrypt.compare(req.body.password, fakePass);
    
            res.send("<div align ='center'><h2>Invalid email or password.</h2></div><br><br><div align='center'><a href='./login.html'>Log in again.<a><div>");
        }
    } catch{
        res.send("Internal server error");
    }
});


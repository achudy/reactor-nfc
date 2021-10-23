/**
 * Imports
 */


const express = require('express')
const bodyparser = require('body-parser')
const http = require('http');
const bcrypt = require('bcrypt');
const path = require("path");
const bodyParser = require('body-parser');
const users = [{ id: 1633879548206, username: 'test', email: 'test@test.com', password: '$2b$10$h00XzLXzs9Xlpk987qFXSeODI9SAJx8lSlUBfloCLAK6oVfUOmPv.' }];
var serviceAccount = require("./firebase-admin.json");


var admin = require("firebase-admin");
admin.initializeApp({credential: admin.credential.cert(serviceAccount)})

const app = express()
app.use(bodyparser.json())

const server = http.createServer(app);

const port = 3000

const notification_options = {
    priority: "high",
    timeToLive: 60 * 60 * 24
  };

app.listen(port, () =>{
    console.log("Listening on port " + port)
})  


app.post('/firebase/notification', (req, res)=>{
    // const  registrationToken = req.body.registrationToken
    // const message = req.body.message
    // const options =  notification_options
    
    const message_notification = {
        notification: {
            title: "xxx",
            body: "yyy"
        },
        //kommar
        //token: "d2ynlnfrtky6ppxjcwc3ce:APA91bH7Bmwv-8R97Az3xY3pmuSydT3LS_XamRQONnUsJa6amzNTA9ci89yN75m2BY3vNGlpf2DUERU_SKAju7H58ukA33Bhy0ye53I3KHZtajxnjcddg3-fPvYhq_r1bk3qXKNsylc8"
        token: "fRRdG02kRfyGtV4W31De-d:APA91bF6b3CNG5Zf31OmZf4BZpUDWTeXO1H9Kt15x2kh-pUTHHsqnKFv_9YTOf9tlSVk75XJTwjq1bgK8egi2jbfpNBUKStI7aOL3yDLvZIw_vHACGVIvMTgHLbBJVuUHxTdHQAS0Ulm"
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

app.post('/test', (req, res)=>{
    res.status(200).send("success");
    console.log("test");
    res.end()
})

function postToNode() {
    console.log("posted");
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/firebase/notification", true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send();
    xhr.onload = function() {
        var data = JSON.parse(this.responseText);
        console.log(data);
    };
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
                        xhr.open("POST", "/firebase/notification", true);
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


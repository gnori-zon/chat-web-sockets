'use strict';

var MAIN_HOST = 'http://localhost:8080';
var CHAT_CLASS = 'chat-button';
var currentUsername = null

// choose sign-in or sign-up
var choosePage = document.querySelector("#main-page")
var loginPage = document.querySelector("#login-page")
var registrationPage = document.querySelector("#registration-page")

var chooseLoginForm = document.querySelector('#chooseLoginForm');
var chooseRegistrationForm = document.querySelector('#chooseRegistrationForm');

function chooseLogin(event) {
    choosePage.classList.add('hidden');
    loginPage.classList.remove('hidden');
    event.preventDefault();
}

function chooseRegistration(event) {
    choosePage.classList.add('hidden');
    registrationPage.classList.remove('hidden');
    event.preventDefault();
}

chooseLoginForm.addEventListener('submit', chooseLogin, true);
chooseRegistrationForm.addEventListener('submit', chooseRegistration, true);

//auth and connect
var stompClient = null;
var chatListPage = document.querySelector("#chats-page")

function authRequest(username, password) {
    fetch(MAIN_HOST + "/users:sign-in", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
        },
        body: new URLSearchParams({
            'username': username,
            'password': password
        })
    })
        .then(response => {
            console.log(response.status)
            if (response.redirected) {
                chatListPage.classList.remove('hidden');
                connect();
            }
        })
        .catch(err => console.log(err))
}


function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

function onConnected(options) {
    stompClient.subscribe(('/topic/' + currentUsername + '/chat-rooms'), onChatRoomReceived);

    stompClient.send('/app/chat-rooms:list');

}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red' + error.toString();
}

//sign-in
var loginForm = document.querySelector('#loginForm');
var password = null
var username = null

function login(event) {
    username = document.querySelector('#username-log').value.trim();
    password = document.querySelector('#password-log').value.trim();

    if (username && password) {
        authRequest(username, password)
        currentUsername = username
        username = null
        password = null
        loginPage.classList.add('hidden');
        event.preventDefault();
    }
}

loginForm.addEventListener('submit', login, true)

//sign-up
var registrationForm = document.querySelector('#registrationForm');
var password2 = null
var email = null
var name = null

function registrate(event) {
    username = document.querySelector('#username-reg').value.trim();
    password = document.querySelector('#password-reg-1').value.trim();
    password2 = document.querySelector('#password-reg-2').value.trim()
    email = document.querySelector('#email-reg').value.trim()
    name = document.querySelector('#name-reg').value.trim()

    if (username && password && password === password2 && email && name) {
        let data = {
            username: username,
            password: password,
            email: email,
            name: name
        };

        fetch((MAIN_HOST + '/users:sign-up'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(response => {
            authRequest(data.username, data.password)
            currentUsername = username
            password2 = null;
            password = null;
            username = null;
            email = null;
            name = null;
            registrationPage.classList.add('hidden');
            console.log(response.status)
        })
            .catch(err => console.log(err))

        event.preventDefault();
    }
}

registrationForm.addEventListener('submit', registrate, true)

//chats
var chats = []
var chatArea = document.querySelector('#chatsArea');

var newChatForm = document.querySelector('#newChatForm');
var chatNameInput = document.querySelector('#chatName');
var chatDescriptionInput = document.querySelector('#chatDescription');

function createChat(event) {
    var chatName = chatNameInput.value.trim();
    var chatDescription = chatDescriptionInput.value.trim();
    if (chatName && chatDescription && stompClient) {
        var chatRoomDto = {
            name: chatName,
            description: chatDescription
        };
        stompClient.send("/app/chat-rooms:create", {}, JSON.stringify(chatRoomDto));
        chatNameInput.value = '';
        chatDescriptionInput.value = '';
    }
    event.preventDefault();
}

function onChatRoomReceived(payload) {
    if (payload.body.startsWith('[')) {
        JSON.parse(payload.body).forEach(chat => processOneChat(chat))
    } else {
        processOneChat(JSON.parse(payload.body));
    }

}

var chatPage = document.querySelector("#chat-page")

function onSelectChat(event) {
    var chatId = event.target.id;
    console.log(chatId);
    var messageDto = {
        chatRoomId: chatId,
        date: null,
        fromUser: null
    }
    chatListPage.classList.add('hidden');
    chatPage.classList.remove('hidden');
    stompClient.subscribe(('/topic/' + chatId + '/old/messages'), onMessageReceived);
    stompClient.send(('/app/old/messages'), {}, JSON.stringify(messageDto));
}

function onMessageReceived(payload) {
    if (payload.body.startsWith('[')) {
        JSON.parse(payload.body).forEach(message => processOneMessage(message))
    } else {
        processOneMessage(JSON.parse(payload.body));
    }
}

function processOneMessage(message){

    var messageElement = document.createElement('li');


    messageElement.classList.add('chat-message');

    var avatarElement = document.createElement('i');
    var avatarText = document.createTextNode(message.fromUser[0]);
    avatarElement.appendChild(avatarText);
    avatarElement.style['background-color'] = getAvatarColor(message.fromUser);

    messageElement.appendChild(avatarElement);

    var usernameElement = document.createElement('span');
    var usernameText = document.createTextNode(message.fromUser);
    usernameElement.appendChild(usernameText);
    messageElement.appendChild(usernameElement);


    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.text);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function processOneChat(chatDto) {
    chats.push(chatDto);
    console.log(chatDto)

    var chatElement = document.createElement('li');
    chatArea.classList.add('chat')

    var avatarChatElement = document.createElement('i');
    var avatarChatText = document.createTextNode(chatDto.name[0]);
    avatarChatElement.appendChild(avatarChatText);
    avatarChatElement.style['background-color'] = getAvatarColor(chatDto.name);

    chatElement.appendChild(avatarChatElement);

    var chatNameButton = document.createElement('button');
    chatNameButton.textContent = chatDto.name;
    chatNameButton.id = chatDto.id;
    chatNameButton.type = 'button';
    chatNameButton.classList.add(CHAT_CLASS);

    chatNameButton.onclick = (event) => {
        onSelectChat(event)
    }
    chatElement.appendChild(chatNameButton);

    chatArea.appendChild(chatElement);
    chatArea.scrollTop = chatArea.scrollHeight;
}

newChatForm.addEventListener('submit', createChat, true);


//todo: write impl main logic
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        var chatMessage = {
            fromUser: username,
            text: messageContent
        };
        stompClient.send("/app/chat/message:send", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

messageForm.addEventListener('submit', sendMessage, true)

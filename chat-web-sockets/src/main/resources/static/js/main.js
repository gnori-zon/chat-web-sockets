'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');

var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');

var newChatForm = document.querySelector('#newChatForm');
var chatNameInput = document.querySelector('#chatName');
var chatDescriptionInput = document.querySelector('#chatDescription');
var chatArea = document.querySelector('#chatArea');

var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}


function onConnected() {
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe(('/topic/'+ username +'/chat-rooms'), onChatRoomReceived);
    //
    stompClient.send("/app/chat/user:add", {}, JSON.stringify({fromUser: username, text: 'hello'}))
    //put username and get all chats
    stompClient.send('/app/chat-rooms:list');

    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


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

function processOneChat(chatDto) {
    var chatElement = document.createElement('li');
    chatArea.classList.add('chat')

    var avatarChatElement = document.createElement('i');
    var avatarChatText = document.createTextNode(chatDto.name[0]);
    avatarChatElement.appendChild(avatarChatText);
    avatarChatElement.style['background-color'] = getAvatarColor(chatDto.name);

    chatElement.appendChild(avatarChatElement);

    var nameTextChatElement = document.createElement('p');
    var nameChat = document.createTextNode(chatDto.name);
    nameTextChatElement.appendChild(nameChat);

    chatElement.appendChild(nameTextChatElement);

    var descriptionTextChatElement = document.createElement('p');
    var descriptionChat = document.createTextNode(chatDto.description);
    descriptionTextChatElement.appendChild(descriptionChat);

    chatElement.appendChild(descriptionTextChatElement);

    chatArea.appendChild(chatElement);
    chatArea.scrollTop = chatArea.scrollHeight;
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

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


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)
newChatForm.addEventListener('submit', createChat, true)
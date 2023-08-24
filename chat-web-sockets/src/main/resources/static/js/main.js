'use strict';
// https://stomp-js.github.io/stomp-websocket/codo/class/Client.html
const MAIN_HOST = 'http://localhost:8080';
const CHAT_CLASS_NAME = 'chat-name';
const CHAT_CLASS_SETTINGS = 'chat-settings';
const CHAT_ID_NAME_PREFIX = 'chat-name_';
const CHAT_ID_SETTINGS_PREFIX = 'chat-settings_';

const MESSAGE_ID_EDIT_PREFIX = 'edit-message_'
const MESSAGE_ID_DELETE_PREFIX = 'delete-message_'
var currentUsername = null

var oldMessageSubscription = null
var messageSubscription = null
var updateMessageSubscription = null

var chatRoomsSubscribtion = null
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
                currentUsername = username
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
    chatRoomsSubscribtion = stompClient.subscribe(('/topic/' + currentUsername + '/chat-rooms'), onChatRoomReceived);

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
        document.querySelector('#username-log').value = '';
        document.querySelector('#password-log').value = '';
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
            document.querySelector('#username-reg').value = '';
            document.querySelector('#password-reg-1').value = '';
            document.querySelector('#password-reg-2').value = '';
            document.querySelector('#email-reg').value = '';
            document.querySelector('#name-reg').value = '';
            registrationPage.classList.add('hidden');
            console.log(response.status)
        })
            .catch(err => console.log(err))

        event.preventDefault();
    }
}

registrationForm.addEventListener('submit', registrate, true)

//chats
var chats = new Map()
var chatArea = document.querySelector('#chatsArea');

var newChatForm = document.querySelector('#newChatForm');
var chatNameInput = document.querySelector('#newChatName');
var chatDescriptionInput = document.querySelector('#newChatDescription');

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

newChatForm.addEventListener('submit', createChat, true);

function onChatRoomReceived(payload) {
    if (payload.body.startsWith('[')) {
        JSON.parse(payload.body).forEach(chat => processOneChat(chat))
    } else {
        processOneChat(JSON.parse(payload.body));
    }

}

var chatPage = document.querySelector("#chat-page")
var currentChatId = null

function onSelectChat(event) {

    var newChatId = event.target.id.slice(CHAT_ID_NAME_PREFIX.length);
    if (newChatId !== currentChat) {
        currentChatId = newChatId;
        messageArea.innerHTML = null;

        if (oldMessageSubscription !== null) {
            oldMessageSubscription.unsubscribe();
        }
        if (messageSubscription !== null) {
            messageSubscription.unsubscribe();
        }
        if (updateMessageSubscription != null) {
            updateMessageSubscription.unsubscribe();
        }

        console.log(currentChatId);
        var messageDto = {
            chatRoomId: currentChatId,
            date: null,
            fromUser: null
        }
        chatListPage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        oldMessageSubscription = stompClient.subscribe(('/topic/' + currentChatId + '/old/messages'), onMessageReceived);
        stompClient.send('/app/old/messages', {}, JSON.stringify(messageDto));
        messageSubscription = stompClient.subscribe(('/topic/' + currentChatId + '/messages'), onMessageReceived);
        updateMessageSubscription = stompClient.subscribe(('/topic/' + currentChatId + '/update/messages'), onMessageForDeleteReceived);
    }
}

function onMessageReceived(payload) {
    if (payload.body.startsWith('[')) {
        JSON.parse(payload.body).forEach(message => processOneMessage(message))
    } else {
        processOneMessage(JSON.parse(payload.body));
    }
}

function processOneMessage(message) {
    var messagePk = MESSAGE_ID_EDIT_PREFIX + utf16ToUtf8(JSON.stringify(message.messagePrimaryKey));
    var existMessage = document.querySelector('li:has(#' + messagePk + ')>p');
    if (existMessage) {
        existMessage.textContent = message.text;
    } else {
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

        var deleteMessageButton = document.createElement('button');
        deleteMessageButton.textContent = '🗑️';
        deleteMessageButton.id = MESSAGE_ID_DELETE_PREFIX + JSON.stringify(message.messagePrimaryKey);
        deleteMessageButton.onclick = (event) => {
            onClickDeleteMessage(event)
        };
        messageElement.appendChild(deleteMessageButton);

        var editMessageButton = document.createElement('button');
        editMessageButton.id = MESSAGE_ID_EDIT_PREFIX + JSON.stringify(message.messagePrimaryKey);
        editMessageButton.textContent = '📝';
        editMessageButton.onclick = (event) => {
            onClickEditMessage(event)
        };
        messageElement.appendChild(editMessageButton);


        messageArea.appendChild(messageElement);
        messageArea.scrollTop = messageArea.scrollHeight;
    }
}

// send message
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');

function sendMessage(event) {
    var messageContent = messageInput.value.trim();

    if (!selectedMessageId && messageContent && stompClient) {
        var chatMessage = {
            chatRoomId: currentChatId,
            date: new Date(),
            fromUser: currentUsername,
            text: messageContent
        };
        stompClient.send("/app/messages:send", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

messageForm.addEventListener('submit', sendMessage, true)

function onMessageForDeleteReceived(payload) {
    var messageDto = JSON.parse(payload.body);
    var messagePk = MESSAGE_ID_DELETE_PREFIX + utf16ToUtf8(JSON.stringify(messageDto.messagePrimaryKey));
    var deleteButton = document.querySelector('#' + messagePk);
    var li = deleteButton.parentElement;
    li.parentElement.removeChild(li);
}

function onClickDeleteMessage(event) {
    var messagePk = JSON.parse(event.target.id.slice(MESSAGE_ID_DELETE_PREFIX.length));
    var messagePayload = {
        chatRoomId: messagePk.chatRoomId,
        date: messagePk.date,
        fromUser: messagePk.username
    }
    stompClient.send('/app/messages:delete', {}, JSON.stringify(messagePayload));

}

var confirmEditMessageButton = document.querySelector("#confirm-edit-message")
confirmEditMessageButton.onclick = (event) => {
    onClickConfirmEditMessage(event)
};

var selectedMessageId = null
var oldSelectedMessageText = null

function onClickEditMessage(event) {
    selectedMessageId = JSON.parse(event.target.id.slice(MESSAGE_ID_EDIT_PREFIX.length));
    oldSelectedMessageText = messageInput.value.trim();
    confirmEditMessageButton.classList.remove('hidden')
    var existMessage = document.querySelector('li:has(#' + utf16ToUtf8(event.target.id) + ')>p');
    if (existMessage) {
        messageInput.value = existMessage.textContent
    }
}

function onClickConfirmEditMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        if (messageContent !== oldSelectedMessageText) {
            var chatMessage = {
                chatRoomId: selectedMessageId.chatRoomId,
                date: selectedMessageId.date,
                fromUser: selectedMessageId.username,
                text: messageContent
            };
            stompClient.send("/app/messages:update", {}, JSON.stringify(chatMessage));
        }
        messageInput.value = '';
        selectedMessageId = null;
        confirmEditMessageButton.classList.add('hidden')
    }
}

function processOneChat(chatDto) {
    if (chats.has(chatDto.id)) {
        replaceChat(chatDto);
    } else {
        addChat(chatDto);
    }
}

function replaceChat(chatDto) {
    chats.set(chatDto.id, chatDto);
    console.log(chatDto);

    var nameElement = document.querySelector('#' + CHAT_ID_NAME_PREFIX + chatDto.id);
    nameElement.textContent = chatDto.name;
    var avatarElement = document.querySelector('li:has(#' + CHAT_ID_NAME_PREFIX + chatDto.id + ')>i');
    avatarElement.textContent = chatDto.name[0];
    avatarElement.style['background-color'] = getAvatarColor(chatDto.name);
}

function deleteChat(id) {
    chats.delete(id);
    var nameElement = document.querySelector('#' + CHAT_ID_NAME_PREFIX + id);
    var chat = nameElement.parentElement
    chat.parentElement.removeChild(chat)
}

function addChat(chatDto) {
    chats.set(chatDto.id, chatDto)
    console.log(chatDto)

    var chatElement = document.createElement('li');
    chatArea.classList.add('chats')

    var avatarChatElement = document.createElement('i');
    var avatarChatText = document.createTextNode(chatDto.name[0]);
    avatarChatElement.appendChild(avatarChatText);
    avatarChatElement.style['background-color'] = getAvatarColor(chatDto.name);

    chatElement.appendChild(avatarChatElement);

    var chatNameButton = document.createElement('button');
    chatNameButton.textContent = chatDto.name;
    chatNameButton.id = CHAT_ID_NAME_PREFIX + chatDto.id;
    chatNameButton.type = 'button';
    chatNameButton.classList.add(CHAT_CLASS_NAME);

    chatNameButton.onclick = (event) => {
        onSelectChat(event)
    }
    chatElement.appendChild(chatNameButton);

    var chatSettingsButton = document.createElement('button');
    chatSettingsButton.textContent = '⚙';
    chatSettingsButton.id = CHAT_ID_SETTINGS_PREFIX + chatDto.id;
    chatSettingsButton.type = 'button';
    chatSettingsButton.classList.add(CHAT_CLASS_SETTINGS);

    chatSettingsButton.onclick = (event) => {
        onSelectSettingChat(event)
    }
    chatElement.appendChild(chatSettingsButton);

    chatArea.appendChild(chatElement);
    chatArea.scrollTop = chatArea.scrollHeight;
}

var currentSettingsChatId = null

var chatSettingsPage = document.querySelector("#chat-settings-page")
var chatSettingsId = document.querySelector("#settings-chat-id")
var chatSettingsName = document.querySelector("#settings-chat-name")
var chatSettingsDescription = document.querySelector("#settings-chat-description")
var chatSettingsOwner = document.querySelector("#settings-chat-owner")
var chatSettingsConnectedUsers = document.querySelector("#settings-chat-connectedUser")
var editChatSettingsButton = document.querySelector("#edit-chat-button")
var deleteChatButton = document.querySelector("#delete-chat-button")

var currentChat = null
var countUser = 0

function onSelectSettingChat(event) {
    currentSettingsChatId = event.target.id.slice(CHAT_ID_SETTINGS_PREFIX.length);
    console.log(currentSettingsChatId);

    chatSettingsPage.classList.remove('hidden');
    if (chats.has(currentSettingsChatId)) {
        currentChat = chats.get(currentSettingsChatId);

        if (currentChat.id !== chatSettingsId.textContent) {
            chatSettingsId.textContent = "id: " + currentChat.id
            chatSettingsName.textContent = "name: " + currentChat.name
            chatSettingsDescription.textContent = "description: " + currentChat.description
            chatSettingsOwner.textContent = "owner: " + currentChat.ownerUsername
            if (currentChat.connectedUsers != null) {
                chatSettingsConnectedUsers.innerHTML = null
                var chatElement = document.createElement('li');
                chatElement.textContent = "connected user: ";
                countUser = 0
                chatSettingsConnectedUsers.appendChild(chatElement);
                currentChat.connectedUsers.forEach(user => writeUserToSettings(user))
            }
            if (currentUsername === currentChat.ownerUsername) {
                editChatSettingsButton.classList.remove('hidden');
            } else {
                editChatSettingsButton.classList.add('hidden');
            }
        }
    }
}

function writeUserToSettings(user) {
    var chatElement = document.createElement('li');
    chatElement.textContent = (countUser + 1) + ". " + user.username;
    chatSettingsConnectedUsers.appendChild(chatElement);
}

editChatSettingsButton.onclick = (event) => {
    onClickEditChatSettings(event)
}
deleteChatButton.onclick = (event) => {
    onClickDeleteChat(event)
}

var chatEditSettingsPage = document.querySelector("#chat-edit-settings-page")
var editChatForm = document.querySelector("#editChatForm")
var editChatName = document.querySelector("#editChatName")
var editChatDescription = document.querySelector("#editChatDescription")

function onClickEditChatSettings(event) {
    chatSettingsPage.classList.add('hidden');
    chatEditSettingsPage.classList.remove('hidden');
    editChatName.setAttribute("value", currentChat.name);
    editChatDescription.setAttribute("value", currentChat.description);
}

editChatForm.addEventListener('submit', updateChat, true)

function updateChat(event) {
    var chatName = editChatName.value.trim();
    var chatDescription = editChatDescription.value.trim();
    if (chatName && chatDescription && stompClient) {
        var chatRoomDto = {
            chatRoomId: currentSettingsChatId,
            name: chatName,
            description: chatDescription
        };
        stompClient.send("/app/chat-rooms:update", {}, JSON.stringify(chatRoomDto));
        chatEditSettingsPage.classList.add('hidden');
        editChatName.value = ''
        editChatDescription.value = ''
        currentSettingsChatId = null
    }
    event.preventDefault();
}

function onClickDeleteChat(event) {
    chatSettingsPage.classList.add('hidden');
    deleteChat(currentChat.id)
    var chatRoomDto = {
        chatRoomId: currentSettingsChatId
    };
    stompClient.send("/app/chat-rooms:delete", {}, JSON.stringify(chatRoomDto));
    currentSettingsChatId = null;
}

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

function utf16ToUtf8(rawStr) {

    return rawStr.replaceAll('{', '\\{')
        .replaceAll(':', '\\:')
        .replaceAll('}', '\\}')
        .replaceAll('"', '\\"')
        .replaceAll(',', '\\,')
        .replaceAll('.', '\\.');
}
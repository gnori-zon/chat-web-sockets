'use strict';
// https://stomp-js.github.io/stomp-websocket/codo/class/Client.html
const MAIN_HOST = 'http://localhost:8080';
const CHAT_CLASS_NAME = 'chat-name';
const CHAT_CLASS_SETTINGS = 'chat-settings';
const CHAT_ID_NAME_PREFIX = 'chat-name_';
const CHAT_ID_SETTINGS_PREFIX = 'chat-settings_';

const MESSAGE_ID_EDIT_PREFIX = 'edit-message_'
const MESSAGE_ID_DELETE_PREFIX = 'delete-message_'

const EXCLUDE_BUTTON_PREFIX = 'exlude-user_';
var currentUsername = null

var oldMessageSubscription = null
var messageSubscription = null

var chatRoomsSubscription = null

var usersSubscription = null;
var errorSubscription = null;

var adminSubscription = null

// choose sign-in or sign-up
var choosePage = document.querySelector("#main-page")
var loginPage = document.querySelector("#login-page")
var registrationPage = document.querySelector("#registration-page")

var chooseLoginForm = document.querySelector('#chooseLoginForm');
var chooseRegistrationForm = document.querySelector('#chooseRegistrationForm');

function chooseLogin(event) {
    hideElement(choosePage);
    displayElement(loginPage);

    event.preventDefault();
}

function chooseRegistration(event) {
    hideElement(choosePage);
    displayElement(registrationPage);

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
                displayElement(chatListPage);
                currentUsername = username
                connect();
            } else if (response.status === 401) {
                displayError("Bad authorize credentials");
                displayElement(choosePage);
            }
        })
        .catch(err => console.log(err))
}

function clearPage() {
    hideElement(chatListPage);
    hideElement(newChatPage);
    hideElement(chatPage);
    hideElement(chatSettingsPage);
    hideElement(userSettingsPage);
    hideElement(editUserPage);
    hideElement(chatEditSettingsPage);
    hideElement(registrationPage);
    hideElement(loginPage);

    displayElement(choosePage);
    currentUsername = ''

    userSettingsName.textContent = '';
    userSettingsUsername.textContent = '';
    userSettingsEmail.textContent = '';

    chatSettingsId.textContent = '';
    chatSettingsName.textContent = '';
    chatSettingsDescription.textContent = '';
    chatSettingsOwner.textContent = '';
    chatSettingsConnectedUsers.innerHTML = null;

    editChatName.value = '';
    editChatDescription.value = '';
    editUserName.value = '';
    editUserEmail.value = '';

    chats = new Map()
    chatArea.innerHTML = null;
    messageArea.innerHTML = null;
    currentChatId = null;
    currentChat = null;
    currentUser = null;
    currentUsername = null;
    currentSettingsChatId = null;
}

function logout() {
    fetch(MAIN_HOST + "/logout", {
        method: 'POST'
    })
        .then(response => {
            console.log(response.status)
            if (chatRoomsSubscription) {
                chatRoomsSubscription.unsubscribe();
            }
            if (errorSubscription) {
                errorSubscription.unsubscribe();
            }
            if (adminSubscription) {
                adminSubscription.unsubscribe();
            }
            if (messageSubscription) {
                messageSubscription.unsubscribe();
            }
            if (usersSubscription) {
                usersSubscription.unsubscribe();
            }
            if (oldMessageSubscription) {
                oldMessageSubscription.unsubscribe();
            }
            if (stompClient) {
                stompClient.disconnect();
                stompClient = null;
            }
            clearPage()
        })
        .catch(err => console.log(err))
}

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

function onConnected(options) {

    chatRoomsSubscription = stompClient.subscribe('/topic/' + currentUsername + '/chat-rooms', onChatRoomReceived);
    usersSubscription = stompClient.subscribe('/topic/' + currentUsername + '/users', onUserDataReceived);
    errorSubscription = stompClient.subscribe('/topic/' + currentUsername + '/errors', onBusinessError);

    stompClient.send('/app/users:get');
    stompClient.send('/app/chat-rooms:list');
    displayElement(userSettingsPage);
    displayElement(newChatPage);

}

var errorElement = document.querySelector('#error-message')

function displayError(errorMessage) {
    errorElement.textContent = errorMessage;
    errorElement.style.color = 'red';

    displayElement(errorElement);
    setTimeout(
        () => hideElement(errorElement),
        5500
    );
}

function onError(error) {
    displayError(error.toString())
}

function onBusinessError(payload) {
    displayError(JSON.parse(payload.body).message);
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
        hideElement(loginPage);
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
            hideElement(registrationPage);
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

var newChatPage = document.querySelector('#new-chat-page');
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

function processOneChat(chat) {
    addChat(chat);
}

function onChatRoomReceived(payload) {

    var parsedPayload = JSON.parse(payload.body);

    if (payload.body.startsWith('[')) {
        parsedPayload.forEach(chat => processOneChat(chat))
    } else if ("GET" === parsedPayload.actionType || "CREATE" === parsedPayload.actionType) {
        processOneChat(parsedPayload);
    } else if ("UPDATE" === parsedPayload.actionType) {
        replaceChat(parsedPayload)
    } else if ("DELETE" === parsedPayload.actionType) {
        deleteChat(parsedPayload)
    }
}

var chatPage = document.querySelector("#chat-page")
var closeChatButton = document.querySelector("#close-chat-page-button")
var currentChatId = null

closeChatButton.onclick = (event) => {
    hideElement(chatPage);
    displayElement(newChatPage);
    displayElement(chatListPage);
    if (messageSubscription) {
        messageSubscription.unsubscribe();
    }
    if (oldMessageSubscription) {
        oldMessageSubscription.unsubscribe();
    }
}

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

        console.log(currentChatId);
        var messageDto = {
            chatRoomId: currentChatId,
            date: null,
            fromUser: null
        }
        hideElement(newChatPage);
        hideElement(chatListPage);
        displayElement(chatPage);

        oldMessageSubscription = stompClient.subscribe(('/topic/' + currentChatId + '/old/messages/' + currentUsername), onMessageReceived);
        stompClient.send('/app/old/messages', {}, JSON.stringify(messageDto));
        messageSubscription = stompClient.subscribe(('/topic/' + currentChatId + '/messages'), onMessageReceived);
    }
}

function onMessageReceived(payload) {

    var parsedPayload = JSON.parse(payload.body);

    if (payload.body.startsWith('[')) {
        parsedPayload.forEach(message => processOneMessage(message))
    } else if ("GET" === parsedPayload.actionType || "CREATE" == parsedPayload.actionType) {
        processOneMessage(parsedPayload);
    } else if ("UPDATE" === parsedPayload.actionType) {
        processUpdateMessage(parsedPayload);
    } else if ("DELETE" === parsedPayload.actionType) {
        processDeleteMessage(parsedPayload);
    }


}

function processUpdateMessage(message) {
    var messagePk = MESSAGE_ID_EDIT_PREFIX + utf16ToUtf8(JSON.stringify(message.messagePrimaryKey));
    var existMessage = document.querySelector('li:has(#' + messagePk + ')>p');
    if (existMessage) {
        existMessage.textContent = message.text;
    }
}

function processOneMessage(message) {
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

function processDeleteMessage(messageDto) {
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
    displayElement(confirmEditMessageButton);
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
                fromUser: currentUsername,
                text: messageContent
            };
            stompClient.send("/app/messages:update", {}, JSON.stringify(chatMessage));
        }
        messageInput.value = '';
        selectedMessageId = null;
        hideElement(confirmEditMessageButton);
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

    if (!chatSettingsPage.classList.contains('hidden') && currentSettingsChatId === chatDto.id) {
        displaySettingsChat();
    }
}

function deleteChat(chatDto) {
    var id = chatDto.id
    chats.delete(id);
    if (currentChatId === id) {
        if (oldMessageSubscription) {
            oldMessageSubscription.unsubscribe();
        }
        if (messageSubscription) {
            messageSubscription.unsubscribe();
        }
    }
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
var backToSettingChatPage = document.querySelector("#back-to-chat-settings-page-button")
var closeChatSettingsPageButton = document.querySelector("#close-chat-edit-settings-page-button")
var deleteChatButton = document.querySelector("#delete-chat-button")
var addUserInChatForm = document.querySelector('#addUserInChatForm')
var usernameAddingUserInput = document.querySelector('#usernameAddingUser')

addUserInChatForm.addEventListener('submit', onClickAddUserInChat, true)
var currentChat = null

closeChatSettingsPageButton.onclick = (event) => {
    hideElement(chatSettingsPage);
    displayElement(newChatPage);
}

backToSettingChatPage.onclick = () => {
    displayElement(chatSettingsPage);
    hideElement(chatEditSettingsPage);
}

function onSelectSettingChat(event) {
    hideElement(newChatPage);
    currentSettingsChatId = event.target.id.slice(CHAT_ID_SETTINGS_PREFIX.length);
    console.log(currentSettingsChatId);
    displaySettingsChat();
}

function displaySettingsChat() {
    displayElement(chatSettingsPage);
    if (chats.has(currentSettingsChatId)) {
        currentChat = chats.get(currentSettingsChatId);

        if (currentChat.id !== chatSettingsId.textContent) {
            chatSettingsId.textContent = "id: " + currentChat.id
            chatSettingsName.textContent = "name: " + currentChat.name
            chatSettingsDescription.textContent = "description: " + currentChat.description
            chatSettingsOwner.textContent = "owner: " + currentChat.ownerUsername
            if (currentChat.connectedUsers != null) {
                chatSettingsConnectedUsers.innerHTML = null

                var textElement = document.createElement('p');
                textElement.textContent = "connected user: ";

                chatSettingsConnectedUsers.appendChild(textElement);
                currentChat.connectedUsers.forEach(user => writeUserToSettings(user))
            }
            if (currentUsername === currentChat.ownerUsername) {
                displayElement(editChatSettingsButton);
            } else {
                hideElement(editChatSettingsButton);
            }
        }
    }
}

function writeUserToSettings(user) {
    var userFromChat = document.createElement('li');

    var textElement = document.createElement('p');
    textElement.textContent = user.username;
    userFromChat.appendChild(textElement);

    var excludeUserButton = document.createElement("button");
    excludeUserButton.textContent = '❌';
    excludeUserButton.id = EXCLUDE_BUTTON_PREFIX + user.username;
    excludeUserButton.onclick = (event) => {
        onClickDeleteUser(event)
    };

    userFromChat.appendChild(excludeUserButton);
    chatSettingsConnectedUsers.appendChild(userFromChat);
}

function onClickDeleteUser(event) {
    var excludeUsername = event.target.id.slice(EXCLUDE_BUTTON_PREFIX.length);
    var payload = {
        username: excludeUsername,
        chatRoomId: currentSettingsChatId
    };
    stompClient.send('/app/chat-rooms/users:delete', {}, JSON.stringify(payload));
}

function onClickAddUserInChat(event) {
    var addingUsername = usernameAddingUserInput.value.trim();
    if (addingUsername && currentSettingsChatId != null) {
        var payload = {
            username: addingUsername,
            chatRoomId: currentSettingsChatId
        };
        stompClient.send('/app/chat-rooms/users:add', {}, JSON.stringify(payload))
        usernameAddingUserInput.value = ''
    }
    event.preventDefault();
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
    hideElement(chatSettingsPage);
    displayElement(chatEditSettingsPage);
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
        hideElement(chatEditSettingsPage);
        editChatName.value = ''
        editChatDescription.value = ''
        currentSettingsChatId = null
    }
    event.preventDefault();
}

function onClickDeleteChat(event) {
    hideElement(chatSettingsPage);

    if (currentChat.owner === currentUsername) {
        var payloadChatOnDelete = {
            chatRoomId: currentSettingsChatId
        };
        stompClient.send("/app/chat-rooms:delete", {}, JSON.stringify(payloadChatOnDelete));
    } else {
        var payloadUserOnDelete = {
            username: currentUsername,
            chatRoomId: currentSettingsChatId
        };
        stompClient.send('/app/chat-rooms/users:delete', {}, JSON.stringify(payloadUserOnDelete));
    }
    currentSettingsChatId = null;
}

var userSettingsPage = document.querySelector("#user-settings-page");
var userSettingsUsername = document.querySelector("#settings-user-username");
var userSettingsName = document.querySelector("#settings-user-name");
var userSettingsEmail = document.querySelector("#settings-user-email");
var editUserAccountButton = document.querySelector("#edit-user-button");
var deleteUserAccountButton = document.querySelector("#delete-user-button");

editUserAccountButton.onclick = (event) => {
    onClickEditUserAccount(event)
};
deleteUserAccountButton.onclick = (event) => {
    onClickDeleteUserAccount(event)
};

function onClickDeleteUserAccount(event) {
    stompClient.send("/app/users:delete", {}, {});

    if (chatRoomsSubscription) {
        chatRoomsSubscription.unsubscribe();
    }
    if (errorSubscription) {
        errorSubscription.unsubscribe();
    }
    if (adminSubscription) {
        adminSubscription.unsubscribe();
    }
    if (messageSubscription) {
        messageSubscription.unsubscribe();
    }
    if (usersSubscription) {
        usersSubscription.unsubscribe();
    }
    if (oldMessageSubscription) {
        oldMessageSubscription.unsubscribe();
    }
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
    }
    clearPage();
}

var editUserPage = document.querySelector("#user-edit-page");

var editUserForm = document.querySelector("#editUserForm");
var editUserName = document.querySelector("#editUserName");
var editUserEmail = document.querySelector("#editUserEmail");

var changePasswordForm = document.querySelector("#changePasswordForm");
var userOldPassword = document.querySelector("#change-old-password");
var userNewPassword1 = document.querySelector("#change-new-password1");
var userNewPassword2 = document.querySelector("#change-new-password2");

function onClickEditUserAccount(event) {
    displayElement(editUserPage);
    stompClient.send()
    editUserName.value = currentUser.name;
    editUserEmail.value = currentUser.email;
}

editUserForm.addEventListener('submit', onClickConfirmEditUser, true);
changePasswordForm.addEventListener('submit', onClickConfirmChangePassword, true);

function onClickConfirmEditUser(event) {
    var newName = editUserName.value.trim();
    var newEmail = editUserEmail.value.trim();

    if (newName && newEmail && (currentUser.name !== newEmail || currentUser.email !== name)) {
        var payload = {
            name: newName,
            email: newEmail
        }
        stompClient.send('/app/users:update', {}, JSON.stringify(payload));
        editUserName.value = '';
        editUserEmail.value = '';
        hideElement(editUserPage);
    }
    event.preventDefault();
}

function onClickConfirmChangePassword(event) {
    var oldPass = userOldPassword.value.trim();
    var newPass1 = userNewPassword1.value.trim();
    var newPass2 = userNewPassword2.value.trim();

    if (oldPass && newPass1 && newPass1 === newPass2) {
        var payload = {
            oldPassword: oldPass,
            newPassword: newPass1
        }
        stompClient.send('/app/users:change-password', {}, JSON.stringify(payload));
        userOldPassword.value = '';
        userNewPassword1.value = '';
        userNewPassword2.value = '';
        hideElement(editUserPage);
    }
    event.preventDefault();
}

var currentUser = null;

var logoutButton = document.querySelector('#logout-button')

logoutButton.onclick = (event) => {
    logout();
}

function onUserDataReceived(payload) {
    currentUser = JSON.parse(payload.body);

    console.log(currentUser)
    if ("GET" === currentUser.actionType ||
        "UPDATE" === currentUser.actionType) {

        userSettingsUsername.textContent = 'username: ' + currentUser.username;
        userSettingsName.textContent = 'name: ' + currentUser.name;
        userSettingsEmail.textContent = 'email: ' + currentUser.email;

        console.log("UDPATED")

        if (currentUser.roles.includes('ADMIN')) {
            displayElement(adminPanel)
            if (adminSubscription) {
                adminSubscription.unsubscribe();
            }
            adminSubscription = stompClient.subscribe("/topic/admin/" + currentUsername + "/users", onAdminUserDataReceived);
        } else {
            hideElement(adminPanel)
            if (adminSubscription) {
                adminSubscription.unsubscribe();
            }
        }

    } else if ("DELETE" === currentUser.actionType) {
        onClickDeleteUserAccount(null);
    }
}

function onAdminUserDataReceived(payload) {
    var adminUser = JSON.parse(payload.body);
    console.log(adminUser)

    if ("GET" === adminUser.actionType) {
        writeInputs(adminUser);
    } else if ("CREATE" === adminUser.actionType || "UPDATE" === adminUser.actionType || "DELETE" === adminUser.actionType) {
        displaySuccessNotification("Success: " + adminUser.actionType);
    }
}

function writeInputs(adminUser) {

    if (!adminUpdateUserForm.classList.contains("hidden")) {
        adminUpdateUserNameInput.value = adminUser.name;
        adminUpdateUserEmailInput.value = adminUser.email;
        adminUpdateUserRolesInput.value = adminUser.roles.toString();
    }
}

var successNotificationElement = document.querySelector('#success-notification')

function displaySuccessNotification(notifyMessage) {
    successNotificationElement.textContent = notifyMessage;
    successNotificationElement.style.color = 'green';

    displayElement(successNotificationElement);
    setTimeout(
        () => hideElement(successNotificationElement),
        5500
    );
}

var adminPanel = document.querySelector('#admin-panel')
var adminCreateUserButton = document.querySelector('#admin-create-user-button')
var adminUpdateUserButton = document.querySelector('#admin-update-user-button')
var adminDeleteUserButton = document.querySelector('#admin-delete-user-button')

var adminCreateUserForm = document.querySelector('#admin-create-user-form');
var adminUpdateUserForm = document.querySelector('#admin-update-user-form');
var adminDeleteUserForm = document.querySelector('#admin-delete-user-form');

var adminCreateUserUsernameInput = document.querySelector('#admin-create-username');
var adminCreateUserNameInput = document.querySelector('#admin-create-name');
var adminCreateUserEmailInput = document.querySelector('#admin-create-email');
var adminCreateUserRolesInput = document.querySelector('#admin-create-roles');
var adminCreateUserPasswordInput = document.querySelector('#admin-create-password');
var adminCreateUserReplyPasswordInput = document.querySelector('#admin-create-reply-password');
var confirmAdminCreateUserButton = document.querySelector('#confirm-admin-create-user');
var backToAdminChooseButtonsFromCreateButton = document.querySelector('#back-to-admin-choose-buttons-from-create');

confirmAdminCreateUserButton.onclick = (event) => {
    var username = adminCreateUserUsernameInput.value.trim();
    var name = adminCreateUserNameInput.value.trim();
    var email = adminCreateUserEmailInput.value.trim();
    var roles = adminCreateUserRolesInput.value.trim().split(",").map(role => role.trim());
    var password = adminCreateUserPasswordInput.value.trim();
    var replyPassword = adminCreateUserReplyPasswordInput.value.trim();

    if (username &&
        name &&
        email &&
        roles.length > 0 &&
        password === replyPassword
    ) {
        var payload = {
            name: name,
            email: email,
            roleList: roles.toString(),
            username: username,
            password: password
        }
        stompClient.send('/app/admin/users:create', {}, JSON.stringify(payload));
    }
    adminCreateUserUsernameInput.value = '';
    adminCreateUserNameInput.value = '';
    adminCreateUserEmailInput.value = '';
    adminCreateUserRolesInput.value = '';
    adminCreateUserPasswordInput.value = '';
    adminCreateUserReplyPasswordInput.value = '';
}

adminCreateUserButton.onclick = (event) => {
    displayElement(adminCreateUserForm);
    hideAdminChooseButtons();
}

backToAdminChooseButtonsFromCreateButton.onclick = (event) => {
    hideElement(adminCreateUserForm)
    displayAdminChooseButton()
}

var adminUpdateUserUsernameInput = document.querySelector('#admin-update-username');
var adminUpdateUserNameInput = document.querySelector('#admin-update-name');
var adminUpdateUserEmailInput = document.querySelector('#admin-update-email');
var adminUpdateUserRolesInput = document.querySelector('#admin-update-roles');
var adminSearchUserDataToUpdate = document.querySelector('#admin-update-search-user-data-to-update');
var confirmAdminUpdateUserButton = document.querySelector('#confirm-admin-update-user');
var backToAdminChooseButtonsFromUpdateButton = document.querySelector('#back-to-admin-choose-buttons-from-update');

adminSearchUserDataToUpdate.onclick = (event) => {
    var username = adminUpdateUserUsernameInput.value.trim();

    if (username) {
        var payload = {
            username: username
        }
        stompClient.send('/app/admin/users:get', {}, JSON.stringify(payload));
    }
}

adminUpdateUserButton.onclick = (event) => {
    displayElement(adminUpdateUserForm);
    hideAdminChooseButtons();
}

confirmAdminUpdateUserButton.onclick = (event) => {
    var username = adminUpdateUserUsernameInput.value.trim();
    var name = adminUpdateUserNameInput.value.trim();
    var email = adminUpdateUserEmailInput.value.trim();
    var roles = adminUpdateUserRolesInput.value.trim().split(",").map(role => role.trim());

    if (username &&
        name &&
        email &&
        roles.length > 0
    ) {
        var payload = {
            name: name,
            email: email,
            roleList: roles.toString(),
            username: username
        }
        stompClient.send('/app/admin/users:update', {}, JSON.stringify(payload));
    }
    adminUpdateUserUsernameInput.value = '';
    adminUpdateUserNameInput.value = '';
    adminUpdateUserEmailInput.value = '';
    adminUpdateUserRolesInput.value = '';
}

backToAdminChooseButtonsFromUpdateButton.onclick = (event) => {
    hideElement(adminUpdateUserForm)
    displayAdminChooseButton()
}

var adminDeleteUserUsernameInput = document.querySelector('#admin-delete-username');
var confirmAdminDeleteUserButton = document.querySelector('#confirm-admin-delete-user');
var backToAdminChooseButtonsFromDeleteButton = document.querySelector('#back-to-admin-choose-buttons-from-delete');

adminDeleteUserButton.onclick = (event) => {
    displayElement(adminDeleteUserForm);
    hideAdminChooseButtons();
}

confirmAdminDeleteUserButton.onclick = (event) => {
    var username = adminDeleteUserUsernameInput.value.trim();

    if (username) {
        var payload = {
            username: username
        }
        stompClient.send('/app/admin/users:delete', {}, JSON.stringify(payload));
    }
    adminDeleteUserUsernameInput.value = '';
}

backToAdminChooseButtonsFromDeleteButton.onclick = (event) => {
    hideElement(adminDeleteUserForm)
    displayAdminChooseButton()
}

function hideAdminChooseButtons() {
    hideElement(adminCreateUserButton);
    hideElement(adminUpdateUserButton);
    hideElement(adminDeleteUserButton);
}

function displayAdminChooseButton() {
    displayElement(adminCreateUserButton);
    displayElement(adminUpdateUserButton);
    displayElement(adminDeleteUserButton);
}

function displayElement(element) {
    element.classList.remove('hidden');
}

function hideElement(element) {
    element.classList.add('hidden');
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
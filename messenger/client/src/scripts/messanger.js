const { invoke } = window.__TAURI__.core;
const { listen } = window.__TAURI__.event;

import { getLocal, remLocal, hideLoading, initApp } from "./utils.js";

const knownUserIds = new Set();
const unreadCounts = {};

async function main() {
  listen("server-message", (event) => {
    try {
      const data = JSON.parse(event.payload);

      switch (data.type) {
        case "CHECK_LOGIN":
          hideLoading();
          if (data.status === "STATE_ERR") {
            remLocal("id");
            remLocal("hash");
            window.location.href = "index.html";
          }
          break;

        case "GET_USERS":
          if (data.status === "STATE_ERR") {
            console.error("Ошибка при получении юзеров: ", data.content.message);
            break;
          }
          updateChatList(data.content.users);
          break;

        case "GET_MESSAGES":
          if (data.status === "STATE_ERR") {
            console.error("Ошибка при получении сообщений: ", data.content.message);
            break;
          }
          renderMessagesForChat(data.content.messages);
          break;

        case "USER_STATUS_CHANGED":
          handleUserStatusChange(data.content);
          break;

        case "NEW_MESSAGE":
          handleNewMessage(data.content.message);
          break;

        case "HEARTBEAT":
          remLocal("id");
          remLocal("hash");
          window.location.href = "index.html";
          break;

        case "NEW_USER":
          if (data.status === "STATE_ERR") {
            console.error("Ошибка при получении нового юзера: ", data.content.message);
            break;
          }
          addChatToList({
            name: data.content.name,
            id: data.content.user_id,
            online: false
          });
          break;
      }
    } catch (e) {
      console.error("Ошибка парсинга:", e, event.payload);
    }
  });

  initApp();

  loadFriends();
  loadMessagesForChat(0);
  
}


document.addEventListener('DOMContentLoaded', () => {
    document.querySelector('.messanger__logout').addEventListener('click', logout);
    document.querySelector(".chat__form-btn").addEventListener("click", sendMessage)
    main();
});

async function loadFriends() {
    const hash = getLocal("hash");

    if (hash == null) {
        window.location.href = "index.html";
    }

    const payload = JSON.stringify({
        type: "GET_USERS",
        content: {
            hash: hash
        }
    });

    await invoke("send_to_java", { json: payload });
}

async function logout() {
    try {
        const payload = JSON.stringify({
        type: "LOGOUT",
        content: {}
        });
    
        await invoke("send_to_java", { json: payload });

        remLocal("id");
        remLocal("hash");
        window.location.href = "index.html";
    } catch (err) {
        console.error("Ошибка при отправке LOGOUT:", err);
    }
}

function sendMessage(e) {
    e.preventDefault();
    const to = getActiveChatId();
    if (to == null) {
        console.error("Ошибка не выбран чат");
        return;
    }
    const text = document.querySelector(".chat__form-input").value;
    if (text.trim() == "") {
        return;
    }

    const hash = getLocal("hash");
    const date = getCurrentISODateTime();
    const payload = JSON.stringify({
        type: "SEND_MESSAGE",
        content: {
            hash: hash,
            to: to,
            text: text,
            timestamp: date
        }
    });

    invoke("send_to_java", { json: payload });

    document.querySelector(".chat__form-input").value = "";
}

function addMessageToChat(name, text, date, fromMe = false) {
    const chatMessages = document.querySelector(".chat__messages");

    const messageEl = document.createElement("div");
    messageEl.classList.add("chat__message", fromMe ? "chat__message--outgoing" : "chat__message--incoming");

    const authorEl = document.createElement("span");
    authorEl.className = "chat__message-author";
    authorEl.textContent = fromMe ? "Вы:" : `${name}:`;

    const textEl = document.createElement("span");
    textEl.className = "chat__message-text";
    textEl.textContent = text;

    const timeEl = document.createElement("span");
    timeEl.className = "chat__message-time";
    timeEl.textContent = date;

    messageEl.appendChild(authorEl);
    messageEl.appendChild(textEl);
    messageEl.appendChild(timeEl);
    chatMessages.appendChild(messageEl);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function getActiveChatId() {
    return document.querySelector(".messanger__chats-item--active").dataset.id;
}

function addChatToList(user) {
    const chatList = document.querySelector('.messanger__chats');

    if (knownUserIds.has(user.id)) return;

    const chatItem = document.createElement('div');
    chatItem.classList.add('messanger__chats-item');
    chatItem.dataset.id = user.id;

    const nameSpan = document.createElement('span');
    nameSpan.className = 'messanger__chats-item-name';
    nameSpan.textContent = user.name;
    nameSpan.dataset.original = user.name;

    const onlineSpan = document.createElement('span');
    onlineSpan.className = 'messanger__chats-item-online';
    if (user.online) {
        onlineSpan.classList.add('messanger__chats-item-online--online');
    } else {
        onlineSpan.classList.remove('messanger__chats-item-online--online');
    }

    chatItem.appendChild(nameSpan);
    chatItem.appendChild(onlineSpan);

    chatItem.addEventListener('click', () => {
        unreadCounts[user.id] = 0;
        updateChatItemName(user.id);

        document.querySelectorAll('.messanger__chats-item').forEach(el => el.classList.remove('messanger__chats-item--active'));

        chatItem.classList.add('messanger__chats-item--active');

        loadMessagesForChat(user.id)
    });

    chatList.appendChild(chatItem);
    knownUserIds.add(user.id);
}

function updateChatList(usersFromServer) {
    const chatList = document.querySelector('.messanger__chats');

    if (!knownUserIds.has('0')) {
        const globalChat = document.createElement('div');
        globalChat.classList.add('messanger__chats-item', 'messanger__chats-item--active');
        globalChat.dataset.id = 0;

        const nameSpan = document.createElement('span');
        nameSpan.className = 'messanger__chats-item-name';
        nameSpan.textContent = 'Общий чат';

        globalChat.appendChild(nameSpan);

        globalChat.addEventListener('click', () => {
            unreadCounts[0] = 0;
            updateChatItemName(0);

            document.querySelectorAll('.messanger__chats-item').forEach(el => el.classList.remove('messanger__chats-item--active'));

            globalChat.classList.add('messanger__chats-item--active');
            loadMessagesForChat(0)
        });

        chatList.appendChild(globalChat);
        knownUserIds.add('0');
    }

    usersFromServer.forEach(user => addChatToList(user));
}

function getCurrentISODateTime() {
    const now = new Date();

    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
}

function formatDate(input) {
    const date = new Date(input);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${day}.${month}.${year} ${hours}:${minutes}`;
}

function renderMessagesForChat(messages) {
    const currentUserId = parseInt(getLocal("id"));
    const container = document.querySelector('.chat__messages');
    container.innerHTML = '';

    messages.forEach(({ sender_id, name, message, timestamp }) => {
        const fromMe = sender_id === currentUserId;
        const displayName = fromMe ? "" : name || `Пользователь ${sender_id}`;
        const formattedDate = formatDate(timestamp);
        addMessageToChat(displayName, message, formattedDate, fromMe);
    });
}

function loadMessagesForChat(chatId) {
    const hash = getLocal("hash");
    const payload = JSON.stringify({
        type: "GET_MESSAGES",
        content: {
            hash: hash,
            chat_id: chatId
        }
    });

    invoke("send_to_java", { json: payload });
}

function handleUserStatusChange({ id, online }) {
    const chatItem = document.querySelector(`.messanger__chats-item[data-id="${id}"]`);
    if (!chatItem) return;

    const onlineDot = chatItem.querySelector('.messanger__chats-item-online');
    if (!onlineDot) return;

    if (online) {
        onlineDot.classList.add('messanger__chats-item-online--online');
    } else {
        onlineDot.classList.remove('messanger__chats-item-online--online');
    }
}

function handleNewMessage(message) {
    const currentUserId = parseInt(getLocal("id"));
    const activeChatId = parseInt(getActiveChatId());

    const fromMe = message.sender_id === currentUserId;
    const isForActiveChat =
        parseInt(message.receiver_id) === activeChatId ||
        (parseInt(message.receiver_id) === 0 && activeChatId === 0) ||
        (message.sender_id === activeChatId && message.receiver_id === currentUserId);

    if (isForActiveChat) {
        const formattedDate = formatDate(message.timestamp);
        addMessageToChat(fromMe ? "" : message.name, message.message, formattedDate, fromMe);
    } else {
        const chatId = message.receiver_id === 0 ? 0 : (message.sender_id === currentUserId ? message.receiver_id : message.sender_id);
        unreadCounts[chatId] = (unreadCounts[chatId] || 0) + 1;
        updateChatItemName(chatId);
    }
}

function updateChatItemName(chatId) {
    const chatItem = document.querySelector(`.messanger__chats-item[data-id="${chatId}"]`);
    if (!chatItem) return;

    const nameSpan = chatItem.querySelector('.messanger__chats-item-name');
    if (!nameSpan) return;

    const baseName = nameSpan.dataset.original || nameSpan.textContent.split(' (')[0];
    const count = unreadCounts[chatId] || 0;

    nameSpan.textContent = count > 0 ? `${baseName} (${count})` : baseName;
}
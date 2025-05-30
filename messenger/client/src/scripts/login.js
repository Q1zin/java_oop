const { invoke } = window.__TAURI__.core;
const { listen } = window.__TAURI__.event;

import { setLocal, remLocal, hideLoading, initApp } from "./utils.js";

let javaReady = false;

async function main() {
  listen("server-message", (event) => {
    try {
      const data = JSON.parse(event.payload);
      switch (data.type) {
        case "LOGIN":
          if (data.status === "STATE_ERR") {
            showError(data.content.message);
            return;
          }

          setLocal("id", data.content.id);
          setLocal("hash", data.content.hash);
          window.location.href = "messanger.html";
          break;

        case "CHECK_LOGIN":
          hideLoading();
          if (data.status === "STATE_ERR") {
            remLocal("id");
            remLocal("hash");
            return;
          }

          window.location.href = "messanger.html";
          break;
      }
    } catch (e) {
      console.error("Ошибка парсинга:", e, event.payload);
    }
  });

  initApp();
}

document.addEventListener("DOMContentLoaded", () => {
  document.querySelector(".login__btn").addEventListener("click", userLoginHandler);
  main();
});

async function userLoginHandler(e) {
  e.preventDefault();
  const email = document.querySelector(".login__email").value;
  const password = document.querySelector(".login__password").value;

  if (!email || !password) {
    showError("Заполните все поля");
    return;
  }

  await loginUser(email, password);
}

async function loginUser(email, password) {
  try {
    const payload = {
      type: "LOGIN",
      content: { email, password }
    };
    await invoke("send_to_java", { json: JSON.stringify(payload) });
  } catch (err) {
    console.error("Ошибка при отправке LOGIN:", err);
  }
}

function showError(message) {
  const errorElement = document.querySelector(".login__error");
  errorElement.innerText = message;
  errorElement.classList.add("login__error--active");
}
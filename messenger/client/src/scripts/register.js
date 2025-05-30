const { invoke } = window.__TAURI__.core;
const { listen } = window.__TAURI__.event;

import { setLocal, remLocal, hideLoading, initApp } from "./utils.js";

async function main() {
  listen("server-message", (event) => {
    try {
      const data = JSON.parse(event.payload);

      switch (data.type) {
        case "REGISTER":
          if (data.status === "STATE_ERR") {
            showError(data.content.message);
            return;
          }

          const id = data.content.id;
          const hash = data.content.hash;

          setLocal("id", id);
          setLocal("hash", hash);
          window.location.href = "messenger.html";
          break;

        case "CHECK_LOGIN":
          hideLoading();
          if (data.status === "STATE_ERR" || data.status === "ERROR") {
            remLocal("id");
            remLocal("hash");
            return;
          }

          window.location.href = "messenger.html";
          break;
      }
    } catch (e) {
      console.error("Ошибка парсинга:", e, event.payload);
    }
  });

  initApp();
}

document.addEventListener("DOMContentLoaded", () => {
  document.querySelector(".register__btn").addEventListener("click", registerUserHandler);
  main();
});

async function registerUserHandler(e) {
  e.preventDefault();

  const name = document.querySelector(".register__name").value.trim();
  const email = document.querySelector(".register__email").value.trim();
  const password = document.querySelector(".register__password").value;

  if (name.length < 3 || name.length > 20) {
    showError("Имя должно быть от 3 до 20 символов");
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    showError("Некорректный email");
    return;
  }

  if (password.length < 3 || password.length > 20) {
    showError("Пароль должен быть от 3 до 20 символов");
    return;
  }

  await registerUser(name, email, password);
}

async function registerUser(name, email, password) {
  try {
    const payload = {
      type: "REGISTER",
      content: { name, email, password },
    };

    await invoke("send_to_java", { json: JSON.stringify(payload) });
  } catch (err) {
    console.error("Ошибка при отправке REGISTER:", err);
  }
}

function showError(message) {
  const errorElement = document.querySelector(".register__error");
  errorElement.innerText = message;
  errorElement.classList.add("register__error--active");
}
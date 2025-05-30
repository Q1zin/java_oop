const { invoke } = window.__TAURI__.core;

export function setLocal(name, value) {
  localStorage.setItem(name, value);
}

export function getLocal(name) {
  return localStorage.getItem(name);
}

export function remLocal(name) {
  localStorage.removeItem(name);
}

export function startHeartbeat() {
  const intervalMs = 5_000;

  setInterval(async () => {
    const hash = getLocal("hash");

    const payload = JSON.stringify({
      type: "HEARTBEAT",
      content: { hash: hash ? hash : "" }
    });

    try {
      await invoke("send_to_java", { json: payload });
    } catch (err) {
      console.error("Ошибка при отправке heartbeat:", err);
    }
  }, intervalMs);
}

export function checkLogining() {
    const id = getLocal("id");
    const hash = getLocal("hash");
    if ((id == null || hash == null) && window.location.href == "messanger.html") {
        window.location.href = "login.html";
    }

    if (!hash) {
      hideLoading();
      return;
    }
    
    const payload = JSON.stringify({
        type: "CHECK_LOGIN",
        content: {
            hash: hash
        }
    });

    invoke("send_to_java", { json: payload });
}

export function showLoading() {
  const loadBlock = document.createElement("div");
  loadBlock.className = "loading-block";

  const spinner = document.createElement("div");
  spinner.className = "loading-spinner";

  loadBlock.appendChild(spinner);
  document.body.appendChild(loadBlock);
}

export function hideLoading() {
  const loadBlock = document.querySelector(".loading-block");
  
  if (loadBlock) {
    document.body.removeChild(loadBlock);
  }
}

// Оставил возможность задание хоста и порта, чтобы можно было использовать разные сервера
// можно сделать что-то типо mattermost, где можно указать адрес сервера и подключиться к нему
// но времени на это нет, поэтому пока так
export async function initApp(host = "localhost", port = 4042) {
  showLoading();

  try {
    await invoke("start_java_listener", { host: host, port: port });
  } catch (err) {
    console.error("Не удалось запустить Java:", err);
    return;
  }

  checkLogining();
  startHeartbeat();
}
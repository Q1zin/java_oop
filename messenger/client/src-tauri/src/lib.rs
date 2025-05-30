
use std::io::{BufRead, Write};
use tauri::Emitter;

use tauri::{Window, Manager};
use serde_json::Value;
use std::{process::{Command, Stdio, ChildStdin}, sync::{Arc, Mutex}};
use tauri::path::BaseDirectory;
use once_cell::sync::Lazy;
use std::sync::RwLock;

struct JavaClient {
    stdin: Arc<Mutex<ChildStdin>>,
}

static JAVA_CLIENT: Lazy<RwLock<Option<JavaClient>>> = Lazy::new(|| RwLock::new(None));
static mut INIT_LISTENER: bool = false;
static DEBUG: bool = true;

#[tauri::command]
fn start_java_listener(window: Window, host: String, port: u16) {
    unsafe {
        if INIT_LISTENER {
            return;
        }
        INIT_LISTENER = true;
    }

    let window_ = window.clone();
    let resolver = window_.app_handle().path();
    let resource_path = resolver.resolve("java/ChatClient.app/Contents/MacOS/ChatClient", BaseDirectory::Resource).expect("Не удалось найти java клиент в ресурсах");
    
        let mut child = Command::new(resource_path)
        .arg(host)
        .arg(port.to_string())
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .spawn()
        .expect("Failed to start Java");

    let stdin = Arc::new(Mutex::new(child.stdin.take().unwrap()));
    let stdout = std::io::BufReader::new(child.stdout.take().unwrap());

    JAVA_CLIENT.write().unwrap().replace(JavaClient { stdin });

    std::thread::spawn(move || {
        for line in stdout.lines() {
            if let Ok(msg) = line {
                if DEBUG {
                    println!("Received from Java: {}", &msg);
                }
                let _ = window.emit("server-message", msg);
            }
        }
    });
}

#[tauri::command]
fn send_to_java(json: Value) {
    if let Some(client) = JAVA_CLIENT.read().unwrap().as_ref() {
        let mut stdin = client.stdin.lock().unwrap();
        let _ = writeln!(stdin, "{}", json.to_string());
        if DEBUG {
            println!("Sent to Java: {}", json.to_string());
        }
    }
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![start_java_listener, send_to_java])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}

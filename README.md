112502503

# Android 區域網路多人連線聊天室 (Socket Server & Client)

這是一個基於 Android 平台的區域網路聊天室專案，採用標準的 Socket 通訊協定與多執行緒架構。專案包含兩個獨立的 Android 應用程式模組，分別負責擔任伺服器端 (Server) 與客戶端 (Client)。

## 專案檔案架構

```text
SocketProject/
├── app/ (客戶端 Client)
│   ├── src/main/java/.../
│   │   ├── MainActivity.java         # Client 第一頁：輸入名稱、IP、Port
│   │   └── ClientChatActivity.java   # Client 第二頁：Socket 連線、收發訊息 UI
│   └── src/main/res/layout/
│       ├── activity_main.xml         # Client 連線設定畫面
│       └── activity_client_chat.xml  # Client 聊天室畫面
│
└── ServerApp/ (伺服器端 Server)
    ├── src/main/java/.../
    │   ├── MainActivity.java         # Server 第一頁：建立房間名稱
    │   └── ServerChatActivity.java   # Server 第二頁：ServerSocket 監聽、多執行緒廣播
    └── src/main/res/layout/
        ├── activity_main.xml         # Server 建立房間畫面
        └── activity_server_chat.xml  # Server 伺服器狀態與連線日誌畫面
```


## 實作功能

### 伺服器端 (ServerApp 模組)
* **自訂房間名稱**：第一頁可自訂並建立專屬聊天室。
* **網路資訊顯示**：第二頁會自動抓取並顯示本機 IP 與 Port，方便 Client 查詢。
* **多人連線支援**：透過多執行緒 (Multi-threading) 架構，可同時接收並處理多個 Client 連線。
* **全域訊息廣播**：自動接收個別 Client 的訊息，並同步推播給所有在聊天室內的成員。
* **伺服器主動發言**：伺服器端可於背景執行緒發送「系統公告」給所有連線中的客戶端。
* **進退房動態提示**：當 Client 加入或正常斷線離開時，會自動在聊天室內廣播通知。
* **安全關閉與強制斷線**：伺服器關閉時會向所有人發送最後的斷線警告，並強制中斷所有 Socket 連線以釋放資源。

### 客戶端 (app 模組)
* **彈性連線設定**：第一頁可輸入自訂暱稱、目標 Server IP 與 Port。
* **即時對話視窗**：第二頁提供滾動式對話紀錄與訊息輸入框。
* **背景網路傳輸**：網路讀寫皆運行於背景執行緒，確保 Android UI 畫面流暢不卡頓。
* **斷線偵測與 UI 鎖定**：自動偵測伺服器斷線狀態，斷線時會即時印出提示，並自動反灰鎖定輸入框與傳送按鈕以防呆。
* **安全離開機制**：點擊離開按鈕會正常關閉 Socket 通道並返回首頁，讓伺服器能準確捕捉離線事件。
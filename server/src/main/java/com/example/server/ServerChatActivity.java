package com.example.server;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ServerChatActivity extends AppCompatActivity {

    private TextView serverInfoText, chatLogText;
    private ScrollView scrollView;
    private ServerSocket serverSocket;
    private final int PORT = 8080;
    private boolean isRunning = true;

    // 修正 1：新增標記避免重複關閉，並新增列表統一管理已連線的 Socket
    private boolean isClosing = false;
    private final List<PrintWriter> clientWriters = new ArrayList<>();
    private final List<Socket> clientSockets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_chat);

        serverInfoText = findViewById(R.id.serverInfoText);
        chatLogText = findViewById(R.id.chatLogText);
        scrollView = findViewById(R.id.scrollView);
        EditText serverMessageInput = findViewById(R.id.serverMessageInput);
        Button serverSendBtn = findViewById(R.id.serverSendBtn);
        Button serverLeaveBtn = findViewById(R.id.serverLeaveBtn);

        String serverName = getIntent().getStringExtra("SERVER_NAME");
        String ip = getLocalIpAddress();
        serverInfoText.setText("房間：" + serverName + "\nServer IP: " + ip + "\nPort: " + PORT);

        startServer();

        serverSendBtn.setOnClickListener(v -> {
            String msg = serverMessageInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                new Thread(() -> {
                    broadcastMessage("[伺服器公告]: " + msg);
                }).start();
                serverMessageInput.setText("");
            }
        });

        serverLeaveBtn.setOnClickListener(v -> closeServerAndLeave());
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                updateLog("伺服器已啟動，等待 Client 連線...");

                while (isRunning) {
                    Socket socket = serverSocket.accept();
                    new ClientHandler(socket).start();
                }
            } catch (IOException e) {
                if (isRunning) updateLog("伺服器發生錯誤：" + e.getMessage());
            }
        }).start();
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(writer);
                }
                // 修正 2：將新加入的 Socket 記錄起來，以便關閉時強制踢出
                synchronized (clientSockets) {
                    clientSockets.add(socket);
                }

                clientName = reader.readLine();
                if (clientName != null) {
                    broadcastMessage(clientName + " 進入了聊天室");
                }

                String message;
                while (isRunning && (message = reader.readLine()) != null) {
                    broadcastMessage(clientName + ": " + message);
                }
            } catch (IOException e) {
                // 正常斷線或強制踢出時會觸發此處
            } finally {
                // 修正 3：如果是伺服器主動關閉，就不需要再對大家廣播「xxx 離開了」
                if (clientName != null && !isClosing) {
                    broadcastMessage(clientName + " 離開了聊天室");
                }
                synchronized (clientWriters) {
                    if (writer != null) clientWriters.remove(writer);
                }
                synchronized (clientSockets) {
                    clientSockets.remove(socket);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void broadcastMessage(String message) {
        updateLog(message);
        synchronized (clientWriters) {
            for (PrintWriter w : clientWriters) {
                w.println(message);
            }
        }
    }

    private void updateLog(String message) {
        runOnUiThread(() -> {
            chatLogText.append(message + "\n");
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    private void closeServerAndLeave() {
        // 修正 4：防呆機制，避免 onCreate 跟 onDestroy 重複呼叫
        if (isClosing) return;
        isClosing = true;
        isRunning = false;

        new Thread(() -> {
            broadcastMessage("⚠️ 伺服器已強制關閉，即將中斷連線。");

            // 修正 5：強制中斷所有 Client 的連線，讓他們立刻顯示斷線，無法再傳送幽靈訊息
            synchronized (clientSockets) {
                for (Socket s : clientSockets) {
                    try {
                        if (s != null) s.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(this::finish);
        }).start();
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "無法取得 IP";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeServerAndLeave();
    }
}
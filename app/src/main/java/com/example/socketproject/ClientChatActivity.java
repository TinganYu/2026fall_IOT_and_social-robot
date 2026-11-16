package com.example.socketproject;
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
import java.net.Socket;

public class ClientChatActivity extends AppCompatActivity {

    private TextView chatLogText;
    private EditText messageInput;
    private ScrollView scrollView;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean isRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_chat);

        chatLogText = findViewById(R.id.chatLogText);
        messageInput = findViewById(R.id.messageInput);
        scrollView = findViewById(R.id.scrollView);
        Button sendBtn = findViewById(R.id.sendBtn);
        Button leaveBtn = findViewById(R.id.leaveBtn);

        String name = getIntent().getStringExtra("CLIENT_NAME");
        String ip = getIntent().getStringExtra("SERVER_IP");
        int port = getIntent().getIntExtra("SERVER_PORT", 8080);

        connectToServer(ip, port, name);

        sendBtn.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage(msg);
                messageInput.setText("");
            }
        });

        // 按下離開按鈕
        leaveBtn.setOnClickListener(v -> leaveChat());
    }

    private void connectToServer(String ip, int port, String name) {
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                writer.println(name);

                String message;
                // 當 Server 關閉 Socket 時，readLine() 會讀不到東西而回傳 null，跳出迴圈
                while (isRunning && (message = reader.readLine()) != null) {
                    String finalMsg = message;
                    runOnUiThread(() -> {
                        chatLogText.append(finalMsg + "\n");
                        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    });
                }

                // 如果 isRunning 還是 true，代表不是 Client 自己按離開，而是 Server 斷線了
                if (isRunning) {
                    runOnUiThread(() -> {
                        chatLogText.append("\n❌ 伺服器已關閉，連線中斷。\n");
                        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

                        // 停用輸入框與傳送按鈕，提示使用者只能按離開按鈕
                        findViewById(R.id.sendBtn).setEnabled(false);
                        messageInput.setEnabled(false);
                    });
                }

            } catch (IOException e) {
                if (isRunning) {
                    runOnUiThread(() -> {
                        chatLogText.append("\n❌ 連線失敗或伺服器已關閉。\n");
                        findViewById(R.id.sendBtn).setEnabled(false);
                        messageInput.setEnabled(false);
                    });
                }
            }
        }).start();
    }

    private void sendMessage(String msg) {
        new Thread(() -> {
            if (writer != null) {
                writer.println(msg);
            }
        }).start();
    }

    private void leaveChat() {
        isRunning = false;
        new Thread(() -> {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 切換回主執行緒並結束這個 Activity
            runOnUiThread(this::finish);
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveChat();
    }
}
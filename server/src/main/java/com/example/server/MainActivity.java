package com.example.server;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText serverNameInput = findViewById(R.id.serverNameInput);
        Button startServerBtn = findViewById(R.id.startServerBtn);

        startServerBtn.setOnClickListener(v -> {
            String serverName = serverNameInput.getText().toString().trim();
            if (serverName.isEmpty()) {
                Toast.makeText(this, "請輸入 Server 名稱", Toast.LENGTH_SHORT).show();
                return;
            }
            // 將名稱傳遞並跳轉到第二頁
            Intent intent = new Intent(MainActivity.this, ServerChatActivity.class);
            intent.putExtra("SERVER_NAME", serverName);
            startActivity(intent);
        });
    }
}
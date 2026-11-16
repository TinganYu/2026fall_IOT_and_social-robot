package com.example.socketproject;
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

        EditText clientNameInput = findViewById(R.id.clientNameInput);
        EditText ipInput = findViewById(R.id.ipInput);
        EditText portInput = findViewById(R.id.portInput);
        Button connectBtn = findViewById(R.id.connectBtn);

        connectBtn.setOnClickListener(v -> {
            String name = clientNameInput.getText().toString().trim();
            String ip = ipInput.getText().toString().trim();
            String portStr = portInput.getText().toString().trim();

            if (name.isEmpty() || ip.isEmpty() || portStr.isEmpty()) {
                Toast.makeText(this, "所有欄位都必須填寫", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(MainActivity.this, ClientChatActivity.class);
            intent.putExtra("CLIENT_NAME", name);
            intent.putExtra("SERVER_IP", ip);
            intent.putExtra("SERVER_PORT", Integer.parseInt(portStr));
            startActivity(intent);
        });
    }
}
package com.example.proyectofinalmqtt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> hacerLogin());

        btnRegister.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(i);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Si ya hay sesión, saltar directo al Home
        if (mAuth.getCurrentUser() != null) {
            irAlHome();
        }
    }

    private void hacerLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    "Login exitoso ✅", Toast.LENGTH_SHORT).show();
                            irAlHome();
                        } else {
                            String msg = (task.getException() != null)
                                    ? task.getException().getMessage()
                                    : "Error desconocido";
                            Toast.makeText(MainActivity.this,
                                    "Error de inicio de sesión: " + msg,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void irAlHome() {
        Intent i = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(i);
        finish(); // evitar volver al login con botón atrás
    }
}

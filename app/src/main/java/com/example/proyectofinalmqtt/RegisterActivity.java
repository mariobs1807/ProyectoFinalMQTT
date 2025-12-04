package com.example.proyectofinalmqtt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombreReg, etEmailReg, etPasswordReg;
    private Button btnCrearCuenta, btnVolverMain;

    private FirebaseAuth mAuth;
    private DatabaseReference usuariosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Vistas
        etNombreReg   = findViewById(R.id.etNombreReg);
        etEmailReg    = findViewById(R.id.etEmailReg);
        etPasswordReg = findViewById(R.id.etPasswordReg);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        btnVolverMain  = findViewById(R.id.btnVolverLogin);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase
                .getInstance()
                .getReference("usuarios");

        // Volver al login
        btnVolverMain.setOnClickListener(v -> {
            finish();
        });

        // Crear cuenta
        btnCrearCuenta.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre   = etNombreReg.getText().toString().trim();
        String correo   = etEmailReg.getText().toString().trim();
        String password = etPasswordReg.getText().toString().trim();

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) Crear usuario en Firebase
        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(RegisterActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (!task.isSuccessful()) {
                                    String msg = (task.getException() != null)
                                            ? task.getException().getMessage()
                                            : "Error desconocido";
                                    Toast.makeText(RegisterActivity.this,
                                            "Error al registrar: " + msg,
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //  Usuario creado, nos entrega  su UID
                                String uid = task.getResult().getUser().getUid();

                                //  objeto a guardar en Realtime DB
                                Map<String, Object> datosUsuario = new HashMap<>();
                                datosUsuario.put("nombre", nombre);
                                datosUsuario.put("correo", correo);
                                datosUsuario.put("rol", "Invitado");

                                // Guardar en /usuarios/UID
                                usuariosRef.child(uid)
                                        .setValue(datosUsuario)
                                        .addOnCompleteListener(t -> {
                                            if (t.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this,
                                                        "Cuenta creada correctamente",
                                                        Toast.LENGTH_SHORT).show();
                                                // Volver al login
                                                finish();
                                            } else {
                                                String msg2 = (t.getException() != null)
                                                        ? t.getException().getMessage()
                                                        : "Error desconocido al guardar datos";
                                                Toast.makeText(RegisterActivity.this,
                                                        msg2,
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        });
    }
}

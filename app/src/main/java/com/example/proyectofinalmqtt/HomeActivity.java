package com.example.proyectofinalmqtt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private TextView tvBienvenida, tvRol;
    private Button btnEditarPerfil, btnLogout, btnFeedback;

    private FirebaseAuth mAuth;
    private DatabaseReference usuariosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvBienvenida   = findViewById(R.id.tvBienvenida);
        tvRol          = findViewById(R.id.tvRol);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnFeedback = findViewById(R.id.btnFeedback);  // NUEVO BOTÓN
        btnLogout       = findViewById(R.id.btnLogout);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            // Sin sesión: volver al login
            Intent i = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(i);
            finish();
            return;
        }

        String uid = user.getUid();
        usuariosRef = FirebaseDatabase
                .getInstance()
                .getReference("usuarios")
                .child(uid);

        cargarDatosUsuario();

        // Botón Editar Perfil
        btnEditarPerfil.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, EditarPerfilActivity.class);
            startActivity(i);
        });

        // Botón Enviar Comentarios (MQTT) - NUEVO
        btnFeedback.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, FeedbackActivity.class);
            startActivity(i);
        });

        // Botón Cerrar Sesión
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent i = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //
        if (mAuth.getCurrentUser() != null) {
            cargarDatosUsuario();
        }
    }

    private void cargarDatosUsuario() {
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String nombre = snapshot.child("nombre").getValue(String.class);
                String rol    = snapshot.child("rol").getValue(String.class);

                if (nombre == null) nombre = "(sin nombre)";
                if (rol == null)    rol    = "(sin rol)";

                tvBienvenida.setText("Bienvenido " + nombre);
                tvRol.setText("Tipo de usuario: " + rol);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this,
                        "Error al leer datos: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
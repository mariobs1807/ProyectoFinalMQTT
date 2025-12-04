package com.example.proyectofinalmqtt;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.HashMap;

public class EditarPerfilActivity extends AppCompatActivity {

    private EditText editTextNombre;
    private EditText editTextTipoUsuario;
    private Button buttonGuardarCambios;

    // Variables de Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_perfil);

        // INICIALIZAR FIREBASE
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("usuarios");

        // Obtener usuario actual
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ENLAZAR VARIABLES CON LA INTERFAZ
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextTipoUsuario = findViewById(R.id.editTextTipoUsuario);
        buttonGuardarCambios = findViewById(R.id.buttonGuardarCambios);

        // CONFIGURAR LISTENER DEL BOTÓN
        buttonGuardarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nuevoNombre = editTextNombre.getText().toString().trim();
                String nuevoRol = editTextTipoUsuario.getText().toString().trim();

                if (nuevoNombre.isEmpty()) {
                    editTextNombre.setError("El nombre es requerido");
                    editTextNombre.requestFocus();
                    return;
                }

                if (nuevoRol.isEmpty()) {
                    editTextTipoUsuario.setError("El rol es requerido");
                    editTextTipoUsuario.requestFocus();
                    return;
                }

                buttonGuardarCambios.setEnabled(false);
                buttonGuardarCambios.setText("Guardando...");

                HashMap<String, Object> updateData = new HashMap<>();
                updateData.put("nombre", nuevoNombre);
                updateData.put("rol", nuevoRol);

                usersRef.child(currentUserId).updateChildren(updateData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(EditarPerfilActivity.this,
                                        "Perfil actualizado correctamente",
                                        Toast.LENGTH_SHORT).show();

                                buttonGuardarCambios.setEnabled(true);
                                buttonGuardarCambios.setText("Guardar Cambios");
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(EditarPerfilActivity.this,
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();

                                buttonGuardarCambios.setEnabled(true);
                                buttonGuardarCambios.setText("Guardar Cambios");
                            }
                        });
            }
        });

        //
        try {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } catch (Exception e) {
            //
        }
    }
}
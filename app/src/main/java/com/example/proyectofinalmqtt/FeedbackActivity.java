package com.example.proyectofinalmqtt;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {

    private TextView textViewRespuestas;
    private Button buttonTest;
    private EditText editTextComentario;
    private org.eclipse.paho.client.mqttv3.MqttClient mqttClient;

    // Constantes MQTT -
    private static final String BROKER_URL = "ssl://7b7ed934554948a1a4f053623181e246.s1.eu.hivemq.cloud:8883";
    private static final String CLIENT_ID = "AndroidApp_" + System.currentTimeMillis();
    private static final String TOPIC = "ProyectoFinalMQTT"; // tópico
    private static final String USERNAME = "mariodev";
    private static final String PASSWORD = "MarioBravo1234-";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        textViewRespuestas = findViewById(R.id.textViewRespuestas);
        buttonTest = findViewById(R.id.buttonEnviar);
        editTextComentario = findViewById(R.id.editTextComentario);

        buttonTest.setText("CONECTAR MQTT");

        //
        textViewRespuestas.setText("Listo para conectar a HiveMQ\n\n"
                + "Usuario: " + USERNAME);

        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conectarMQTT();
            }
        });
    }

    private void conectarMQTT() {
        textViewRespuestas.setText("Conectando a HiveMQ...");
        buttonTest.setEnabled(false);

        new Thread(() -> {
            try {
                // Crear cliente
                mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient(
                        BROKER_URL,
                        CLIENT_ID + System.currentTimeMillis(),
                        new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence()
                );

                // Configurar opciones
                org.eclipse.paho.client.mqttv3.MqttConnectOptions options =
                        new org.eclipse.paho.client.mqttv3.MqttConnectOptions();
                options.setCleanSession(true);
                options.setConnectionTimeout(15);
                options.setKeepAliveInterval(30);
                options.setUserName(USERNAME);
                options.setPassword(PASSWORD.toCharArray());

                // Configurar SSL
                javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                sslContext.init(null, null, new java.security.SecureRandom());
                options.setSocketFactory(sslContext.getSocketFactory());

                // Conectar
                mqttClient.connect(options);

                // ========== CONFIGURAR RECEPCIÓN ==========
                mqttClient.setCallback(new org.eclipse.paho.client.mqttv3.MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        runOnUiThread(() -> {
                            Toast.makeText(FeedbackActivity.this,
                                    "Conexión perdida", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
                        String mensaje = new String(message.getPayload());
                        runOnUiThread(() -> {
                            mostrarMensajeRecibido(mensaje);
                        });
                    }

                    @Override
                    public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) {
                        // Mensaje entregado
                    }
                });

                // Suscribirse para recibir mensajes
                mqttClient.subscribe(TOPIC, 0);
                // ===========================================

                runOnUiThread(() -> {
                    textViewRespuestas.setText("✅ Conectado a HiveMQ\n\n"
                            + "Escribe un comentario y toca ENVIAR");
                    Toast.makeText(FeedbackActivity.this,
                            "✅ Conectado y suscrito", Toast.LENGTH_SHORT).show();

                    buttonTest.setText("ENVIAR COMENTARIO");
                    buttonTest.setEnabled(true);
                    buttonTest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            enviarComentario();
                        }
                    });
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    textViewRespuestas.setText("❌ Error: " + e.getMessage() + "\n\n"
                            + "Intentando sin SSL...");
                    conectarMQTTSinSSL();
                });
            }
        }).start();
    }

    private void conectarMQTTSinSSL() {
        new Thread(() -> {
            try {
                String brokerSinSSL = "tcp://7b7ed934554948a1a4f053623181e246.s1.eu.hivemq.cloud:1883";
                mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient(
                        brokerSinSSL,
                        CLIENT_ID + "_noSSL",
                        new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence()
                );

                org.eclipse.paho.client.mqttv3.MqttConnectOptions options =
                        new org.eclipse.paho.client.mqttv3.MqttConnectOptions();
                options.setCleanSession(true);
                options.setConnectionTimeout(10);
                options.setKeepAliveInterval(20);
                options.setUserName(USERNAME);
                options.setPassword(PASSWORD.toCharArray());

                mqttClient.connect(options);

                // Configurar recepción también aquí
                mqttClient.setCallback(new org.eclipse.paho.client.mqttv3.MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        runOnUiThread(() -> {
                            Toast.makeText(FeedbackActivity.this,
                                    "Conexión perdida", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
                        String mensaje = new String(message.getPayload());
                        runOnUiThread(() -> {
                            mostrarMensajeRecibido(mensaje);
                        });
                    }

                    @Override
                    public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) {
                        // Mensaje entregado
                    }
                });

                mqttClient.subscribe(TOPIC, 0);

                runOnUiThread(() -> {
                    textViewRespuestas.setText("✅ Conectado (sin SSL)");
                    Toast.makeText(FeedbackActivity.this,
                            "✅ Conectado sin SSL", Toast.LENGTH_SHORT).show();

                    buttonTest.setText("ENVIAR COMENTARIO");
                    buttonTest.setEnabled(true);
                    buttonTest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            enviarComentario();
                        }
                    });
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    textViewRespuestas.setText("❌ Error en ambas conexiones\n\n"
                            + e.getMessage());
                    buttonTest.setEnabled(true);
                });
            }
        }).start();
    }

    private void enviarComentario() {
        String mensaje = editTextComentario.getText().toString().trim();

        if (mensaje.isEmpty()) {
            editTextComentario.setError("Escribe algo primero");
            editTextComentario.requestFocus();
            return;
        }

        if (mqttClient == null || !mqttClient.isConnected()) {
            Toast.makeText(this, "No conectado", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonTest.setEnabled(false);
        buttonTest.setText("ENVIANDO...");

        new Thread(() -> {
            try {
                org.eclipse.paho.client.mqttv3.MqttMessage mqttMsg =
                        new org.eclipse.paho.client.mqttv3.MqttMessage();
                mqttMsg.setPayload(mensaje.getBytes());
                mqttMsg.setQos(0);

                // Publicar en el mismo tópico
                mqttClient.publish(TOPIC, mqttMsg);

                runOnUiThread(() -> {
                    // Mostrar mensaje enviado
                    String textoActual = textViewRespuestas.getText().toString();
                    String tiempo = new java.text.SimpleDateFormat("HH:mm:ss")
                            .format(new java.util.Date());
                    String nuevoTexto = "[" + tiempo + "] Tú: " + mensaje + "\n" + textoActual;
                    textViewRespuestas.setText(nuevoTexto);

                    editTextComentario.setText("");
                    buttonTest.setEnabled(true);
                    buttonTest.setText("ENVIAR COMENTARIO");

                    Toast.makeText(FeedbackActivity.this,
                            "✅ Enviado", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(FeedbackActivity.this,
                            "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    buttonTest.setEnabled(true);
                    buttonTest.setText("ENVIAR COMENTARIO");
                });
            }
        }).start();
    }

    private void mostrarMensajeRecibido(String mensaje) {
        String textoActual = textViewRespuestas.getText().toString();
        String tiempo = new java.text.SimpleDateFormat("HH:mm:ss")
                .format(new java.util.Date());
        String nuevoTexto = "[" + tiempo + "] Dev: " + mensaje + "\n" + textoActual;
        textViewRespuestas.setText(nuevoTexto);

        Toast.makeText(this, "📩 Nuevo mensaje", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
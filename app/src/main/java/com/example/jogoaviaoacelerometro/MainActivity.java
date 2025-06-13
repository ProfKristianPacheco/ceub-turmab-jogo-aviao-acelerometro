package com.example.jogoaviaoacelerometro;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private Sensor acelerometro;
    private SensorManager gerenciador;
    private SensorEventListener listener;
    private ImageView aviao;
    private float larguraTela;
    private FrameLayout layoutJogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        startGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gerenciador.registerListener(listener, acelerometro, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gerenciador.unregisterListener(listener);
    }

    private void startGame() {
        gerenciador = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometro = gerenciador.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        larguraTela = getResources().getDisplayMetrics().widthPixels;
        aviao = findViewById(R.id.imageViewAviao);
        layoutJogo = findViewById(R.id.layoutJogo);

        moveAviao();
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                disparaTiro();
                handler.postDelayed(this, 500);
            }
        });


    }

    private void disparaTiro() {
        ImageView tiro = new ImageView(this);
        tiro.setImageResource(R.drawable.tiro);

        tiro.setLayoutParams(new FrameLayout.LayoutParams(100, 100));

        tiro.setX(aviao.getX() + (aviao.getWidth()/2)-50);
        tiro.setY(aviao.getY());

        ObjectAnimator animator = ObjectAnimator.ofFloat(tiro, "y", 0);
        animator.setDuration(1000);
        animator.start();

        layoutJogo.addView(tiro);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                layoutJogo.removeView(tiro);
            }
        });
    }

    private void moveAviao() {
        listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float eixoX = event.values[0];
                float novaPosicao = aviao.getX() - eixoX * 4;

                if(novaPosicao < 0){
                    novaPosicao = 0;
                }

                if(novaPosicao > larguraTela - aviao.getWidth()){
                    novaPosicao = larguraTela - aviao.getWidth();
                }
                aviao.setX(novaPosicao);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }
}
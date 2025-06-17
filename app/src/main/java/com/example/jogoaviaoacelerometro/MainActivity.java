package com.example.jogoaviaoacelerometro;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Sensor acelerometro;
    private SensorManager gerenciador;
    private SensorEventListener listener;
    private ImageView aviao;
    private ImageView gameOver;
    private float larguraTela;
    private float alturaTela;
    private FrameLayout layoutJogo;
    private List<ImageView> listaAvioesInimigos = new ArrayList<>();
    private List<ImageView> listaTirosInimigos = new ArrayList<>();
    private List<ImageView> listaTiros = new ArrayList<>();
    private boolean continua;
    private int pontuacao;
    private TextView textViewPontuacao;
    private ImageButton restart;
    Handler handler = new Handler();

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
        gerenciador.registerListener(listener, acelerometro, SensorManager.SENSOR_DELAY_GAME); //REGISTRANDO O LISTENER NO ONRESUME POR BOA PRÁTICA, POUPANDO RECURSOS DO SMARTPHONE
    }

    @Override
    protected void onPause() {
        super.onPause();
        gerenciador.unregisterListener(listener); //DESCADASTRANDO O LISTENER NO ONPAUSE, PARA QUE ELE NÃO SEJA EXECUTADO QUANDO A ACTIVITY ESTÁ INATIVA
    }

    private void startGame() {
        gerenciador = (SensorManager) getSystemService(SENSOR_SERVICE); //inicia o gerenciador e o acelerômetro
        acelerometro = gerenciador.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        larguraTela = getResources().getDisplayMetrics().widthPixels; //CAPTURA A LARGURA E A ALTURA DA TELA
        alturaTela = getResources().getDisplayMetrics().heightPixels;

        aviao = findViewById(R.id.imageViewAviao); //INICIALIZA A VARIÁVEL AVIÃO QUE ESTÁ NO LAYOUT PRINCIPAL
        layoutJogo = findViewById(R.id.layoutJogo); //INICIALIZA O FRAMELAYOUT QUE ESTÁ NO LAYOUT PRINCIPAL. A IDEIA É USAR UM LAYOUT SEPARADO PRA FAZER A MOVIMENTAÇÃO DOS COMPONENTES DO JOGO, PARA NÃO FICAR PRESO A CONSTRAINTS
        gameOver = findViewById(R.id.imageViewGameOver); //INICIALIZA O IMAGE VIEW GAME OVER
        textViewPontuacao = findViewById(R.id.textViewPontuacao); //INICIALIZA O TEXT VIEW DE PONTUAÇÃO
        restart = findViewById(R.id.imageButtonRestart); //INICIALIZA O IMAGE BUTTON RESTART

        aviao.setVisibility(ImageView.VISIBLE); //DEFINE O AVIÃO PRINCIPAL COMO VISÍVEL
        pontuacao = 0; //ZERA A PONTUAÇÃO
        continua = true; //BOOLEAN PRA CONTINUAR O JOGO

        gameOver.setVisibility(ImageView.GONE); //ESCONDE O RESTART E O GAME OVER, E EXIBE A PONTUAÇÃO INICIAL QUE É ZERO
        restart.setVisibility(ImageButton.GONE);
        textViewPontuacao.setText("Pontuação: " + pontuacao);

        moveAviao(); //USA O ACELERÔMETRO PRA MOVER O AVIÃO NO EIXO X

        handler.post(new Runnable() {
            @Override
            public void run() { // O HANDLER "AGENDA" A EXECUÇÃO DOS MÉTODOS ABAIXO, QUE PRECISAM SER EXECUTADOS DE FORMA SÍNCRONA COM O MOVIMENTO DO AVIÃO
                if (continua) {
                    disparaTiro(); // DISPARA UM TIRO VINDO DO AVIÃO PRINCIPAL. COMO O MÉTODO ESTÁ EM RECURSIVIDADE, IRÁ GERAR INFINITOS TIROS ATÉ QUE O JOGO FINALIZE
                    geraAviaoInimigo(); //GERA UM AVIÃO INIMIGO. COMO O MÉTODO ESTÁ EM RECURSIVIDADE, IRÁ GERAR INFINITOS AVIÕES ATÉ QUE O JOGO FINALIZE
                    handler.postDelayed(this, 500); //CHAMA O PRÓPRIO MÉTODO A CADA MEIO SEGUNDO (RECURSIVIDADE)
                }
            }
        });

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(); //REINICIA O JOGO AO CLICAR EM RESTART
            }
        });


    }

    private void geraAviaoInimigo() {
        ImageView aviaoInimigo = new ImageView(this); //CRIA UM NOVO COMPONENTE DE TELA IMAGEVIEW PRO AVIÃO INIMIGO

        aviaoInimigo.setImageResource(R.drawable.inimigo); //SETA A IMAGEM DO AVIÃO INIMIGO
        aviaoInimigo.setLayoutParams(new FrameLayout.LayoutParams(200, 200)); //DEFINE A LARGURA E A ALTURA DO AVIÃO INIMIGO

        aviaoInimigo.setX((float) (Math.random() * larguraTela)); //GERA O AVIÃO EM UMA POSIÇÃO ALEATÓRIA DO EIXO X
        aviaoInimigo.setY((float) (Math.random() * 1000)); //GERA O AVIÃO EM UMA POSIÇÃO ALEATÓRIA DO EIXO Y

        layoutJogo.addView(aviaoInimigo); //ADICIONA O AVIÃO AO FRAMELAYOUT DO JOGO
        listaAvioesInimigos.add(aviaoInimigo); //ADICIONA O AVIÃO À LISTA DE AVIÕES INIMIGOS

        handler.post(new Runnable() {
            @Override
            public void run() {
                disparaTiroInimigo(aviaoInimigo); //DISPARA UM TIRO INIMIGO PARA CADA AVIÃO GERADO. COMO O MÉTODO ESTÁ EM RECURSIVIDADE, IRÁ GERAR INFINITOS TIROS ATÉ QUE O JOGO FINALIZE
                handler.postDelayed(this, 1000);
            }
        });

        ObjectAnimator animator = ObjectAnimator.ofFloat(aviaoInimigo, "x", larguraTela); //MOVE O AVIÃO INIMIGO NO EIXO X
        animator.setDuration(2000); //DURAÇÃO DA ANIMAÇÃO DE MOVIMENTAÇÃO DO AVIÃO INIMIGO
        animator.start(); //INICIA A ANIMAÇÃO

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                layoutJogo.removeView(aviaoInimigo); //REMOVE O AVIÃO INIMIGO DO LAYOUT DO JOGO QUANDO A ANIMAÇÃO FINALIZA
                listaAvioesInimigos.remove(aviaoInimigo); //REMOVE O AVIÃO INIMIGO DA LISTA DE AVIÕES QUANDO A ANIMAÇÃO FINALIZA
            }
        });
    }

    private void disparaTiroInimigo(ImageView aviaoInimigo) {
        ImageView tiroInimigo = new ImageView(this); //CRIA UM NOVO COMPONENTE DE TELA IMAGEVIEW PRO TIRO INIMIGO
        tiroInimigo.setImageResource(R.drawable.tiroinimigo); //SETA A IMAGEM DO TIRO INIMIGO
        tiroInimigo.setLayoutParams(new FrameLayout.LayoutParams(100,100)); //SETA O TAMANHO DO TIRO INIMIGO

        tiroInimigo.setX(aviaoInimigo.getX() + aviaoInimigo.getWidth()/2); // SETA A POSIÇÃO X E Y DO TIRO DE ACORDO COM A POSIÇÃO DO AVIÃO
        tiroInimigo.setY(aviaoInimigo.getY() + aviaoInimigo.getHeight());

        layoutJogo.addView(tiroInimigo); //ADICIONA O TIRO AO LAYOUT DO JOGO
        listaTirosInimigos.add(tiroInimigo); //ADICIONA O TIRO À LISTA DE TIROS INIMIGOS

        ObjectAnimator animator = ObjectAnimator.ofFloat(tiroInimigo, "y", alturaTela); //CRIA UMA ANIMAÇÃO PRO TIRO INIMIGO, PERCORRENDO A TELA NO EIXO Y
        animator.setDuration(2000); //SETA 2 SEGUNDOS DE DURAÇÃO DA ANIMAÇÃO
        animator.start(); //INICIA A ANIMAÇÃO

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                layoutJogo.removeView(tiroInimigo); //REMOVE O TIRO INIMIGO QUANDO A ANIMAÇÃO FINALIZA
                listaTirosInimigos.remove(tiroInimigo); //REMOVE O TIRO INIMIGO DA LISTA DE TIROS
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                for (ImageView tiroAtual: listaTirosInimigos) { //LÓGICA DE FIM DE JOGO. QUALQUER TIRO DA LISTA DE TIROS INIMIGOS PODE ME ATINGIR
                    if(verificaAcerto(tiroAtual, aviao)){ //SE ATINGIR, ESCONDE O AVIÃO, E EXIBE A TELA DE GAME OVER, E O BOTÃO DE RESTART
                        continua = false;
                        aviao.setVisibility(ImageView.GONE);
                        gameOver.setVisibility(ImageView.VISIBLE);
                        restart.setVisibility(ImageButton.VISIBLE);
                        listaTiros.clear(); //limpa as listas pra melhorar a performance
                        listaAvioesInimigos.clear();
                        listaTirosInimigos.clear();
                        return;
                    }
                }
            }
        });
    }

    private void disparaTiro() {
        ImageView tiro = new ImageView(this); //CRIA UM NOVO COMPONENTE IMAGEVIEW
        tiro.setImageResource(R.drawable.tiro); //SETA A IMAGEM CORRESPONDENTE

        tiro.setLayoutParams(new FrameLayout.LayoutParams(100, 100)); //SETA O TAMANHO DO COMPONENTE (LARGURA E ALTURA)

        tiro.setX(aviao.getX() + (aviao.getWidth()/2)-50); //SETA A POSIÇÃO X E Y DO TIRO, SAINDO DO BICO DO AVIÃO
        tiro.setY(aviao.getY());

        ObjectAnimator animator = ObjectAnimator.ofFloat(tiro, "y", 0); //CRIA UMA NOVA ANIMAÇÃO PRO IMAGEVIEW TIRO. ESSA ANIMAÇÃO FARÁ O IMAGEVIEW PERCORRER O EIXO Y, ATÉ A POSIÇÃO 0
        animator.setDuration(1000); //SETA 1 SEGUNDO DE DURAÇÃO PRA ANIMAÇÃO
        animator.start(); //INICIA A ANIMAÇÃO

        layoutJogo.addView(tiro); // ADICIONA O TIRO AO FRAMELAYOUT
        listaTiros.add(tiro);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                layoutJogo.removeView(tiro); //REMOVE O TIRO QUANDO ELE CHEGA NA POSIÇÃO 0
                listaTiros.remove(tiro);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                for (ImageView inimigoAtual: listaAvioesInimigos) { //LÓGICA DE PONTUAÇÃO. CADA TIRO GERADO PODE ATINGIR QUALQUER INIMIGO
                    if(verificaAcerto(tiro, inimigoAtual)){ //QUANDO O TIRO ATINGE O INIMIGO, REMOVE O INIMIGO DO JOGO, REMOVE O TIRO QUE O ATINGIU, INCREMENTA A PONTUAÇÃO, E ATUALIZA O TEXTVIEW
                       layoutJogo.removeView(inimigoAtual);
                       layoutJogo.removeView(tiro);
                       listaTiros.remove(tiro);
                       listaAvioesInimigos.remove(inimigoAtual);
                       pontuacao++;
                       textViewPontuacao.setText("Pontuação: " + pontuacao);
                       return;
                    }
                }
            }
        });

    }

    private boolean verificaAcerto(ImageView tiroAtual, ImageView aviao) { //VERIFICA ACERTO A PARTIR DE RETÂNGULOS, E RETORNA TRUE SE ELES SE ENCOSTAREM
        Rect tiroRect = new Rect();
        tiroAtual.getGlobalVisibleRect(tiroRect);

        Rect aviaoRect = new Rect();
        aviao.getGlobalVisibleRect(aviaoRect);
        return Rect.intersects(tiroRect, aviaoRect);
    }

    private void moveAviao() {
        listener = new SensorEventListener() { //LISTENER DO SENSOR INICIALIZADO
            @Override
            public void onSensorChanged(SensorEvent event) {
                float eixoX = event.values[0]; //CAPTURA O MOVIMENTO DO ACELERÔMETRO PRA DIREITA/ESQUERDA
                float novaPosicao = aviao.getX() - eixoX * 4; //DEFINE A NOVA POSIÇÃO DE ACORDO COM A POSIÇÃO DO ACELERÔMETRO + A POSIÇÃO ATUAL DO AVIÃO

                if(novaPosicao < 0){
                    novaPosicao = 0; //GARANTE QUE O AVIÃO NÃO VAI ULTRAPASSAR A BORDA ESQUERDA DO SMARTPHONE
                }

                if(novaPosicao > larguraTela - aviao.getWidth()){
                    novaPosicao = larguraTela - aviao.getWidth(); //GARANTE QUE O AVIÃO NÃO VAI ULTRAPASSAR A BORDA DIREITA DO SMARTPHONE
                }
                aviao.setX(novaPosicao); //SETA A NOVA POSIÇÃO DO AVIÃO
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }
}
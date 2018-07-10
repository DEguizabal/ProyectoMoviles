package com.example.uno.proyectomoviles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class GameView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    private Player player;

    //un almacenador para el ancho de la pantalla
    int screenX;


    //Contexto que se utilizara en onTouchEvent para realizar una transicion de GameActivity a MainActivity
    Context context;

    //El almacenador para score
    int score;

    //un almacenador para los 4 scores mas altos
    int highScore[] = new int[4];

    //Objeto Shared Preferrences para guardar el score
    SharedPreferences sharedPreferences;


    //El contador para el numero de fallos
    int countMisses;

    //Un indicador que nos dice si un enemigo ha entrado en la pantalla
    boolean flag ;

    //Un indicador por si el juego termina
    private boolean isGameOver ;

    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private Enemy enemies;



    //Creando una referencia a la clase Friend
    private Friend friend;


    private ArrayList<Star> stars = new
            ArrayList<Star>();

    //Definiendo un objeto de tipo boom para mostrar una explosion
    private Boom boom;

    //Los objetos MediaPlayer para configurar la musica de fondo
    static  MediaPlayer gameOnsound;

    final MediaPlayer killedEnemysound;

    final MediaPlayer gameOversound;



    public GameView(Context context, int screenX, int screenY) {
        super(context);
        player = new Player(context, screenX, screenY);


        surfaceHolder = getHolder();
        paint = new Paint();

        //Inicializando el contexto
        this.context = context;

        int starNums = 100;
        for (int i = 0; i < starNums; i++) {
            Star s = new Star(screenX, screenY);
            stars.add(s);
        }




        enemies = new Enemy(context,screenX,screenY);

        //Inicializando el objeto boom
        boom = new Boom(context);

        //Inicializando el objeto de amigo
        friend = new Friend(context, screenX, screenY);

        //Seteando el score a 0
        score = 0;

        //Seteando los fallos a 0
        countMisses = 0;


        this.screenX = screenX;


        isGameOver = false;


        //Iniciando el objeto SharedPreferences
        sharedPreferences = context.getSharedPreferences("SHAR_PREF_NAME",Context.MODE_PRIVATE);


        //initializing the array high scores with the previous values
        highScore[0] = sharedPreferences.getInt("score1",0);
        highScore[1] = sharedPreferences.getInt("score2",0);
        highScore[2] = sharedPreferences.getInt("score3",0);
        highScore[3] = sharedPreferences.getInt("score4",0);

        //Inicializando los archivos para la musica y sonidos del juego
        gameOnsound = MediaPlayer.create(context,R.raw.gameon);
        killedEnemysound = MediaPlayer.create(context,R.raw.killedenemy);
        gameOversound = MediaPlayer.create(context,R.raw.gameover);

        //Iniciando la musica que sera reproducida en el juego
        gameOnsound.start();

    }





    @Override
    public void run() {

        while (playing) {

            update();
            draw();
            control();

        }


    }


    private void update() {

        //Incrementando el score mientras pasa el tiempo
        score++;

        player.update();

        //Seteando la colision fuera de la pantalla
        boom.setX(-250);
        boom.setY(-250);

        for (Star s : stars) {

            s.update(player.getSpeed());
        }

        //Seteando la flag a true cuando un enemigo entra en la pantalla
        if(enemies.getX()==screenX){

            flag = true;
        }


        enemies.update(player.getSpeed());
        //Si ocurre una colision con el jugador
        if (Rect.intersects(player.getDetectCollision(), enemies.getDetectCollision())) {

            //Mostrando la colision en esa ubicacion
            boom.setX(enemies.getX());
            boom.setY(enemies.getY());


            //Reproduce un sonido cuando el jugador colisiona con un enemigo
            killedEnemysound.start();

            enemies.setX(-200);
        }

        else{// La condicion cuando el jugador falla a un enemigo

            //Si el enemigo acaba de entrar
            if(flag){

                //Si la coordenada x del jugador es igual a la coordenada x del enemigo
                if(player.getDetectCollision().exactCenterX()>=enemies.getDetectCollision().exactCenterX()){

                    //incrementando countMisses
                    countMisses++;

                    //Cambiando el valor de flag a false dado que el else solo se ejecutara cuando un nuevo enemigo entre en la pantalla
                    flag = false;

                    //Si el numero de fallos es igual a 3 entonces se acaba el juego
                    if(countMisses==3){

                        //Seteando playing a false y Game over a true
                        playing = false;
                        isGameOver = true;


                        //Parando la musica de juego
                        gameOnsound.stop();
                        //Reproduciendo la musica de juego acabado
                        gameOversound.start();

                        //Asignando los scores al arreglo de scores
                        for(int i=0;i<4;i++){
                            if(highScore[i]<score){

                                final int finalI = i;
                                highScore[i] = score;
                                break;
                            }
                        }

                        //Almacenando el score con Shared Preferences
                        SharedPreferences.Editor e = sharedPreferences.edit();

                        for(int i=0;i<4;i++){

                            int j = i+1;
                            e.putInt("score"+j,highScore[i]);
                        }
                        e.apply();

                    }

                }
            }

        }



        //Actualizando las coordenadas de las naves aliadas
        friend.update(player.getSpeed());
        //Chequeando si hay colision entre el jugador y un aliado
        if(Rect.intersects(player.getDetectCollision(),friend.getDetectCollision())){

            //Mostrando la explosion en la colision
            boom.setX(friend.getX());
            boom.setY(friend.getY());
            //Cambiando playing a falso cuando se acaba el juego
            playing = false;
            //Cambiando el valor de isGameover a true
            isGameOver = true;



            //Parando la musica del juego
            gameOnsound.stop();
            //Reproduciendo la musica de game over
            gameOversound.start();

            //Assigning the scores to the highscore integer array
            for(int i=0;i<4;i++){

                if(highScore[i]<score){

                    final int finalI = i;
                    highScore[i] = score;
                    break;
                }


            }
            //Guardando el score con shared preferences
            SharedPreferences.Editor e = sharedPreferences.edit();

            for(int i=0;i<4;i++){

                int j = i+1;
                e.putInt("score"+j,highScore[i]);
            }
            e.apply();

        }

    }


    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);



            paint.setColor(Color.WHITE);
            paint.setTextSize(20);

            for (Star s : stars) {
                paint.setStrokeWidth(s.getStarWidth());
                canvas.drawPoint(s.getX(), s.getY(), paint);
            }

            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);


            canvas.drawBitmap(
                    enemies.getBitmap(),
                    enemies.getX(),
                    enemies.getY(),
                    paint

            );

            //dibujando el score en la pantalla
            paint.setTextSize(30);
            canvas.drawText("Score:"+score,100,50,paint);


            //dibujando la imagen de la colision
            canvas.drawBitmap(
                    boom.getBitmap(),
                    boom.getX(),
                    boom.getY(),
                    paint
            );






            //Dibujando la imagen de aliado
            canvas.drawBitmap(

                    friend.getBitmap(),
                    friend.getX(),
                    friend.getY(),
                    paint
            );


            //Dibuja en la pantalla game over cuando el juego termina
            if(isGameOver){
                paint.setTextSize(150);
                paint.setTextAlign(Paint.Align.CENTER);

                int yPos=(int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
                canvas.drawText("Game Over",canvas.getWidth()/2,yPos,paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);

        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    //Para la musica al salir del juego
    public static void stopMusic(){

        gameOnsound.stop();
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {


        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                player.stopBoosting();
                break;
            case MotionEvent.ACTION_DOWN:
                player.setBoosting();
                break;

        }

        //Si el juego se acaba un toque en la pantalla nos devolvera a la pantalla principal
        if(isGameOver){

            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){

                context.startActivity(new Intent(context,MainActivity.class));

            }

        }

        return true;

    }




}

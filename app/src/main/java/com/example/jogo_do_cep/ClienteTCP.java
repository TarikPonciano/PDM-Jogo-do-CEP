package com.example.jogo_do_cep;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClienteTCP extends AppCompatActivity {
    TextView tvStatus, tvNumPìngsPongs;
    Socket clientSocket;
    DataOutputStream socketOutput;
    BufferedReader socketEntrada;
    DataInputStream socketInput;
    Button btConectar;
    EditText edtIp;
    long pings,pongs,gameState;
    TextView estadoJogo;
    EditText cepJogador, cepOponente;
    TextView cepCorreto, estadoCep, cidadeCep, ultimoCep, logradouroCep;
    String cepRecebido, cepRecente, cepSalvo;
    Button definirCep, buscarCep;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_t_c_p);
        tvStatus=findViewById(R.id.tvStatusClient);
        btConectar=findViewById(R.id.btConectar);
     //   tvNumPìngsPongs=findViewById(R.id.tvNumPP_C);
        edtIp=findViewById(R.id.edtIP);

        estadoJogo = findViewById(R.id.tvEstadoJogo);
        cepJogador= findViewById(R.id.editCepJogador);
        cepOponente = findViewById(R.id.editBuscarCep);
        cepCorreto = findViewById(R.id.tvCepCorreto);
        definirCep = findViewById(R.id.btnDefinirCep);
        buscarCep = findViewById(R.id.btnBuscarCep);
        estadoCep = findViewById(R.id.tvEstadoCep);

        cidadeCep = findViewById(R.id.tvCidadeCep);
        ultimoCep = findViewById(R.id.tvUltimoCep);
        logradouroCep = findViewById(R.id.tvLogradouroCep);


        buscarCep.setEnabled(false);


        gameState = 0;
        cepSalvo = "";
        cepRecebido = "";
        cepRecente = "";
        atualizarStatus();
    }


    public void conectar(View v) {
        final String ip=edtIp.getText().toString();
        tvStatus.setText("Conectando em "+ip+":9090");




        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {



                    clientSocket = new Socket (ip,9090);

                    tvStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Conectado com "+ip+":9090");
                        }
                    });




                    socketOutput =
                            new DataOutputStream(clientSocket.getOutputStream());
                    socketInput=
                            new DataInputStream (clientSocket.getInputStream());
                    while (socketInput!=null) {
                        String result = socketInput.readUTF();
                        if (gameState==0){
                            if (result!=null) {
                                cepRecebido = result;
                                gameState = 2;

                                atualizarStatus();
                            }

                        }else{
                            if (gameState==1){
                                if (result!=null) {
                                    cepRecebido = result;
                                    gameState = 4;

                                    atualizarStatus();
                                }


                            }
                            else{if (gameState==4){
                                if (result.compareTo("Acertei")==0) {

                                    gameState = 6;

                                    atualizarStatus();
                                }
                                if (result.compareTo("Errei")==0){
                                    gameState = 3;
                                    atualizarStatus();
                                }

                            }
                            }
                        }
                    }


                } catch (Exception e) {

                    tvStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Erro na conexão com "+ip+":9090");
                        }
                    });

                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public void mandarPing(View v){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socketOutput!=null) {
                        socketOutput.writeUTF("PING");
                        socketOutput.flush();
                        pings++;
                        atualizarStatus();
                    }else{
                        tvStatus.setText("Cliente Desconectado");
                        btConectar.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();



    }

    public void mandarCepInicial(View v){

        cepSalvo = cepJogador.getText().toString();
        Log.e("Teste", "GameState antes envio cep: "+gameState);
        Log.e("Teste", "GameState antes envio cep: "+cepSalvo);
    if(gameState==0 || gameState==2) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socketOutput != null) {

                        socketOutput.writeUTF(cepSalvo);
                        socketOutput.flush();

                        if (gameState == 0) {
                            gameState = 1;
                        } else {
                            if (gameState == 2) {
                                gameState = 4;
                            }
                        }

                        Log.i("Teste", "GameState envio cep: " + gameState);


                        atualizarStatus();
                    } else {
                        //tvStatus.setText("Cliente Desconectado");
                        // btConectar.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    }

    public void buscarCep(View v) {

        if (gameState == 3) {

            cepRecente = cepOponente.getText().toString();
             String cepFinal = cepRecente + cepCorreto.getText().toString();

            if (cepRecebido.compareTo(cepFinal)==0) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (socketOutput != null) {

                                socketOutput.writeUTF("Acertei");
                                socketOutput.flush();

                                gameState = 5;

                                Log.i("Teste", "GameState envio cep: " + gameState);

                                atualizarStatus();
                            } else {
                                //tvStatus.setText("Cliente Desconectado");
                                // btConectar.setEnabled(true);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();

            } else {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (socketOutput != null) {

                                socketOutput.writeUTF("Errei");
                                socketOutput.flush();

                                gameState = 4;

                                Log.i("Teste", "GameState envio cep: " + gameState);

                                atualizarStatus();
                            } else {
                                //tvStatus.setText("Cliente Desconectado");
                                // btConectar.setEnabled(true);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
            }
        }
    }
    public void atualizarStatus() {
        //Método que vai atualizar os pings e pongs, usando post para evitar problemas com as threads



        Log.d("Teste", "GameState antes do post: "+gameState);
        estadoJogo.post(new Runnable() {
            @Override
            public void run() {

                if(gameState==0||gameState==2){

                    estadoJogo.setText("Insira a posição da máquina do tempo");

                }
                else{

                    if(gameState==1){
                        estadoJogo.setText("Aguardando oponente inserir posição");
                    }else{


                        if(gameState==3){
                            estadoJogo.setText("É a sua vez");

                        }else{ if(gameState==4){
                            estadoJogo.setText("Aguardando o turno do Oponente");


                        }else{ if (gameState==5){
                            estadoJogo.setText("Você Venceu!!!");

                        }else{if (gameState==6){
                            estadoJogo.setText("Você Perdeu :(");
                        }
                        }

                        }
                        }}}

            }
        });

        Log.i("Teste", "GameState final atualizar: "+gameState);

        if (cepRecebido!=""&&(gameState==3||gameState==4)){
            cepCorreto.post(new Runnable() {
                                @Override
                                public void run() {
                                    String subCepRecebido = cepRecebido.substring(3);
                                    cepCorreto.setText(subCepRecebido);}
                            }
            );}

        if (!(gameState==0||gameState==2)){
            definirCep.post(new Runnable() {
                @Override
                public void run() {
                    definirCep.setEnabled(false);
                }
            });
        }

        if (gameState==4){
            if (cepRecente != ""){
                ultimoCep.post(new Runnable(){
                    @Override
                    public void run(){
                        String cepComplemento = cepCorreto.getText().toString();
                        ultimoCep.setText("Último Cep Escolhido: " + cepRecente+cepComplemento);
                    }

                });
            }
            buscarCep.post(new Runnable(){
                @Override
                public void run(){
                    buscarCep.setEnabled(false);
                }

            });
        }else{if(gameState==3){
            buscarCep.post(new Runnable(){
                @Override
                public void run(){
                    buscarCep.setEnabled(true);
                }

            });} else{if(gameState==5||gameState==6){
            buscarCep.post(new Runnable(){
                @Override
                public void run(){
                    buscarCep.setEnabled(false);
                }

            });
        }
        }
        }
        if (gameState==4){
            String cepFinal = cepOponente.getText().toString() + cepCorreto.getText().toString();
            final int cepAtual = Integer.parseInt(cepFinal);
            final int cepComparar = Integer.parseInt(cepRecebido);

            estadoCep.post(new Runnable() {
                @Override
                public void run() {
                    if (cepAtual>cepComparar){
                        estadoCep.setText("O Cep correto é menor que o inserido!");
                    }
                    else{estadoCep.setText("O Cep correto é maior que o inserido!");}
                }


            });} else{ if (gameState==5||gameState==6){

            estadoCep.post(new Runnable() {
                @Override
                public void run() {
                    estadoCep.setText("Jogo finalizado!");
                }
            });

        }
        }


    }
}
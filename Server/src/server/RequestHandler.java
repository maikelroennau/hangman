/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author 110453310
 */
public class RequestHandler extends Thread {

    private Socket socket;
    private Scanner receiver;
    private PrintWriter sender;

    RequestHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.receiver = new Scanner(this.socket.getInputStream());
        this.sender = new PrintWriter(this.socket.getOutputStream());
    }

    public void run() {

        String linha = receiver.nextLine();

        System.out.println("Requisicao Recebida: " + linha);

        if (linha.contains("LISTAARQUIVOS")) {
            this.listaArquivos(linha.replace("LISTAARQUIVOS ", ""));
        } else if (linha.contains("GETFREESPACE")) {
            this.retornaEspacoLivre();
        } else if (linha.contains("TESTARCONECCAO")) {
            getConnectionStatus();
        } else {
            sender.println("Comando Invalido");
            sender.flush();
        }

        System.out.println("Encerrando conexao: " + linha);
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void listaArquivos(String strPath) {

        File path = new File(strPath);
        if (!path.exists()) {
            sender.println("Diretorio Inexistente");
            sender.flush();
            return;
        }

        if (!path.isDirectory()) {
            sender.println("Caminho nao eh um diretorio");
            sender.flush();
            return;
        }

        String[] arquivos = path.list();
        if (arquivos.length == 0) {
            sender.println("Diretorio nao possui arquivos");
            sender.flush();
            return;
        }

        for (String arquivo : arquivos) {
            sender.println(arquivo);
        }
        sender.flush();
    }

    public void retornaEspacoLivre() {
        File arquivo = new File("/");
        sender.println(arquivo.getFreeSpace());
        sender.flush();
    }
    
    public void getConnectionStatus() {
        sender.println("CONECCAOOK");
        sender.flush();
    }
}

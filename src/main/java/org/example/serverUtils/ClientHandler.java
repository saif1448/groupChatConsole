package org.example.serverUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUserName;

    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = bufferedReader.readLine();
            clientHandlers.add(this);
            boradCastMessage("SERVER: "+clientUserName+" has entered the chat");
        } catch (IOException e) {
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String msgFromClient;
        while (socket.isConnected()){
            try {
                msgFromClient = bufferedReader.readLine();
                boradCastMessage(msgFromClient);
            } catch (IOException e) {
                e.printStackTrace();
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }

        }
    }

    public void boradCastMessage(String messageToSend){
        clientHandlers.stream()
                .filter(e -> !e.clientUserName.equals(clientUserName))
                .forEach(e -> {
                    try {
                        e.bufferedWriter.write(messageToSend);
                        e.bufferedWriter.newLine();
                        e.bufferedWriter.flush();
                    } catch (IOException ex) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        ex.printStackTrace();
                    }
                });
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        boradCastMessage("SERVER: "+clientUserName+ " has left the chat");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try {
            if(bufferedReader!= null) bufferedReader.close();
            if(bufferedWriter!=null) bufferedWriter.close();
            if(socket!=null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

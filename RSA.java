package jmaxibus1184;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;


@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class RSA extends JFrame implements ActionListener {

    public BigInteger[] my_pk = new BigInteger[2];
    public BigInteger[] kek_pk = new BigInteger[2];
    public BigInteger[] sk = new BigInteger[2];

    final File public_key = new File("public_key");
    final File private_key = new File("private_key");
    public final int size = 4096;

    final JButton generate = new JButton("generate");
    final JButton store_pk = new JButton("store pk");
    final JButton load_pk = new JButton("load pk");
    final JButton share_pk = new JButton("share pk");
    final JButton send = new JButton("send");
    final JButton receive = new JButton("receive");



    public RSA(){
        this.setVisible(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(800,500);
        this.setBounds((1920-this.getWidth())/2,(1080-this.getHeight())/2,this.getWidth(),this.getHeight());

        this.setLayout(new GridLayout(2,3));


        generate.addActionListener(this);
        store_pk.addActionListener(this);
        load_pk.addActionListener(this);
        share_pk.addActionListener(this);
        send.addActionListener(this);
        receive.addActionListener(this);

        this.add(generate);
        this.add(store_pk);
        this.add(load_pk);
        this.add(share_pk);
        this.add(send);
        this.add(receive);

        this.setVisible(true);

        //SCHEISSE LADEN
        try {

            if(!public_key.exists() || !private_key.exists()){
                switch (JOptionPane.showConfirmDialog(this,"NO KEY PAIR FOUND, GENERATE NEW ONE OR EXIT?","?",JOptionPane.YES_NO_OPTION)){
                    case JOptionPane.YES_OPTION:
                        generate();
                        JOptionPane.showMessageDialog(this,"NEW KEY PAIR GENERATED SUCCESSFULLY");
                    case JOptionPane.NO_OPTION:
                        System.exit(0);
                }
            }

            String data = null;
            Scanner myReader = new Scanner(public_key);
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
            }
            myReader.close();

            my_pk[0] = new BigInteger(data.split(",")[0]);
            my_pk[1] = new BigInteger(data.split(",")[1]);

            data = null;
            myReader = new Scanner(private_key);
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
            }
            myReader.close();

            sk[0] = new BigInteger(data.split(",")[0]);
            sk[1] = new BigInteger(data.split(",")[1]);

        }catch (Exception scheisse){
            scheisse.printStackTrace();
            System.exit(0);
        }

    }

    @Override
    public void actionPerformed(ActionEvent ev) {

        if (ev.getSource() == generate){
            if(JOptionPane.showConfirmDialog(this,"DO YOU REALLY WANNA GENERATE A NEW KEY PAIR?","?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                generate();
                JOptionPane.showMessageDialog(this,"NEW KEY PAIR GENERATED SUCCESSFULLY");
            }

        }

        if (ev.getSource() == store_pk){
            try {
                String data = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();

                String n = data.split(",")[0];
                String e = data.split(",")[1];

                String name = JOptionPane.showInputDialog(this,"NAME");

                storePk(new BigInteger(n), new BigInteger(e), name);
            }catch (Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        if (ev.getSource() == load_pk){
            String name = JOptionPane.showInputDialog(this,"name");
            loadPk(name);
        }

        if (ev.getSource() == share_pk){
            String string = my_pk[0].toString()+","+my_pk[1].toString();
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(string), null);
        }

        if (ev.getSource() == send){
            if(kek_pk.length != 0){
                JOptionPane.showMessageDialog(this,"PLEASE LOAD KEK PK FIRST!","ERROR",JOptionPane.ERROR_MESSAGE);
            }else{
                send();
            }
        }

        if (ev.getSource() == receive){
            receive();
        }
    }








    public static void main(String[] args) {
        RSA rsa = new RSA();
    }

    public void generate(){
        BigInteger p = new BigInteger("0");
        BigInteger q = new BigInteger("0");
        BigInteger e = new BigInteger("0");


        Random rand = new Random();

        p = BigInteger.probablePrime(size, rand);
        q = BigInteger.probablePrime(size, rand);
        e = BigInteger.probablePrime(size, rand);

        BigInteger n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        BigInteger d = e.modInverse(phi);

        try{
            FileWriter pk = new FileWriter(public_key);
            pk.write(n.toString());
            pk.write(",");
            pk.write(e.toString());
            pk.close();

            FileWriter sk = new FileWriter(private_key);
            sk.write(n.toString());
            sk.write(",");
            sk.write(d.toString());
            sk.close();
        }catch (Exception scheisse){
            scheisse.printStackTrace();
            System.exit(1);
        }

    }
    public void send(){

        BigInteger n = kek_pk[0];
        BigInteger e = kek_pk[1];

        String message = JOptionPane.showInputDialog(null, "MESSAGE");

        String encrypted = encrypt(n, e, message).toString();

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(encrypted), null);



    }
    public void receive(){

        BigInteger n = sk[0];
        BigInteger d = sk[1];


        BigInteger message_encrypted = null;
        try {
            message_encrypted = new BigInteger(Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        String message = decrypt(n, d, message_encrypted);

        JOptionPane.showMessageDialog(this, message, "DECRYPTED MESSAGE", JOptionPane.INFORMATION_MESSAGE);

    }

    public void storePk(BigInteger n, BigInteger e, String name){
        try {

            FileWriter pk = new FileWriter(name);
            pk.write(n.toString());
            pk.write(",");
            pk.write(e.toString());
            pk.close();
        }catch (Exception fuck){
            fuck.printStackTrace();
            System.exit(1);
        }
    }
    public void loadPk(String name){
        try {



            String data = null;
            File myObj = new File(name);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
            }
            myReader.close();

            kek_pk[0] = new BigInteger(data.split(",")[0]);
            kek_pk[1] = new BigInteger(data.split(",")[1]);




        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

    }
    public BigInteger encrypt(BigInteger n, BigInteger e, String message) {
        return stringToBi(message).modPow(e, n);
    }
    public String decrypt(BigInteger n, BigInteger d, BigInteger encryptedMessage) {
        return biToString(encryptedMessage.modPow(d, n));
    }
    public BigInteger stringToBi(String message) {
        byte[] bytes = {0};
        try{
            bytes = message.getBytes(StandardCharsets.US_ASCII);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BigInteger(bytes);
    }
    public String biToString(BigInteger bi) {
        byte[] bytes = {0};
        bytes = bi.toByteArray();
        return new String(bytes);
    }


}
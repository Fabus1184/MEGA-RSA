

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.math.*;
import java.nio.charset.*;
import java.util.*;

public class RSA extends JFrame implements ActionListener {

    public static String chars = "";
    public final int size = 4096;
    final File public_key = new File("public_key");
    final File private_key = new File("private_key");
    final JButton generate = new JButton("generate");
    final JButton store_pk = new JButton("store pk");
    final JButton load_pk = new JButton("load pk");
    final JButton share_pk = new JButton("share pk");
    final JButton send = new JButton("send");
    final JButton receive = new JButton("receive");
    final JButton sign = new JButton("sign");
    final JButton checkSign = new JButton("check signature");
    public BigInteger[] my_pk = new BigInteger[2];
    public BigInteger[] kek_pk = new BigInteger[2];
    public BigInteger[] sk = new BigInteger[2];

    public RSA() {
        this.setVisible(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(800, 500);
        this.setBounds((1920 - this.getWidth()) / 2, (1080 - this.getHeight()) / 2, this.getWidth(), this.getHeight());

        this.setLayout(new GridLayout(2, 4));

        generate.addActionListener(this);
        store_pk.addActionListener(this);
        load_pk.addActionListener(this);
        share_pk.addActionListener(this);
        send.addActionListener(this);
        receive.addActionListener(this);
        sign.addActionListener(this);
        checkSign.addActionListener(this);

        this.add(generate);
        this.add(store_pk);
        this.add(load_pk);
        this.add(share_pk);
        this.add(send);
        this.add(receive);
        this.add(sign);
        this.add(checkSign);
        
        this.setVisible(true);

        try {

            if (!public_key.exists() || !private_key.exists()) {
                switch (JOptionPane.showConfirmDialog(this, "NO KEY PAIR FOUND, GENERATE NEW ONE OR EXIT?", "?", JOptionPane.YES_NO_OPTION)) {
                    case JOptionPane.YES_OPTION:
                        generate();
                        JOptionPane.showMessageDialog(this, "NEW KEY PAIR GENERATED SUCCESSFULLY");
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

        } catch (Exception scheisse) {
            scheisse.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args) {

        for(int i=13312; i<40907; i++){
            if(i != 21325 && i != 21328) {
                chars += String.valueOf((char) i);
            }
        }

        RSA rsa = new RSA();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {

        if (ev.getSource() == generate) {
            if (JOptionPane.showConfirmDialog(this, "DO YOU REALLY WANNA GENERATE A NEW KEY PAIR?", "?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                generate();
                JOptionPane.showMessageDialog(this, "NEW KEY PAIR GENERATED SUCCESSFULLY");
            }
        }

        if (ev.getSource() == store_pk) {
            try {
                String data = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();

                String n = data.split(",")[0];
                String e = data.split(",")[1];

                String name = JOptionPane.showInputDialog(this, "NAME");

                storePk(new BigInteger(n), new BigInteger(e), name);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        if (ev.getSource() == load_pk) {
            String name = JOptionPane.showInputDialog(this, "name");
            if(name.length() > 0) {
                loadPk(name);
            }
        }

        if (ev.getSource() == share_pk) {
            String string = my_pk[0].toString() + "," + my_pk[1].toString();
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(string), null);
        }

        if (ev.getSource() == send) {
            if (kek_pk[0] == null) {
                JOptionPane.showMessageDialog(this, "PLEASE LOAD KEK PK FIRST!", "ERROR", JOptionPane.ERROR_MESSAGE);
            } else {
                send();
            }
        }

        if (ev.getSource() == receive) {
            receive();
        }
        if (ev.getSource() == sign) {
        	sign();
        }
        if (ev.getSource() == checkSign) {
        	if (kek_pk[0] == null) {
                JOptionPane.showMessageDialog(this, "PLEASE LOAD KEK PK FIRST!", "ERROR", JOptionPane.ERROR_MESSAGE);
            } else {
                checkSign();
            }
        }
    }

    public void generate() {
        BigInteger p;
        BigInteger q;
        BigInteger e;


        Random rand = new Random();

        p = BigInteger.probablePrime(size, rand);
        q = BigInteger.probablePrime(size, rand);
        e = BigInteger.probablePrime(size, rand);

        BigInteger n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        BigInteger d = e.modInverse(phi);

        try {
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
        } catch (Exception scheisse) {
            scheisse.printStackTrace();
            System.exit(1);
        }
    }

    public void send() {

        BigInteger n = kek_pk[0];
        BigInteger e = kek_pk[1];

        String message = JOptionPane.showInputDialog(null, "MESSAGE");

        if (message.length() >= 7*(size/32)) {
            JOptionPane.showMessageDialog(null,"DU KEK, DIE NACHRICHT IST ZU LANG!");

        }else {

            String encrypted = encrypt(n, e, message).toString();

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(encrypted), null);
        }


    }

    public void receive() {

        BigInteger n = sk[0];
        BigInteger d = sk[1];


        String message_encrypted = null;
        try {
            message_encrypted = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        String message = decrypt(n, d, message_encrypted);


        JOptionPane.showMessageDialog(this, message, "DECRYPTED MESSAGE", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void checkSign() {
    	BigInteger n = kek_pk[0];
    	BigInteger e = kek_pk[1];
    	
    	String signature = null;
    	String content = JOptionPane.showInputDialog(null, "CONTENT");
    	try {
            signature = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
        } catch (Exception scheiss) {
            scheiss.printStackTrace();
            System.exit(1);
        }
    	boolean sign_valid = content.equals(decrypt(n, e, signature));
    	if(sign_valid) {
    		JOptionPane.showMessageDialog(null,"The signature is correct");
    	} else {
    		JOptionPane.showMessageDialog(null,"DU KEK, DIE SIGNATUR IST FALSCH");
    	}
    }
    
    public void sign() {
    	BigInteger n = sk[0];
        BigInteger d = sk[1];
        String content = JOptionPane.showInputDialog(null, "CONTENT");
        if(content.length() >= 7*(size/32)) {
            JOptionPane.showMessageDialog(null,"DU KEK, DER INHALT IST ZU LANG!");

        }else {

            String signature = encrypt(n, d, content).toString();

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(signature), null);
        }
    }

    public void storePk(BigInteger n, BigInteger e, String name) {
        try {

            FileWriter pk = new FileWriter(name);
            pk.write(n.toString());
            pk.write(",");
            pk.write(e.toString());
            pk.close();
        } catch (Exception fuck) {
            fuck.printStackTrace();
            System.exit(1);
        }
    }

    public void loadPk(String name) {
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


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public String encrypt(BigInteger n, BigInteger e, String message) {
        return encryptedToString(stringToBi(message).modPow(e, n));
    }

    public String decrypt(BigInteger n, BigInteger d, String encryptedMessage) {
        return biToString(stringToEncrypted(encryptedMessage).modPow(d, n));
    }

    public BigInteger stringToBi(String message) {
        byte[] bytes;
        bytes = message.getBytes(StandardCharsets.UTF_8);

        Random rd = new Random();
        byte[] padding = new byte[128];
        rd.nextBytes(padding);

        byte[] result = Arrays.copyOf(bytes, bytes.length + padding.length);
        System.arraycopy(padding, 0, result, bytes.length, padding.length);


        return new BigInteger(result);
    }

    public String biToString(BigInteger bi) {
        byte[] bytes;
        bytes = bi.toByteArray();

        byte[] result = new byte[bytes.length-128];

        for (int i =0; i<bytes.length-128; i++){
            result[i] = bytes[i];
        }

        return new String(result, StandardCharsets.UTF_8);
    }

    public String encryptedToString(BigInteger encrypted) {
        String result = "";
        while (!encrypted.equals(BigInteger.ZERO)) {
            result = result + chars.charAt(encrypted.mod(BigInteger.valueOf(chars.length())).intValue());
            encrypted = encrypted.divide(BigInteger.valueOf(chars.length()));
        }
        return result;
    }

    public BigInteger stringToEncrypted(String encrypted) {
        BigInteger result = new BigInteger("0");
        for (int index = encrypted.length()-1; index>=0;index--) {
            result = result.multiply(BigInteger.valueOf(chars.length()));
            result = result.add(BigInteger.valueOf(chars.indexOf(encrypted.charAt(index))));
        }
        return result;
    }
}
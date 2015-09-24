package StegoTag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 *  Programmer:     Richard Lewis
 *  Class:          COSC 620
 *  Assignment:     Final Project
 *  Due Date:       12/14/2011
 *  Date Submitted: 12/14/2011
 *  File Name:      Extractor.java
 *  Other Files     StegoTag.java, Embedder.java, SDES.java
 *  Description:    This class extracts an encrypted message from an HTML file
 *                  and decrypts it using simplified DES
 */

public class Extractor
{
    // instance variables
    String inFile, outFile, keyString;
    ArrayList text = new ArrayList();
    ArrayList text2 = new ArrayList();

    // constructor
    public Extractor(String in, String out, String ks)
    {
        inFile = in;
        outFile = out;
        keyString = ks;
    }

    // opens the html file containing the message read into an arraylist
    private void openFile()
    {
        try {

            // use a Scanner to parse file
            Scanner sf = new Scanner(new File(inFile));
            while (sf.hasNext()) {
                text.add(sf.nextLine()); // write each line to an arrayList
            }
            sf.close(); // close the Scanner
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Embedder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // method to extract the encoded message
    public String retrieve() throws FileNotFoundException, IOException
    {
        // Scanner to parse the file
        Scanner sf2 = new Scanner(new File(inFile));
        while(sf2.hasNext())
        {
            text2.add(sf2.nextLine());
        }
        sf2.close(); // close scanner
        String codeString = ""; // initialize codestring with empty string
        // loop through the arrayList
        for(int y = 0; y < text2.size(); y++)
        {
            // cast arraylist object to a string
            String textLine2 = (String) text2.get(y);
            // convert string to a character array
            char ch2[] = textLine2.toCharArray();
            // loop through the character array
            for(int z = 0; z < ch2.length; z++)
            {
                if(ch2[z] == '>') // find chaacter >
                {
                    // check previous character for a space
                    if(ch2[z-1] == ' ')
                    {
                        // if space add a 1 to codestring
                        codeString = codeString + 1;
                    }
                    else
                    {
                        // if not space add a 0 to codestring
                        codeString = codeString + 0;
                    }
                }
            }

        }
        // call to decrypt method
        String message = this.decryption(codeString);
        return message;
    }

    // method to decrypt the extracted message
    private String decryption(String cs) throws IOException
    {
        String message=""; // initialize message with empty string
        // convert key string into an integer
        int key = Integer.parseInt(keyString,2);
        // instantiate a SDES object
        SDES decoder = new SDES(key);
        // calculate the last byte
        int lastByte = cs.length() - (cs.length() %8);
        // loop through each byte
        for(int x = 0; x < lastByte; x += 8)
        {
            // get the binary representation of a byte
            String codedLetter = cs.substring(x, x + 8);
            // convert string into an integer
            int letter = Integer.parseInt(codedLetter, 2);
            // // call decode method on the byte
            int decodedLetter = decoder.decrypt(letter);
            // add decoded letter to the message
            message = message + (char)decodedLetter;
        }
        // call to method thar writes the message to a file
        this.createFile(message);

        return message;

    }

    // method to write the message to a file
    private void createFile(String msg) throws IOException
    {
        // create a filewriter object
        FileWriter fw = new FileWriter(outFile);
        // create a printwriter object
        PrintWriter pw = new PrintWriter(fw);
        pw.println(msg); //write to the file
        pw.close(); // close printwriter
        fw.close(); // close filewriter
    }
}
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
 *  File Name:      Embedder.java
 *  Other Files     StegoTag.java, Extractor.java, SDES.java
 *  Description:    This class embeds an encrypted message into an HTML file
 *                  using simplified DES
 */

public class Embedder {
    // instance variables
    ArrayList text = new ArrayList();
    String inFile, outFile, keyString, message, maxChar;

    // constructor
    public Embedder(String in, String out, String ks, String msg, String size)
    {
        inFile = in;
        outFile = out;
        keyString = ks;
        message = msg;
        maxChar = size;
    }

    // opens a file and read it into an arrayList
    public void openFile(String file)
    {
        try {

            // use a scanner to parse a file
            Scanner sf = new Scanner(new File(file));
            while (sf.hasNext()) {
                text.add(sf.nextLine());
            }
            sf.close(); // close scanner
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Embedder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // converts a string of characters into a string of bits(1's and 0's)
    private String getBits(String message)
    {
        // convert string into an array of bytes
        byte[] bytes = message.getBytes();
        // create a stringbuilder
        StringBuilder binary = new StringBuilder();
        // loop through each byte
        for (byte b : bytes)
        {
            // get integer value of byte
            int val = b;
            // loop through each bit
            for (int i = 0; i < 8; i++)
            {
                // add the 1 or 0 to the stringbuilder
                binary.append((val & 128) == 0 ? 0 : 1);
                // bit shift left one place
                val <<= 1;
            }
        }
        // convert to a string
        String binaryMessage = binary.toString();
        return binaryMessage;

    }

    /*
     * takes a String of characters and coverts it to a binary string
     * encrypts the binary message using SDES class
     * returns the encrypted binay String
     */
    public String encryption()
    {
      String encryptedMessage = ""; // initialize with empty string
      // convert key string into an integer
      int key = Integer.parseInt(keyString,2);
      // call to method to convert message into bit representation
      String bMessage = this.getBits(message);
      // convert the string representation of maximum # of characters to an integer
      int size = Integer.parseInt(maxChar);
      // calculate  how many bytes need to be padded on message
      int padding = size - (bMessage.length() / 8);
      for(int y = 0; y < padding; y++)
      {
          // pad message with a byte of all 0's
          bMessage = bMessage + "00000000";
      }
      // create a SDES object to encrypt message
      SDES encoder = new SDES(key);
      //loop through each byte of message
      for(int x = 0; x < bMessage.length(); x+=8)
      {
        // get each byte
        String letter = bMessage.substring(x, x + 8);
        // convert string letter to an integer
        int binaryLetter = Integer.parseInt(letter,2);
        // encrypt each byte using SDES object
        int newBinaryLetter = encoder.encrypt(binaryLetter);
        // format encrypted byte to have leading 0's
        encryptedMessage = encryptedMessage + String.format("%8s", Integer.toBinaryString(newBinaryLetter)).replace(" ", "0");
      }
      return encryptedMessage;

    }

    //Embeds an encrypted message into an HTML file
    public String hide(String eMessage) throws IOException
    {
        // create a filewriter
        FileWriter fw = new FileWriter(outFile);
        // create a printwriter
        PrintWriter pw = new PrintWriter(fw);
        int counter = 0; // initialize counter to 0
        String newLine = ""; // initialize newline with the empty string
        // loop through the arraylist
        for (int y = 0; y < text.size(); y++)
        {
            // cast arrylist object to a string
            String textLine = (String) text.get(y);
            // convert string to a character array
            char[] ch = textLine.toCharArray();
            int added = 0; // initialize added to 0
                // loop throuch character array
                for (int z = 0; z < ch.length; z++)
                {
                    // check if counter is less than the length of the message
                    if (counter < eMessage.length())
                    {
                        // test if the character is a >
                        if (ch[z] == '>')
                        {
                            if (eMessage.charAt(counter) == '1')
                            {
                                // if current bit is is 1 insert a space
                                textLine = textLine.substring(0, (z + added)) + " " + textLine.substring(z + added);
                                added++; // increment added
                                counter++; // increment counter
                            }
                            else
                            {
                                // if current bit is 0 do nothing
                                counter++; //increment counter
                                continue; // move to next itteration
                            }
                        }
                    }
                }
            pw.println(textLine); // write the line to the new file

        }
        pw.close(); // close printwriter
        fw.close(); // close filewriter
        return "Message has been embedded";
    }
}
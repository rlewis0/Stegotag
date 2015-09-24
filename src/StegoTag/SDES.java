package StegoTag;

/*
 *  Programmer:     Richard Lewis
 *  Class:          COSC 620
 *  Assignment:     Final Project
 *  Due Date:       12/14/2011
 *  Date Submitted: 12/14/2011
 *  File Name:      SDES.java
 *  Other Files     StegoTag.java, Extractor.java, Embedder.java
 *  Description:    This class encypts and decrypts binary Strings
 *                  using simplified DES
 */
class SDES {

    // constructor
    public SDES(int k)
    {
        key = k;
        createSubKeys(key);
    }

    //method to create the sub keys
    private void createSubKeys(int origKey) {
        //permute the key
        origKey = permute(origKey, P10, 10);

        //split key in half
        int firstHalf = (origKey >> 5) & 31;
        int secondHalf = origKey & 31;

        //apply a circular left shift 1 place to each half
        firstHalf = ((firstHalf & 15) << 1) | ((firstHalf & 16) >> 4);
        secondHalf = ((secondHalf & 15) << 1) | ((secondHalf & 16) >> 4);

        //permute 8 of the 10 bits to create key 1
        key1 = permute((firstHalf << 5) | secondHalf, P8, 10);

        //apply a circular left shift 2 place to each half
        firstHalf = ((firstHalf & 7) << 2) | ((firstHalf & 24) >> 3);
        secondHalf = ((secondHalf & 7) << 2) | ((secondHalf & 24) >> 3);

        //permute 8 of the 10 bits to create key 2
        key2 = permute((firstHalf << 5) | secondHalf, P8, 10);

    }

    //method to encrypt plain text
    public int encrypt(int plainText) {
        //initial permutation
        int output = permute(plainText, IP, 8);

        //function fk with key 1
        output = functionK(output, key1);

        //swap halves
        output = swap(output);

        //function fk with key 2
        output = functionK(output, key2);

        //initial permutation inverse
        output = permute(output, IPI, 8);

        return output;
    }

    //method to decrypt cipher text
    public int decrypt(int cipherText) {
        //initial permutation
        int output = permute(cipherText, IP, 8);

        //function fk with key 2
        output = functionK(output, key2);

        //swap halves
        output = swap(output);

        //function fk with key 1
        output = functionK(output, key1);

        //initial permutation inverse
        output = permute(output, IPI, 8);
        return output;
    }

    //method to apply function f sub k
    private int functionK(int input, int subKey) {
        //split text into halves
        int left = (input >> 4) & 15;
        int right = input & 15;

        //xor left side with right side mapped to the s-boxes using sub key
        //then combine the halves
        return ((left ^ mapping(right, subKey)) << 4) | right;
    }

    //method to map to S-boxes
    private int mapping(int right, int subKey) {
        //expand and permute the right half
        int p = permute(right, EP, 4) ^ subKey;

        //split expanded half into halves again
        int pLeft = (p >> 4) & 15;
        int pRight = p & 15;

        //1st and 4th bit represent row of s-box
        //2nd and 3rd bir represent column of s-box
        pLeft = S0[((pLeft & 8) >> 2) | (pLeft & 1)][(pLeft >> 1) & 3];
        pRight = S1[((pRight & 8) >> 2) | (pRight & 1)][(pRight >> 1) & 3];

        //combine s-box output and permute
        p = permute((pLeft << 2) | pRight, P4, 4);
        return p;
    }

    //method to permute the bits according to selected table
    private int permute(int num, int[] permuteTable, int maxPos) {
        int pos = 0; //initialize bit position

        //loop through selected permute table
        for (int x = 0; x < permuteTable.length; x++) {
            pos = pos << 1; //shift 1 bit position to the left

            //shift to proper position, and with 1 to place bit
            pos = pos | (num >> (maxPos - permuteTable[x])) & 1;
        }
        return pos;
    }

    //method to swap right and left halves
    private int swap(int input) {
        //shift bits to swap halves
        return ((input & 15) << 4) | ((input >> 4) & 15);
    }
    //instance variables
    private int key, key1, key2;
    //constants for permute tables and s-boxes
    private final int P10[] = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    private final int P8[] = {6, 3, 7, 4, 8, 5, 10, 9};
    private final int P4[] = {2, 4, 3, 1};
    private final int IP[] = {2, 6, 3, 1, 4, 8, 5, 7};
    private final int IPI[] = {4, 1, 3, 5, 7, 2, 8, 6};
    private final int EP[] = {4, 1, 2, 3, 2, 3, 4, 1};
    private final int S0[][] = {
        {1, 0, 3, 2},
        {3, 2, 1, 0},
        {0, 2, 1, 3},
        {3, 1, 3, 2}
    };
    private final int S1[][] = {
        {0, 1, 2, 3},
        {2, 0, 1, 3},
        {3, 0, 1, 0},
        {2, 1, 0, 3}
    };
}

package de.mindscan.furiousiron.hfb.io;

import org.junit.jupiter.api.Test;

public class BitwiseCalculationsTest {

    @Test
    public void testGetByteWeights_calculateMatrix_expectCodeOuptut() throws Exception {
        int[] hexdigitweights = BitwiseCalculations.getHexDigitWeight();

        for (int j = 0; j < 16; j++) {

            StringBuffer commentLine = new StringBuffer();
            commentLine.append( "//" );
            for (int i = 0; i < 16; i++) {
                commentLine.append( String.format( " 0x%x%x", j, i ) );
            }

            System.out.println( commentLine.toString() );

            StringBuffer codeLine = new StringBuffer();
            commentLine.append( "  " );

            for (int i = 0; i < 16; i++) {
                int byteweight = hexdigitweights[j] + hexdigitweights[i];
                codeLine.append( String.format( "   %d,", byteweight ) );
            }

            codeLine.append( " //" );
            System.out.println( codeLine.toString() );
        }
    }

}

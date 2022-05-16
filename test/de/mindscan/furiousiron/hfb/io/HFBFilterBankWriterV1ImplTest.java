package de.mindscan.furiousiron.hfb.io;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.mindscan.furiousiron.hfb.HFBFilterBank;

public class HFBFilterBankWriterV1ImplTest {

    @TempDir
    Path tempDir;

    @Test
    public void testWrite() throws Exception {
        int number_of_documents = 13332;

        // arrange
        HFBFilterBankWriterV1Impl writer = new HFBFilterBankWriterV1Impl();
        HFBFilterBank filterBank = new HFBFilterBank();
        filterBank.initFilters( 128, number_of_documents, 2 );

        Collection<BigInteger> documents = getDocumentIdCollection( 0xbadface1, number_of_documents );
        filterBank.addDocumentIds( documents );

        // tempDir.resolve( "filterbank.hfbv1" ).toString()
        // act
        writer.write( filterBank, "D:\\myfirstFilterbank.hfbv1" );

        // assert
        // cpxuas
    }

    Collection<BigInteger> getDocumentIdCollection( long seed, int count ) {
        Collection<BigInteger> result = new HashSet<>();

        Random random = new Random( seed );

        for (int i = 0; i < 13332; i++) {
            byte[] target = new byte[16];
            random.nextBytes( target );
            result.add( new BigInteger( target ) );
        }

        return result;
    }

}

package de.mindscan.furiousiron.hfb.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.junit.jupiter.api.Test;

import de.mindscan.furiousiron.hfb.HFBFilterBank;
import de.mindscan.furiousiron.hfb.HFBFilterBankReader;

public class HFBFilterBankReaderV1ImplTest {

    @Test
    public void testReadFromFile() throws Exception {
        int number_of_documents = 13332;

        // arrange
        HFBFilterBankReader reader = new HFBFilterBankReaderV1Impl();
        Collection<BigInteger> documentCollection = getDocumentIdCollection( 0xbadface1, number_of_documents );

        // act
        HFBFilterBank filterbank = reader.readFromFile( "D:\\myfirstFilterbank.hfbv1" );

        // assert all 13332 elements are listed in the filterbank
        for (BigInteger documentId : documentCollection) {
            boolean isContained = filterbank.containsDocumentId( documentId );
            assertThat( isContained, equalTo( true ) );
        }
    }

    @Test
    public void testReadFromFile_OneMoreButNotIncludedDocument_fails() throws Exception {
        int number_of_documents = 13333;

        // arrange
        HFBFilterBankReader reader = new HFBFilterBankReaderV1Impl();
        Collection<BigInteger> documentCollection = getDocumentIdCollection( 0xbadface1, number_of_documents );

        // act
        HFBFilterBank filterbank = reader.readFromFile( "D:\\myfirstFilterbank.hfbv1" );

        // assert all 13332 elements are listed in the filterbank
        for (BigInteger documentId : documentCollection) {
            boolean isContained = filterbank.containsDocumentId( documentId );
            assertThat( isContained, equalTo( true ) );
        }
    }

    Collection<BigInteger> getDocumentIdCollection( long seed, int count ) {
        Collection<BigInteger> result = new HashSet<>();

        Random random = new Random( seed );

        for (int i = 0; i < count; i++) {
            byte[] target = new byte[16];
            random.nextBytes( target );
            result.add( new BigInteger( target ) );
        }

        return result;
    }

}

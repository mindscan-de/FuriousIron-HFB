package de.mindscan.furiousiron.hfb.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
    public void testReadFromFile_OneMoreDocumentButNotIncludedDocument_documentIdReportedMissing() throws Exception {
        int number_of_documents = 13332;

        // arrange
        HFBFilterBankReader reader = new HFBFilterBankReaderV1Impl();
        Set<BigInteger> documentCollection = getDocumentIdCollection( 0xbadface1, number_of_documents );
        Set<BigInteger> documentCollectionAndOneMore = getDocumentIdCollection( 0xbadface1, number_of_documents + 1 );

        documentCollectionAndOneMore.removeAll( documentCollection );

        // act
        HFBFilterBank filterbank = reader.readFromFile( "D:\\myfirstFilterbank.hfbv1" );

        // Test that the single extra document is reported as non existent.
        for (BigInteger documentId : documentCollectionAndOneMore) {
            boolean isContained = filterbank.containsDocumentId( documentId );
            assertThat( isContained, equalTo( false ) );
        }

    }

    Set<BigInteger> getDocumentIdCollection( long seed, int count ) {
        Set<BigInteger> result = new HashSet<>();

        Random random = new Random( seed );

        for (int i = 0; i < count; i++) {
            byte[] target = new byte[16];
            random.nextBytes( target );
            result.add( new BigInteger( target ) );
        }

        return result;
    }

}

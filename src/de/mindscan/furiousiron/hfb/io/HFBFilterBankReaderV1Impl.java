/**
 * 
 * MIT License
 *
 * Copyright (c) 2022 Maxim Gansert, Mindscan
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */
package de.mindscan.furiousiron.hfb.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.mindscan.furiousiron.hfb.HFBFilterBank;
import de.mindscan.furiousiron.hfb.HFBFilterBankReader;
import de.mindscan.furiousiron.hfb.HFBFilterData;

/**
 * 
 */
public class HFBFilterBankReaderV1Impl implements HFBFilterBankReader {

    /** 
     * {@inheritDoc}
     */
    @Override
    public HFBFilterBank readFromFile( String filePath ) {
        try (BufferedReader reader = Files.newBufferedReader( Paths.get( filePath ) )) {

            // TODO expect "HFB.v1" (6 Bytes),
            // (0x00,0x00) 2 bytes)

            HFBFilterBank filterBank = new HFBFilterBank();

            // bits in documentId (4 bytes) 
            int bitsInDocumentId = 128;

            // number of documents in filter (8 bytes)
            long occurenceCount = 10000L;

            // loadfactor (4 bytes)
            int loadFactor = 5;

            int numberOfFilters = 3;

            // TODO: do initialization. / shaping of the filterbank 
            filterBank.initFilters( bitsInDocumentId, occurenceCount, loadFactor );

            // 
            for (int i = 0; i < numberOfFilters; i++) {
                // read current filterbank position
                // read current filterbank data
                // read more stuff.
                int current_filterbankIndex = 0;

                HFBFilterData filterData = filterBank.getFilterData( current_filterbankIndex );

                // verify with read data

                filterData.setSliceData( new byte[0] );
            }

        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO Auto-generated method stub
        return null;
    }

}

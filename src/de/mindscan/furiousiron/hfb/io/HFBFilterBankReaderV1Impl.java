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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.mindscan.furiousiron.hfb.HFBFilterBank;
import de.mindscan.furiousiron.hfb.HFBFilterBankReader;

/**
 * 
 */
public class HFBFilterBankReaderV1Impl implements HFBFilterBankReader {

    /** 
     * {@inheritDoc}
     */
    @Override
    public HFBFilterBank readFromFile( String filePath ) {
        try (InputStream reader = Files.newInputStream( Paths.get( filePath ) )) {

            byte[] hfb_header_buffer = reader.readNBytes( 28 );

            boolean isHFB = RawUtils.isMarker4b( hfb_header_buffer, 0, HFBFilterBankWriterV1Impl.INT_HFB_MARKER );
            boolean isV1 = RawUtils.isMarker4b( hfb_header_buffer, 4, HFBFilterBankWriterV1Impl.INT_V1_MARKER );

            if (!isHFB) {
                throw new FileFormatException( "This is not a HFB-File." );
            }

            if (!isV1) {
                throw new FileFormatException( "Can't read this particular version of the HFBFile." );
            }

            HFBFilterBank filterBank = new HFBFilterBank();
            int bitsInDocumentId = RawUtils.toUnsignedInt4b( hfb_header_buffer, 8 );
            long occurrenceCount = RawUtils.toUnsignedLong8b( hfb_header_buffer, 12 );
            int loadFactor = RawUtils.toUnsignedInt4b( hfb_header_buffer, 20 );

            filterBank.initFilters( bitsInDocumentId, occurrenceCount, loadFactor );

            int numberOfFilters = RawUtils.toUnsignedInt4b( hfb_header_buffer, 24 );

            for (int filterID = 0; filterID < numberOfFilters; filterID++) {

            }

//            // 
//            for (int i = 0; i < numberOfFilters; i++) {
//                // read current filterbank position
//                // read current filterbank data
//                // read more stuff.
//                int current_filterbankIndex = 0;
//
//                HFBFilterData filterData = filterBank.getFilterData( current_filterbankIndex );
//
//                // verify with read data
//
//                filterData.setSliceData( new byte[0] );
//            }

            return filterBank;
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO Auto-generated method stub
        return null;
    }

}

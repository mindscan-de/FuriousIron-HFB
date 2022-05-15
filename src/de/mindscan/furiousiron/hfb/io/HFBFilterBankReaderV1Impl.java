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
import de.mindscan.furiousiron.hfb.HFBFilterData;

/**
 * 
 */
public class HFBFilterBankReaderV1Impl implements HFBFilterBankReader {

    private static final int HFB_MARKER = HFBFilterBankWriterV1Impl.HFB_MARKER;
    private static final int HFB_V1_MARKER = HFBFilterBankWriterV1Impl.HFB_V1_MARKER;
    private static final int HFB_FILTERDATA_MARKER_UNCOMPRESSED = HFBFilterBankWriterV1Impl.HFB_FILTERDATA_MARKER;

    /** 
     * {@inheritDoc}
     */
    @Override
    public HFBFilterBank readFromFile( String filePath ) {
        try (InputStream reader = Files.newInputStream( Paths.get( filePath ) )) {

            byte[] hfb_header_buffer = reader.readNBytes( 28 );

            boolean isHFB = RawUtils.isMarker4b( hfb_header_buffer, 0, HFB_MARKER );
            boolean isV1 = RawUtils.isMarker4b( hfb_header_buffer, 4, HFB_V1_MARKER );

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

            filterBank.initFiltersLazy( bitsInDocumentId, occurrenceCount, loadFactor );

            int numberOfFilters = RawUtils.toUnsignedInt4b( hfb_header_buffer, 24 );

            for (int filterID = 0; filterID < numberOfFilters; filterID++) {
                readFilterBankData( reader, filterBank );
            }

            return filterBank;
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO Auto-generated method stub
        return null;
    }

    private void readFilterBankData( InputStream reader, HFBFilterBank filterBank ) throws IOException {
        byte[] filter_data_header_buffer = reader.readNBytes( 20 );

        if (!RawUtils.isMarker4b( filter_data_header_buffer, 0, HFB_FILTERDATA_MARKER_UNCOMPRESSED )) {
            throw new FileFormatException( "Can't decode filter bank data. Marker unknown." );
        }

        int slicePosition = RawUtils.toUnsignedInt4b( filter_data_header_buffer, 8 );
        int sliceBitSize = RawUtils.toUnsignedInt4b( filter_data_header_buffer, 12 );

        HFBFilterData hfbdata = new HFBFilterData( slicePosition, sliceBitSize );

        int filterDataLength = RawUtils.toUnsignedInt4b( filter_data_header_buffer, 16 );

        byte[] filterDataArray = reader.readNBytes( filterDataLength );
        hfbdata.setSliceData( filterDataArray );

        filterBank.addFilterData( hfbdata );
    }

}

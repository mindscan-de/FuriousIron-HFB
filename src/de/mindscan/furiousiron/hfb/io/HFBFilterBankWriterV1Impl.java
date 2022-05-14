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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import de.mindscan.furiousiron.hfb.HFBFilterBank;
import de.mindscan.furiousiron.hfb.HFBFilterBankWriter;
import de.mindscan.furiousiron.hfb.HFBFilterData;

/**
 * 
 */
public class HFBFilterBankWriterV1Impl implements HFBFilterBankWriter {

    public final static String FILE_SUFFIX = "hfbv1";
    public final static String FILE_DOT_SUFFIX = ".hfbv1";

    // 'HFB.'
    public final static int HFB_MARKER = 0x4846422e;
    // 'v1', 0x00, 0x00
    public final static int HFB_V1_MARKER = 0x76310000;

    /** 
     * {@inheritDoc}
     */
    @Override
    public void write( HFBFilterBank filterBank, String outputPath ) {

        if (!outputPath.endsWith( FILE_DOT_SUFFIX )) {
            outputPath = outputPath + FILE_DOT_SUFFIX;
        }

        try (OutputStream writer = Files.newOutputStream( Paths.get( outputPath ), StandardOpenOption.TRUNCATE_EXISTING )) {

            // write HFB Marker Header -- 4 bytes
            writer.write( RawUtils.toByteArray4b( HFB_MARKER ) );
            // write HFB Version Information -- 4 bytes
            writer.write( RawUtils.toByteArray4b( HFB_V1_MARKER ) );

            // [option 2: is writing the chosen slice size ]
            // [option 2: this would be more future proof if allocation mechanism changes ]
            // [option 2: number of documents still interesting, for further optimizations ]

            // write GetBitsInDocumentId -- 4bytes
            writer.write( RawUtils.toByteArray4b( filterBank.getBitsInDocumentId() ) );
            // write Number of occurrences / number of documents -- 8 bytes
            writer.write( RawUtils.toByteArray8b( filterBank.getOccurrenceCount() ) );
            // write spread factor / load factor -- 4 bytes
            writer.write( RawUtils.toByteArray4b( filterBank.getLoadFactor() ) );

            // now write the number of preserved/serialized filterdata
            // currently we will save all of them.
            writer.write( RawUtils.toByteArray4b( filterBank.getNumberOfFilters() ) );
            for (int filterID = 0; filterID < filterBank.getNumberOfFilters(); filterID++) {
                // TODO: write filterdataheader

                HFBFilterData filterData = filterBank.getFilterData( filterID );
                // TODO: write filterdata configuration
                // TODO: write length of filterdata in bytes
                // TODO: write filterdata of current filterID 
            }

            writer.flush();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//            // we want to write the number of bits
//            // we want to write the 
//            //current numberof filterbank
//
//            int slicePosition = filterData.getSlicePosition();
//            int sliceBitSize = filterData.getSliceBitSize();
//            byte[] sliceData = filterData.getSliceData();
//
//            // also add marker and length data.
//            // TODO write the index and the position, the sliced bits and the slicedata (also the number of bytes)

    }

}

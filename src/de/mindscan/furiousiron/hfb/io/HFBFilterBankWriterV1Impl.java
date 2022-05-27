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
import java.util.List;

import de.mindscan.furiousiron.hfb.HFBFilterBank;
import de.mindscan.furiousiron.hfb.HFBFilterBankWriter;
import de.mindscan.furiousiron.hfb.HFBFilterData;
import de.mindscan.furiousiron.hfb.options.HFBFilterWriteOption;

/**
 * 1st MVP: save full hfb-filter-bank with full filter data to disk [DONE]
 * 
 * 2nd MVP: save sparse hfb-filter-banks to disk (save only those 3 or 4 with the fewest set bits in the filterdata)
 *          and adapt the filter bank to work on sparse filter data. sparse filter data is faster to load (lower IO)
 *          and faster to filter, and randomizes the bit positions, so that different portions of documentid are 
 *          matched, which will lead to a more consistent document drop-out rate. The randomized and more consistent
 *          drop out, will then remove documents earlier from the candidate list, saving time via non spend cpu cycles  
 */
public class HFBFilterBankWriterV1Impl implements HFBFilterBankWriter {

    public final static String FILE_SUFFIX = "hfbv1";
    public final static String FILE_DOT_SUFFIX = ".hfbv1";

    // 'HFB.'
    public final static int HFB_MARKER = 0x4846422e;
    // 'v1', 0x00, 0x00
    public final static int HFB_V1_MARKER = 0x76310000;
    // 'FDv1' - Uncompressed filter data
    public final static int HFB_FILTERDATA_MARKER_UNCOMPRESSED = 0x46447631;

    /** 
     * {@inheritDoc}
     */
    @Override
    public void write( HFBFilterBank filterBank, String outputPath ) {
        this.write( filterBank, outputPath, HFBFilterWriteOption.SAVE_ALL_FILTERBANKS, HFBFilterWriteOption.ORDER_BY_STARTPOSITION );
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void write( HFBFilterBank filterBank, String outputPath, HFBFilterWriteOption... options ) {
        if (!outputPath.endsWith( FILE_DOT_SUFFIX )) {
            outputPath = outputPath + FILE_DOT_SUFFIX;
        }

        try (OutputStream writer = Files.newOutputStream( Paths.get( outputPath ), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING )) {

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

            // Filter
            // TODO: Order (efficiency, start position, random)
            List<Object> orderedFilterbanks = orderFilterBanks( filterBank, options );

            // TODO: Limit (ALL, FOUR, Three, ((TODO: HALF the filters, Third the filters, Below one percent (related to order), below half percent, below one per mille)))
            List<Object> filteredOrdered = filterFilterBanks( orderedFilterbanks, options );

            // TODO: provide order for the filter banks, and maybe selection of the filter banks, by separate Collection 
            writeAllFilterbanks( filterBank, writer );

            writer.flush();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param filterBank
     * @param options
     * @return
     */
    private List<Object> orderFilterBanks( HFBFilterBank filterBank, HFBFilterWriteOption[] options ) {
        // TODO Auto-generated method stub

        // TODO: random
        // TODO: start position
        // if none of them then start_position

        // use that seed to figure out the filters by
        // TODO: if efficiency, then reorder them by efficiency,
        //       but keep same order for equal  

        return null;
    }

    /**
     * @param orderedFilterbanks
     * @param options
     * @return
     */
    private List<Object> filterFilterBanks( List<Object> orderedFilterbanks, HFBFilterWriteOption[] options ) {
        // TODO Auto-generated method stub
        // TODO: Test for FOUR, Three, Half, ALL

        // TODO: None of these -> return all of them...
        return orderedFilterbanks;
    }

    private void writeAllFilterbanks( HFBFilterBank filterBank, OutputStream writer ) throws IOException {
        // now write the number of preserved/serialized filterdata
        // currently we will save all of them.
        writer.write( RawUtils.toByteArray4b( filterBank.getNumberOfFilters() ) );
        for (int filterID = 0; filterID < filterBank.getNumberOfFilters(); filterID++) {
            writeFilterBankData( filterBank, writer, filterID );
        }
    }

    private void writeFilterBankData( HFBFilterBank filterBank, OutputStream writer, int filterID ) throws IOException {
        HFBFilterData filterData = filterBank.getFilterData( filterID );

        writer.write( RawUtils.toByteArray4b( HFB_FILTERDATA_MARKER_UNCOMPRESSED ) );
        writer.write( RawUtils.toByteArray4b( filterID ) );

        writer.write( RawUtils.toByteArray4b( filterData.getSlicePosition() ) );
        writer.write( RawUtils.toByteArray4b( filterData.getSliceBitSize() ) );

        System.out.println( filterData.getSliceBitSize() );
        System.out.println( 1L << filterData.getSliceBitSize() );

        byte[] filterDataArray = filterData.getSliceData();
        writer.write( RawUtils.toByteArray4b( filterDataArray.length ) );
        writer.write( filterDataArray );

        int bitweight = BitwiseCalculations.calculateBitWeight( filterDataArray );
        int invbitweight = BitwiseCalculations.calculateInvBitWeight( filterDataArray );
        System.out.println( String.format( "Filter #%d has %d bits set (1) and %d not set (0).", filterID, bitweight, invbitweight ) );
    }

}

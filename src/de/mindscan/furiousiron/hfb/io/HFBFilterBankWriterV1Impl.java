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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.mindscan.furiousiron.hfb.HFBFilterBank;
import de.mindscan.furiousiron.hfb.HFBFilterBankWriter;
import de.mindscan.furiousiron.hfb.HFBFilterData;
import de.mindscan.furiousiron.hfb.options.HFBFilterWriteOption;

/**
 * This HFB writer implements an uncompressed hfb filter writer. It can save the HFB Filter according to different
 * options. 
 * 
 * - save full hfb-filter-bank with full filter data to disk [DONE]
 * - save sparse hfb-filter-banks to disk (save only those 3 or 4 with the fewest set bits in the filterdata) [DONE]
 * - save most efficient hfb-filter-banks only
 * - sparse filter data is faster to load (lower I/O) and faster to filter, 
 * - randomize the bit positions, so that different portions of documentid are matched, which will lead to a more 
 *   consistent document drop-out rate. The randomized and more consistent drop out, will then remove documents 
 *   earlier from the candidate list, saving time via non spend cpu cycles  
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

            // write number of bits in DocumentId -- 4bytes
            writer.write( RawUtils.toByteArray4b( filterBank.getBitsInDocumentId() ) );
            // write Number of occurrences / number of documents -- 8 bytes
            writer.write( RawUtils.toByteArray8b( filterBank.getOccurrenceCount() ) );
            // write spread factor / load factor -- 4 bytes
            writer.write( RawUtils.toByteArray4b( filterBank.getLoadFactor() ) );

            List<HFBFilterBankStats> orderedSelection = calculateHFBFilterBankOrder( filterBank, options );

            writeFilterbanksByOrder( filterBank, writer, orderedSelection );

            writer.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<HFBFilterBankStats> calculateHFBFilterBankOrder( HFBFilterBank filterBank, HFBFilterWriteOption... options ) {
        Set<HFBFilterWriteOption> optionSet = convertOptionsToSet( options );
        return filterFilterBanks( orderFilterBanks( filterBank, optionSet ), optionSet );
    }

    private Set<HFBFilterWriteOption> convertOptionsToSet( HFBFilterWriteOption... options ) {
        return options == null ? new HashSet<>() : Arrays.stream( options ).collect( Collectors.toSet() );
    }

    private List<HFBFilterBankStats> orderFilterBanks( HFBFilterBank filterBank, Set<HFBFilterWriteOption> optionSet ) {
        List<HFBFilterBankStats> result = new ArrayList<>();
        for (int i = 0; i < filterBank.getNumberOfFilters(); i++) {
            result.add( new HFBFilterBankStats( filterBank.getFilterData( i ), i,
                            BitwiseCalculations.calculateBitWeight( filterBank.getFilterData( i ).getSliceData() ) ) );
        }

        if (optionSet.contains( HFBFilterWriteOption.ORDER_BY_RANDOM )) {
            Collections.shuffle( result );
        }
        else if (optionSet.contains( HFBFilterWriteOption.ORDER_BY_STARTPOSITION )) {
            // intentionally left blank - keep order of initialization
        }
        else {
            // intentionally left blank - keep order of initialization
        }

        if (optionSet.contains( HFBFilterWriteOption.ORDER_BY_EFFICIENCY )) {
            // resort but keep former order in case of same weights.
            // as long as the filter data always has the same slice size (within one filter, Bitweight is correct, othwerwise bitweight in relation to slice size is important.
            Collections.sort( result, Comparator.comparingLong( HFBFilterBankStats::getBitweight ) );
        }

        return result;
    }

    private List<HFBFilterBankStats> filterFilterBanks( List<HFBFilterBankStats> orderedFilterbanks, Set<HFBFilterWriteOption> optionSet ) {
        if (optionSet.contains( HFBFilterWriteOption.SAVE_ALL_FILTERBANKS )) {
            return orderedFilterbanks;
        }

        if (optionSet.contains( HFBFilterWriteOption.SAVE_FOUR_FILTERBANKS )) {
            return orderedFilterbanks.stream().limit( 4 ).collect( Collectors.toList() );
        }

        if (optionSet.contains( HFBFilterWriteOption.SAVE_THREE_FILTERBANKS )) {
            return orderedFilterbanks.stream().limit( 3 ).collect( Collectors.toList() );
        }

        if (optionSet.contains( HFBFilterWriteOption.SAVE_HALF_FILTERBANKS )) {
            return orderedFilterbanks.stream().limit( (1 + orderedFilterbanks.size()) >> 1 ).collect( Collectors.toList() );
        }

        if (optionSet.contains( HFBFilterWriteOption.SAVE_THIRD_FILTERBANKS )) {
            return orderedFilterbanks.stream().limit( (2 + orderedFilterbanks.size()) / 3 ).collect( Collectors.toList() );
        }

        // TODO: Limit (((TODO: Below one percent (related to order), below half percent, below one per mille)))
        // None of these -> return all of them...
        // save by certaincy score.

        return orderedFilterbanks;
    }

    private void writeFilterbanksByOrder( HFBFilterBank filterBank, OutputStream writer, List<HFBFilterBankStats> order ) throws IOException {
        int numberOfFilters = order.size();

        writer.write( RawUtils.toByteArray4b( numberOfFilters ) );
        for (int filterID = 0; filterID < numberOfFilters; filterID++) {
            writeFilterBankData( filterBank, writer, order.get( filterID ).getPosition() );
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

        // int bitweight = BitwiseCalculations.calculateBitWeight( filterDataArray );
        // int invbitweight = BitwiseCalculations.calculateInvBitWeight( filterDataArray );
        // System.out.println( String.format( "Filter #%d has %d bits set (1) and %d not set (0).", filterID, bitweight, invbitweight ) );
    }

}

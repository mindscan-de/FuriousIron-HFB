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

import de.mindscan.furiousiron.hfb.HFBFilterBank;
import de.mindscan.furiousiron.hfb.HFBFilterBankWriter;
import de.mindscan.furiousiron.hfb.HFBFilterData;

/**
 * 
 */
public class HFBFilterBankWriterV1Impl implements HFBFilterBankWriter {

    public final static String FILE_SUFFIX = "hfbv1";

    /** 
     * {@inheritDoc}
     */
    @Override
    public void write( HFBFilterBank filterBank, String outputPath ) {
        // TODO write header
        // Write number of filters
        // write size
        // write filterBank.bitsOfDocumentId;
        // write filterBank.numberODocumentsInfilter
        // write loadfactor?
        // or just the slice size?
        // number of documents in filter is nice for later analysis of effectiveness.

        int numberOfFilters = filterBank.getNumberOfFilters();

        // for each filter save filter to output file
        for (int i = 0; i < numberOfFilters; i++) {
            HFBFilterData filterData = filterBank.getFilterData( i );
            // we want to write the number of bits
            // we want to write the 
            //
            int slicePosition = filterData.getSlicePosition();
            int sliceBitSize = filterData.getSliceBitSize();
            byte[] sliceData = filterData.getSliceData();

            // also add marker and length data.
            // TODO write the index and the position, the sliced bits and the slicedata (also the number of bytes)
        }

    }

}
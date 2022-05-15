/**
 * 
 * MIT License
 *
 * Copyright (c) 2021 Maxim Gansert, Mindscan
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
package de.mindscan.furiousiron.hfb;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A filter bank is a collection of multiple filters, applied to a document id.
 * 
 * The cool thing about such a filter bank is, that it could already combine often
 * used search terms, instead of only tri-grams. We can build a hash of these and 
 * cache this particular filter bank. 
 * 
 * HFBFilter data can be combined using a binary AND-Operation if they have the 
 * same sliceSize and the same slicePosition.
 */
public class HFBFilterBank {

    private List<HFBFilterData> hfbfilters = new ArrayList<>();
    private int bitsInDocumentId;
    private long occurrenceCount;
    private int loadFactor;

    /**
     * 
     */
    public HFBFilterBank() {
    }

    /**
     * 
     * @param bitsInDocumentId e.g. 128 for md5 hashsums
     * @param occurenceCount number of documents for a particular value
     * @param loadFactor set it to 5 (five)
     */
    public void initFilters( int bitsInDocumentId, long occurenceCount, int loadFactor ) {
        initFiltersLazy( bitsInDocumentId, occurenceCount, loadFactor );

        long highestBitMasked = Long.highestOneBit( occurenceCount * loadFactor );
        int sliceSize = (int) Long.numberOfTrailingZeros( highestBitMasked );

        for (int slicePosition = bitsInDocumentId - sliceSize; slicePosition >= 0; slicePosition -= sliceSize) {
            HFBFilterData hfbdata = new HFBFilterData( slicePosition, sliceSize );
            hfbdata.initEmpty();

            addFilterData( hfbdata );
        }
    }

    public void initFiltersLazy( int bitsInDocumentId, long occurenceCount, int loadFactor ) {
        this.bitsInDocumentId = bitsInDocumentId;
        this.occurrenceCount = occurenceCount;
        this.loadFactor = loadFactor;
    }

    public void addFilterData( HFBFilterData hfbdata ) {
        hfbfilters.add( hfbdata );
    }

    public int getNumberOfFilters() {
        return hfbfilters.size();
    }

    public HFBFilterData getFilterData( int filterbankIndex ) {
        if (filterbankIndex < 0 || filterbankIndex >= hfbfilters.size()) {
            throw new IllegalArgumentException( "FilterbankIndex is invalid" );
        }
        return hfbfilters.get( filterbankIndex );
    }

    public void addDocumentId( BigInteger documentId ) {
        // we use each HFBFilterdata and add it to each filter we currently know.
        for (HFBFilterData filter : hfbfilters) {
            // this may be useful to transfer to the filter itself, and set the
            // index by using a BigInteger
            BigInteger partId = documentId.shiftRight( filter.getSlicePosition() ).and( filter.getSliceBitMaskBI() );
            filter.setIndex( partId.intValueExact() );
        }
    }

    public boolean containsDocumentId( BigInteger documentId ) {
        int i = 1;
        for (HFBFilterData filter : hfbfilters) {
            BigInteger partId = documentId.shiftRight( filter.getSlicePosition() ).and( filter.getSliceBitMaskBI() );

            if (!filter.isIndexSet( partId.intValueExact() )) {
                return false;
            }

            // now the trick, is how many of these filters we do want to apply?
            // count the number of applied filters and return true, if we passed 3 max. 4 filters?
            // with 80% dropout rate we get a maximum false positive error rate 
            // * of 0,8 percent when 3 hfb filters are asked = 3 times O(1) lookup
            // * of 0,16 percent when 4 hfb filters are asked = 4 times O(1) lookup
            // if we use a smaller sparsity we have to increase the number of filters

            if (i >= 3) {
                return true;
            }
            i++;
        }
        return true;
    }

    /**
     * @return the bitsInDocumentId
     */
    public int getBitsInDocumentId() {
        return bitsInDocumentId;
    }

    /**
     * @return the occurrenceCount
     */
    public long getOccurrenceCount() {
        return occurrenceCount;
    }

    /**
     * @return the loadFactor
     */
    public int getLoadFactor() {
        return loadFactor;
    }
}

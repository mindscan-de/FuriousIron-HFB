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
package de.mindscan.furiousiron.hfb;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;

/**
 * 
 */
public class HFBFilterBankCompiler {

    /**
     * Will create a filter bank from a set of given documentIds. You should not
     * add any more documentIds to this filter, to keep efficiency.
     * 
     * @param documentIds we assume that documentIds are encoded as hexadecimal values (as of now)
     * @return a fully initialized filter, containing all given document ids
     */
    public HFBFilterBank compileFilterHex( Collection<String> documentIds ) {
        HFBFilterBank compiledFilterBank = new HFBFilterBank();

        // we assume 128 bit long document ids, and a minimum of 32 
        // documents in the filter, and a desired rejection rate of 80% for
        // each filter step.
        compiledFilterBank.initFilters( 128, Math.min( documentIds.size(), 32 ), 5 );

        for (String documentIdStr : documentIds) {
            BigInteger documentId = new BigInteger( documentIdStr, 16 );
            compiledFilterBank.addDocumentId( documentId );
        }

        return compiledFilterBank;
    }

    /**
     * Use this method to create a filter bank of the estimated size, and then
     * use {@link #insertDocumentIds(HFBFilterBank, Set)} to add documentIds 
     * to the filter. 
     * 
     * @param numberOfDocuments number of document ids to be inserted into the hfb filter 
     * @return an empty initialized filter, containing no document ids.
     */
    public HFBFilterBank createEmptyFilter( long numberOfDocuments ) {
        HFBFilterBank compiledFilterBank = new HFBFilterBank();

        // we assume 128 bit long document ids, and a minimum of 32 
        // documents in the filter, and a desired rejection rate of 
        // 80% for  each filter step.
        compiledFilterBank.initFilters( 128, Math.min( numberOfDocuments, 32 ), 5 );

        return compiledFilterBank;
    }

    /**
     * Inserts a collection of document ids given as hexadecimal number. 
     * 
     * @param filter the filter where we want to add a collection of documentIds
     * @param documentIds  we assume that documentIds are encoded as hexadecimal values (as of now)
     * @return filter is returned
     */
    public HFBFilterBank insertDocumentIdsHex( HFBFilterBank filter, Collection<String> documentIds ) {
        for (String documentIdStr : documentIds) {
            BigInteger documentId = new BigInteger( documentIdStr, 16 );
            filter.addDocumentId( documentId );
        }

        return filter;
    }

    /**
     * Inserts a collection of document ids given as biginteger values.
     * 
     * @param filter the filter where we want to add a bunch of documentIds
     * @param documentIds the document ids are provided as a collection of bigintegers
     * @return filter is returned
     */
    public HFBFilterBank insertDocumentIdsBI( HFBFilterBank filter, Collection<BigInteger> documentIds ) {
        filter.addDocumentIds( documentIds );
        return filter;
    }

}

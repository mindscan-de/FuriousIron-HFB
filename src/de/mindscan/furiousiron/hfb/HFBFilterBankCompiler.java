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
import java.util.Set;

/**
 * 
 */
public class HFBFilterBankCompiler {

    /**
     * Will create a filterbank from a set of given documentIds. You should not
     * add any more documentIds to this filter
     * 
     * @param documentIds
     * @return
     */
    public HFBFilterBank compileFilter( Set<String> documentIds ) {
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
     * @param documentIds
     * @param numberOfDocuments number of document ids to be inserted into the hfb filter 
     * @return
     */
    public HFBFilterBank compileFilter( long numberOfDocuments ) {
        HFBFilterBank compiledFilterBank = new HFBFilterBank();

        // we assume 128 bit long document ids, and a minimum of 32 
        // documents in the filter, and a desired rejection rate of 
        // 80% for  each filter step.
        compiledFilterBank.initFilters( 128, Math.min( numberOfDocuments, 32 ), 5 );

        return compiledFilterBank;
    }

    public HFBFilterBank insertDocumentIds( HFBFilterBank filter, Set<String> documentIds ) {
        for (String documentIdStr : documentIds) {
            BigInteger documentId = new BigInteger( documentIdStr, 16 );
            filter.addDocumentId( documentId );
        }

        return filter;
    }

}

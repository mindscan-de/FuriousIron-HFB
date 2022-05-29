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
import de.mindscan.furiousiron.hfb.HFBFilterBankReader;

/**
 * This is the second version of the reader, which can read HFBFilterbanks where
 * the filter data was encoded using a Golomb code coding scheme.
 * 
 * This golomb coding scheme is just to add a little complication to that whole 
 * thing. For indexes like mine, this probably serves no real purpose to save some
 * bits. But for large scale a reduction in size adds up. Anyhow a single bit error
 * on the golomb encoded filter bank, will corrupt the filter.
 * 
 * It is just an idea, maybe i will drop it if i find enough reasons to not use 
 * golomb coding. Let's see how far we will go here.
 * 
 * Idea for the golomb decoding algorithm:
 * 
 * The decoder is planned as a kind of a state table instead of calculating bit 
 * positions. It will calculate bit wise or operations on memory and perform them 
 * if necessary in one go.
 */
public class HFBFilterBankReaderV2Impl implements HFBFilterBankReader {

    /** 
     * {@inheritDoc}
     */
    @Override
    public HFBFilterBank readFromFile( String filePath ) {
        return null;
    }

}

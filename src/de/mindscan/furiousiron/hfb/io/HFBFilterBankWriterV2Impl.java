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

/**
 * 
 * This is the second version of the HFBFilterbank Writer. This encodes the byte data, which is 
 * actually used as a bitset using a golomb code. Because we use a load factor of at least three
 * to five we expect more zeros bits in a single byte. that will allow to compress bytes using 
 * fewer bits.
 *  
 *  
 * Maybe i can implement a fast encoding and/decoding state machine to create and read the
 * golombcode. I think of a state machine which is used for quasi-arithmetic coding. Maybe
 * i will use (quasi) arithmetic coding instead... let's see... Maybe it will be a golombcode
 * 
 * Golomb, S.W.: Run-Length Encodings. IEEE Transactions on Information Theory, Vol.12, September 1966, 399–401
 * 
 * Howard & Vitter 1993? 
 * * Design and Analysis of Fast Text Compression Based on Quasi-Arithmetic Coding
 * * https://kuscholarworks.ku.edu/bitstream/handle/1808/7210/HoV93.qtfull.pdf;sequence=1

 * Maybe we will use a different approach, using an optimized Golomb-Rice Code. Not yet decided....
 * https://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=8272498
 */
public class HFBFilterBankWriterV2Impl implements HFBFilterBankWriter {

    /** 
     * {@inheritDoc}
     */
    @Override
    public void write( HFBFilterBank filterBank, String outputPath ) {
        // TODO Auto-generated method stub

    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void write( HFBFilterBank filterBank, String outputPath, HFBFilterWriteOption... options ) {
        // TODO Auto-generated method stub

    }
}

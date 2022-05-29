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
import java.util.Arrays;

/**
 * Hold hfb filter data using a bit field.
 * 
 * This is one particular hfb filter related to the 'slicePositon' and 'sliceBitSize'
 * as a hash value extractor.
 */
public class HFBFilterData {

    // modulo by 8 (byte size in bits) is an and by 7
    public static final int BYTE_ADDRESS_MASK = 7;
    // divide by 8 (byte size in bits) is a shift by 3
    public static final int BYTE_ADDRESS_SHIFT = 3;

    // The position (number of bits to shift right before applying the sliceBitMask.
    private int slicePosition;

    // the number of bits for the HFB-Filter
    private int sliceBitSize;

    // the mask for the bits.
    private long sliceBitMask;

    // contains the filter data
    private byte[] sliceData;
    private byte[] asBitPosition = { 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80 };

    private BigInteger sliceBitMaskBI;

    public HFBFilterData( int slicePosition, int numberOfBits ) {
        setSlicePosition( slicePosition );
        setSliceMaskSize( numberOfBits );
    }

    public void setSliceMaskSize( int numberOfBits ) {
        this.sliceBitSize = numberOfBits;
        long sliceSize = 1L << (numberOfBits);

        this.sliceBitMask = sliceSize - 1L;
        this.sliceBitMaskBI = new BigInteger( Long.toString( this.sliceBitMask ) );

        // currently the number of documents in my index won't exceed my memory needs 
        // for a couple of filters

        // allocate according to sliceSize () - well maybe this is too large,
        // but we really shouldn't care right now. I leave it for future 
        // development and future improvements
    }

    public void initEmpty() {
        int numberOfBits = Math.max( this.sliceBitSize - BYTE_ADDRESS_SHIFT, 0 );
        setSliceDataInternal( new byte[1 << numberOfBits] );
    }

    public void setSliceData( byte[] filterData ) {
        int numberOfBits = Math.max( this.sliceBitSize - BYTE_ADDRESS_SHIFT, 0 );
        this.sliceData = Arrays.copyOf( filterData, 1 << numberOfBits );
    }

    private void setSliceDataInternal( byte[] filterData ) {
        this.sliceData = filterData;
    }

    public byte[] getSliceData() {
        return this.sliceData;
    }

    public long getSliceBitMask() {
        return sliceBitMask;
    }

    public BigInteger getSliceBitMaskBI() {
        return sliceBitMaskBI;
    }

    public int getSliceBitSize() {
        return sliceBitSize;
    }

    public void setSlicePosition( int slicePosition ) {
        this.slicePosition = slicePosition;
    }

    public int getSlicePosition() {
        return slicePosition;
    }

    public void setIndex( int index ) {
        this.sliceData[index >> BYTE_ADDRESS_SHIFT] |= asBitPosition[index & BYTE_ADDRESS_MASK];
    }

    public void clearIndex( int index ) {
        this.sliceData[index >> BYTE_ADDRESS_SHIFT] &= ~asBitPosition[index & BYTE_ADDRESS_MASK];
    }

    public boolean isIndexSet( int index ) {
        return (this.sliceData[index >> BYTE_ADDRESS_SHIFT] & asBitPosition[index & BYTE_ADDRESS_MASK]) != 0;
    }
}

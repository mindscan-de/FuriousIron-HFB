/**
 * 
 * MIT License
 *
 * Copyright (c) 2021, 2022 Maxim Gansert, Mindscan
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

/**
 * 
 */
public class RawUtils {

    static int toUnsignedInt4b( byte[] readNBytes, int offset ) {
        int b0 = (readNBytes[0 + offset]) & 0xff;
        int b1 = (readNBytes[1 + offset]) & 0xff;
        int b2 = (readNBytes[2 + offset]) & 0xff;
        int b3 = (readNBytes[3 + offset]) & 0xff;

        return (b0 << 24) | (b1 << 16) | (b2 << 8) | (b3);
    }

    static int toUnsignedInt2b( byte[] readNBytes, int offset ) {
        int b0 = (readNBytes[0 + offset]) & 0xff;
        int b1 = (readNBytes[1 + offset]) & 0xff;

        return (b0 << 8) | (b1);
    }

    static int toUnsignedInt1b( byte[] readNBytes, int offset ) {
        int b0 = (readNBytes[0 + offset]) & 0xff;
        return b0;
    }

    static long toUnsignedLong8b( byte[] readNBytes, int offset ) {
        long hiInt = toUnsignedInt4b( readNBytes, offset ) & 0xffffffffL;
        long lowInt = toUnsignedInt4b( readNBytes, offset + 4 ) & 0xffffffffL;

        return (hiInt << 32) | lowInt;
    }

    static boolean isMarker4b( byte[] data, int offset, int expectedMarker ) {
        if (data == null || data.length < offset + 4) {
            return false;
        }

        int marker = toUnsignedInt4b( data, offset );
        return marker == expectedMarker;
    }

}

/* 
 * Copyright 2014 Igor Maznitsa (http://www.igormaznitsa.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.jbbp.io;

import static com.igormaznitsa.jbbp.io.JBBPOut.*;
import java.io.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class JBBPOutTest {
  
  @Test
  public void testBeginBin() throws Exception {
    assertArrayEquals(new byte[]{1}, BeginBin().Byte(1).End().toByteArray());
    assertArrayEquals(new byte[]{0x02, 0x01}, BeginBin(JBBPByteOrder.LITTLE_ENDIAN).Short(0x0102).End().toByteArray());
    assertArrayEquals(new byte[]{0x40, (byte) 0x80}, BeginBin(JBBPByteOrder.LITTLE_ENDIAN, JBBPBitOrder.MSB0).Short(0x0102).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0x80}, BeginBin(JBBPBitOrder.MSB0).Byte(1).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0x80}, BeginBin(1).Byte(0x80).End().toByteArray());

    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    assertSame(buffer, BeginBin(buffer).End());
  }

  @Test
  public void testSkip() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0x01, (byte) 0xFF}, JBBPOut.BeginBin().Bit(1).Skip(0).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0x01, 0x00, (byte) 0xFF}, JBBPOut.BeginBin().Bit(1).Skip(1).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0x01, 0x00, 0x00, (byte) 0xFF}, JBBPOut.BeginBin().Bit(1).Skip(2).Byte(0xFF).End().toByteArray());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSkip_ErrorForNegativeValue() throws Exception {
    JBBPOut.BeginBin().Skip(-1);
  }

  @Test
  public void testAlign() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0x01, (byte) 0xFF}, JBBPOut.BeginBin().Bit(1).Align().Byte(0xFF).End().toByteArray());
  }

  @Test
  public void testAlignWithArgument() throws Exception {
    assertEquals(0, JBBPOut.BeginBin().Align(2).End().toByteArray().length);
    assertArrayEquals(new byte[]{(byte) 0x01, (byte) 0xFF}, JBBPOut.BeginBin().Bit(1).Align(1).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0xFF}, JBBPOut.BeginBin().Align(3).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{0x01, (byte) 0xFF}, JBBPOut.BeginBin().Bit(1).Align(1).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{0x01, 0x00, (byte) 0xFF}, JBBPOut.BeginBin().Bit(1).Align(2).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{0x01, 0x00, 0x00, 0x00, (byte) 0xFF}, JBBPOut.BeginBin().Bit(1).Align(4).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{0x01, 0x02, 0x00, 0x00, (byte) 0xFF}, JBBPOut.BeginBin().Byte(1, 2).Align(4).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x00, 0x00, (byte) 0xFF}, JBBPOut.BeginBin().Byte(1, 2, 3).Align(5).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x00, (byte) 0xFF}, JBBPOut.BeginBin().Byte(1, 2, 3, 4).Align(5).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, (byte) 0xFF}, JBBPOut.BeginBin().Byte(1, 2, 3, 4, 5).Align(5).Byte(0xFF).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0x01, 0x00, 0x02, 0x00, (byte) 0x03}, JBBPOut.BeginBin().Align(2).Byte(1).Align(2).Byte(2).Align(2).Byte(3).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0xF1, 0x00, (byte) 0x01, 0x00, 0x02, 0x00, (byte) 0x03}, JBBPOut.BeginBin().Byte(0xF1).Align(2).Byte(1).Align(2).Byte(2).Align(2).Byte(3).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0xF1, 0x00, 0x00, (byte) 0x01, 0x00, 00, 0x02, 0x00, 00, (byte) 0x03}, JBBPOut.BeginBin().Byte(0xF1).Align(3).Byte(1).Align(3).Byte(2).Align(3).Byte(3).End().toByteArray());
    assertArrayEquals(new byte[]{0x01, 0x02, 03, 0x04, 0x00, (byte) 0xF1}, JBBPOut.BeginBin().Int(0x01020304).Align(5).Byte(0xF1).End().toByteArray());
    assertArrayEquals(new byte[]{0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0xF1}, JBBPOut.BeginBin().Bit(1).Align(5).Byte(0xF1).End().toByteArray());
  }

  @Test
  public void testEmptyArray() throws Exception {
    assertEquals(0, JBBPOut.BeginBin().End().toByteArray().length);
  }

  @Test
  public void testByte() throws Exception {
    assertArrayEquals(new byte[]{-34}, BeginBin().Byte(-34).End().toByteArray());
  }

  @Test
  public void testByteArrayAsInts() throws Exception {
    assertArrayEquals(new byte[]{1, 3, 0, 2, 4, 1, 3, 7}, BeginBin().Byte(1, 3, 0, 2, 4, 1, 3, 7).End().toByteArray());
  }

  @Test
  public void testByteArrayAsByteArray() throws Exception {
    assertArrayEquals(new byte[]{1, 3, 0, 2, 4, 1, 3, 7}, BeginBin().Byte(new byte[]{1, 3, 0, 2, 4, 1, 3, 7}).End().toByteArray());
  }

  @Test
  public void testByteArrayAsString() throws Exception {
    assertArrayEquals(new byte[]{(byte) 'a', (byte) 'b', (byte) 'c'}, BeginBin().Byte("abc").End().toByteArray());
  }

  @Test
  public void testByteArrayAsString_RussianChars() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0x20, (byte) 0x43, (byte) 0x41}, BeginBin().Byte("Рус").End().toByteArray());
  }

  @Test
  public void testUtf8_OnlyLatinChars() throws Exception {
    assertArrayEquals(new byte[]{(byte) 'a', (byte) 'b', (byte) 'c'}, BeginBin().Utf8("abc").End().toByteArray());
  }

  @Test
  public void testUtf8_RussianChars() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0xD0, (byte) 0xA0, (byte) 0xD1, (byte) 0x83, (byte) 0xD1, (byte) 0x81}, BeginBin().Utf8("Рус").End().toByteArray());
  }

  @Test
  public void testBit() throws Exception {
    assertArrayEquals(new byte[]{1}, BeginBin().Bit(1).End().toByteArray());
  }

  @Test
  public void testBit_MSB0() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0x80}, BeginBin(JBBPByteOrder.BIG_ENDIAN, JBBPBitOrder.MSB0).Bit(1).End().toByteArray());
  }

  @Test
  public void testBit_LSB0() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0x01}, BeginBin(JBBPByteOrder.BIG_ENDIAN, JBBPBitOrder.LSB0).Bit(1).End().toByteArray());
  }

  @Test
  public void testBits_Int() throws Exception {
    assertArrayEquals(new byte[]{0xD}, BeginBin().Bits(JBBPBitNumber.BITS_4, 0xFD).End().toByteArray());
  }

  @Test
  public void testBits_IntArray() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0xED}, BeginBin().Bits(JBBPBitNumber.BITS_4, 0xFD, 0xFE).End().toByteArray());
  }

  @Test
  public void testBits_ByteArray() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0xED}, BeginBin().Bits(JBBPBitNumber.BITS_4, new byte[]{(byte) 0xFD, (byte) 0x8E}).End().toByteArray());
  }

  @Test
  public void testBitArrayAsInts() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0xE3}, BeginBin().Bit(1, 3, 0, 2, 4, 1, 3, 7).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0x0B}, BeginBin().Bit(1, 3, 0, 7).End().toByteArray());
  }

  @Test
  public void testBitArrayAsBytes() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0xE3}, BeginBin().Bit(new byte[]{(byte) 1, (byte) 3, (byte) 0, (byte) 2, (byte) 4, (byte) 1, (byte) 3, (byte) 7}).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0x0B}, BeginBin().Bit(new byte[]{(byte) 1, (byte) 3, (byte) 0, (byte) 7}).End().toByteArray());
  }

  @Test
  public void testBitArrayAsBooleans() throws Exception {
    assertArrayEquals(new byte[]{(byte) 0xE3}, BeginBin().Bit(true, true, false, false, false, true, true, true).End().toByteArray());
    assertArrayEquals(new byte[]{(byte) 0x0B}, BeginBin().Bit(true, true, false, true).End().toByteArray());
  }

  @Test
  public void testShort() throws Exception {
    assertArrayEquals(new byte[]{0x01, 02}, BeginBin().Short(0x0102).End().toByteArray());
  }

  @Test
  public void testShort_BigEndian() throws Exception {
    assertArrayEquals(new byte[]{0x01, 02}, BeginBin().ByteOrder(JBBPByteOrder.BIG_ENDIAN).Short(0x0102).End().toByteArray());
  }

  @Test
  public void testShort_LittleEndian() throws Exception {
    assertArrayEquals(new byte[]{0x02, 01}, BeginBin().ByteOrder(JBBPByteOrder.LITTLE_ENDIAN).Short(0x0102).End().toByteArray());
  }

  @Test
  public void testShortArray_AsIntegers() throws Exception {
    assertArrayEquals(new byte[]{1, 2, 3, 4}, BeginBin().Short(0x0102, 0x0304).End().toByteArray());
  }

  @Test
  public void testShortArray_AsIntegers_BigEndian() throws Exception {
    assertArrayEquals(new byte[]{1, 2, 3, 4}, BeginBin().ByteOrder(JBBPByteOrder.BIG_ENDIAN).Short(0x0102, 0x0304).End().toByteArray());
  }

  @Test
  public void testShortArray_AsIntegers_LittleEndian() throws Exception {
    assertArrayEquals(new byte[]{2, 1, 4, 3}, BeginBin().ByteOrder(JBBPByteOrder.LITTLE_ENDIAN).Short(0x0102, 0x0304).End().toByteArray());
  }

  @Test
  public void testShortArray_AsShorts() throws Exception {
    assertArrayEquals(new byte[]{1, 2, 3, 4}, BeginBin().Short(new short[]{(short) 0x0102, (short) 0x0304}).End().toByteArray());
  }

  @Test
  public void testShortArray_AsShortArray() throws Exception {
    assertArrayEquals(new byte[]{1, 2, 3, 4}, BeginBin().Short(new short[]{(short) 0x0102, (short) 0x0304}).End().toByteArray());
  }

  @Test
  public void testInt() throws Exception {
    assertArrayEquals(new byte[]{0x01, 02, 0x03, 0x04}, BeginBin().Int(0x01020304).End().toByteArray());
  }

  @Test
  public void testIntArray() throws Exception {
    assertArrayEquals(new byte[]{0x01, 02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}, BeginBin().Int(0x01020304, 0x05060708).End().toByteArray());
  }

  @Test
  public void testInt_BigEndian() throws Exception {
    assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, BeginBin().ByteOrder(JBBPByteOrder.BIG_ENDIAN).Int(0x01020304).End().toByteArray());
  }

  @Test
  public void testInt_LittleEndian() throws Exception {
    assertArrayEquals(new byte[]{0x04, 0x03, 0x02, 0x01}, BeginBin().ByteOrder(JBBPByteOrder.LITTLE_ENDIAN).Int(0x01020304).End().toByteArray());
  }

  @Test
  public void testFloat_BigEndian() throws Exception {
    final int flt = Float.floatToIntBits(Float.MAX_VALUE);
    assertArrayEquals(new byte[]{(byte)(flt>>>24),(byte) (flt >>> 16),(byte) (flt >>> 8),(byte) flt}, BeginBin().ByteOrder(JBBPByteOrder.BIG_ENDIAN).Float(Float.MAX_VALUE).End().toByteArray());
  }

  @Test
  public void testFloat_LittleEndian() throws Exception {
    final int flt = Float.floatToIntBits(Float.MAX_VALUE);
    assertArrayEquals(new byte[]{(byte)flt,(byte) (flt >>> 8),(byte) (flt >>> 16),(byte) (flt>>>24)}, BeginBin().ByteOrder(JBBPByteOrder.LITTLE_ENDIAN).Float(Float.MAX_VALUE).End().toByteArray());
  }

  
  
  @Test
  public void testLong() throws Exception {
    assertArrayEquals(new byte[]{0x01, 02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}, BeginBin().Long(0x0102030405060708L).End().toByteArray());
  }

  @Test
  public void testLongArray() throws Exception {
    assertArrayEquals(new byte[]{0x01, 02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18}, BeginBin().Long(0x0102030405060708L, 0x1112131415161718L).End().toByteArray());
  }

  @Test
  public void testLong_BigEndian() throws Exception {
    assertArrayEquals(new byte[]{0x01, 02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}, BeginBin().ByteOrder(JBBPByteOrder.BIG_ENDIAN).Long(0x0102030405060708L).End().toByteArray());
  }

  @Test
  public void testLong_LittleEndian() throws Exception {
    assertArrayEquals(new byte[]{0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01}, BeginBin().ByteOrder(JBBPByteOrder.LITTLE_ENDIAN).Long(0x0102030405060708L).End().toByteArray());
  }

  @Test
  public void testDouble_BigEndian() throws Exception {
    final long dbl = Double.doubleToLongBits(Double.MAX_VALUE);
    final byte [] array = BeginBin().ByteOrder(JBBPByteOrder.BIG_ENDIAN).Double(Double.MAX_VALUE).End().toByteArray();
    assertArrayEquals(new byte[]{(byte) (dbl >>> 56), (byte) (dbl >>> 48), (byte) (dbl >>> 40), (byte) (dbl >>> 32), (byte) (dbl >>> 24), (byte) (dbl >>> 16), (byte) (dbl >>> 8), (byte) dbl}, array);
  }
  
  @Test
  public void testDouble_LittleEndian() throws Exception {
    final long dbl = Double.doubleToLongBits(Double.MAX_VALUE);
    assertArrayEquals(new byte[]{(byte)dbl, (byte) (dbl >>> 8),(byte) (dbl >>> 16),(byte) (dbl >>> 24), (byte) (dbl >>> 32),(byte) (dbl >>> 40),(byte) (dbl >>> 48),(byte) (dbl >>> 56)}, BeginBin().ByteOrder(JBBPByteOrder.LITTLE_ENDIAN).Double(Double.MAX_VALUE).End().toByteArray());
  }
  
  @Test
  public void testFlush() throws Exception {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final JBBPOut out = BeginBin(buffer);

    out.Bit(true);
    assertEquals(0, buffer.size());
    out.Flush();
    assertEquals(1, buffer.size());
  }

  @Test
  public void testExceptionForOperatioOverEndedProcess() throws Exception {
    final JBBPOut out = BeginBin();
    out.ByteOrder(JBBPByteOrder.BIG_ENDIAN).Long(0x0102030405060708L).End();
    try {
      out.Align();
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Align(3);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Bit(true);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Bit(true, false);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Bit((byte) 34);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Bit(new byte[]{(byte) 34, (byte) 12});
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Bit(34, 12);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Bits(JBBPBitNumber.BITS_3, 12);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Bits(JBBPBitNumber.BITS_3, 12, 13, 14);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Bits(JBBPBitNumber.BITS_3, new byte[]{(byte) 1, (byte) 2, (byte) 3});
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Bool(true);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Bool(true, false);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Byte(new byte[]{(byte) 1, (byte) 2, (byte) 3});
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Byte(1);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Byte((String) null);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Utf8((String) null);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Byte(1, 2, 3);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.ByteOrder(JBBPByteOrder.BIG_ENDIAN);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Flush();
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Int(1);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Int(1, 2);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Long(1L);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Long(1L, 2L);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Short(1);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Short(1, 2, 3);
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.Short(new short[]{(short) 1, (short) 2, (short) 3});
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }

    try {
      out.End();
      fail("Must throw ISE");
    }
    catch (IllegalStateException ex) {
    }
  }

  @Test
  public void testExternalStreamButNoByteArrayOutputStream() throws Exception {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final DataOutputStream dout = new DataOutputStream(buffer);

    assertNull(BeginBin(dout).Byte(1, 2, 3).End());
    assertArrayEquals(new byte[]{1, 2, 3}, buffer.toByteArray());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExceptionForBitOrderConfilctInCaseOfUsageBitOutputStream() throws Exception {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final JBBPBitOutputStream bitstream = new JBBPBitOutputStream(buffer, JBBPBitOrder.LSB0);

    JBBPOut.BeginBin(bitstream, JBBPByteOrder.BIG_ENDIAN, JBBPBitOrder.MSB0);
  }

  @Test
  public void testComplexWriting_1() throws Exception {
    final byte[] array
            = BeginBin().
            Bit(1, 2, 3, 0).
            Bit(true, false, true).
            Align().
            Byte(5).
            Short(1, 2, 3, 4, 5).
            Bool(true, false, true, true).
            Int(0xABCDEF23, 0xCAFEBABE).
            Long(0x123456789ABCDEF1L, 0x212356239091AB32L).
            Utf8("JFIF").
            Byte("Рус").
            End().toByteArray();

    assertEquals(47, array.length);
    assertArrayEquals(new byte[]{
      (byte) 0x55, 5, 0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 1, 0, 1, 1,
      (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x23, (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
      0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF1, 0x21, 0x23, 0x56, 0x23, (byte) 0x90, (byte) 0x91, (byte) 0xAB, 0x32,
      0x4A, 0x46, 0x49, 0x46,
      (byte) 0x20, (byte) 0x43, (byte) 0x41
    }, array);
  }

  @Test(expected = NullPointerException.class)
  public void testVar_NPEForNullProcessor() throws Exception {
    BeginBin().Var(null).End();
  }

  @Test
  public void testVar_ProcessRest() throws Exception {
    final byte[] array = BeginBin().
            Byte(0xCC).
            Var(new JBBPOutVarProcessor() {

              public boolean processVarOut(final JBBPOut context, final JBBPBitOutputStream outStream, final Object... args) throws IOException {
                assertNotNull(context);
                assertNotNull(outStream);
                assertEquals(0, args.length);
                outStream.write(0xDD);
                return true;
              }
            }).
            Byte(0xAA).
            End().toByteArray();

    assertArrayEquals(new byte[]{(byte) 0xCC, (byte) 0xDD, (byte) 0xAA}, array);
  }

  @Test
  public void testVar_SkipRest() throws Exception {
    final byte[] array = BeginBin().
            Byte(0xCC).
            Var(new JBBPOutVarProcessor() {

              public boolean processVarOut(final JBBPOut context, final JBBPBitOutputStream outStream, final Object... args) throws IOException {
                assertNotNull(context);
                assertNotNull(outStream);
                assertEquals(0, args.length);
                outStream.write(0xDD);
                return false;
              }
            }).
            Byte(0xAA).
            Align(15).
            Align().
            Bit(true).
            Bit(34).
            Bit(true, false).
            Bit((byte) 11).
            Bit(new byte[]{(byte) 11, (byte) 45}).
            Bit(111, 222).
            Bits(JBBPBitNumber.BITS_5, 0xFF).
            Bits(JBBPBitNumber.BITS_5, 0xFF, 0xAB).
            Bits(JBBPBitNumber.BITS_5, new byte[]{(byte) 0xFF, (byte) 0xAB}).
            Bool(true).
            Bool(false, false).
            Byte("HURRAAA").
            Byte(new byte[]{(byte) 1, (byte) 2, (byte) 3}).
            Byte(232324).
            Byte(2322342, 2323232).
            ByteOrder(JBBPByteOrder.LITTLE_ENDIAN).
            Int(23432432).
            Int(234234234, 234234234).
            Long(234823948234L).
            Long(234823948234L, 234233243243L).
            Short(234).
            Short(234, 233).
            Short(new short[]{(short) 234, (short) 233}).
            Skip(332).
            Utf8("werwerew").
            Var(new JBBPOutVarProcessor() {

              public boolean processVarOut(JBBPOut context, JBBPBitOutputStream outStream, Object... args) throws IOException {
                fail("Must not be called");
                return false;
              }
            }).
            End().toByteArray();

    assertArrayEquals(new byte[]{(byte) 0xCC, (byte) 0xDD}, array);
  }

  @Test
  public void testVar_VariableContent() throws Exception {
    final JBBPOutVarProcessor var = new JBBPOutVarProcessor() {
      public boolean processVarOut(JBBPOut context, JBBPBitOutputStream outStream, Object... args) throws IOException {
        final int type = (Integer)args[0];
        switch(type){
          case 0 : {
            context.Int(0x01020304);
          }break;
          case 1 : {
            context.Int(0x05060708);
          }break;
          default: {
            fail("Unexpected parameter ["+type+']');
          }break;
        }
        return true;
      }
    };
    
    final byte [] array = JBBPOut.BeginBin().
            Var(var, 0).
            Var(var, 1).
            End().toByteArray();
    
    assertArrayEquals(new byte[]{1,2,3,4,5,6,7,8}, array);
    
  }
  
}

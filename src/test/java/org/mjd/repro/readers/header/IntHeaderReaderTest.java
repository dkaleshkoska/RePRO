package org.mjd.repro.readers.header;

import java.nio.ByteBuffer;

import com.google.common.primitives.Ints;
import com.mscharhag.oleaster.runner.OleasterRunner;
import org.junit.runner.RunWith;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.beforeEach;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.describe;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.it;

@RunWith(OleasterRunner.class)
public final class IntHeaderReaderTest
{
    private IntHeaderReader readerUnderTest;

    private final int singleFirstByteValue = 5;
    private final int lastThreeByteValue = 3841;
    private byte[] fullFiveHeader;

    private byte[] lastThreeByteHeader;
    private ByteBuffer completeBuffer;
    private ByteBuffer partBuffer;
    private ByteBuffer restOfItBuffer;

    // TEST BLOCK
    {
        beforeEach(() -> {
            readerUnderTest = new IntHeaderReader("test");
            fullFiveHeader = Ints.toByteArray(singleFirstByteValue);
            lastThreeByteHeader = Ints.toByteArray(lastThreeByteValue);
            completeBuffer = ByteBuffer.wrap(fullFiveHeader);
            partBuffer = ByteBuffer.wrap(fullFiveHeader, 0, fullFiveHeader.length - 1);
            restOfItBuffer = ByteBuffer.wrap(fullFiveHeader, fullFiveHeader.length - 1, 1);
        });

        describe("When a client asks the IntHeader reader for the header size", () -> {
        	it("should return " + Integer.BYTES, () -> {
        		expect(readerUnderTest.getSize()).toEqual(Integer.BYTES);
        	});
        });

        describe("when the header data", () -> {
            describe("arrives in", ()-> {
                describe("one single read, that is, all at once", ()-> {
                	beforeEach(() -> {
                		readerUnderTest.readHeader(completeBuffer);
                	});
                    it("should read the correct header value", ()->{
                        expect(readerUnderTest.isComplete()).toBeTrue();
                    });
                    it("should have 0 bytes remaining", () -> {
                    	expect(readerUnderTest.remaining()).toEqual(0);
                    });
                    it("should have the correct value", () -> {
                    	expect(readerUnderTest.getValue()).toEqual(singleFirstByteValue);
                    });
                });
                describe("two single bytes and the rest does not arrive", () -> {
                	beforeEach(() -> {
                		readerUnderTest.readHeader(ByteBuffer.wrap(fullFiveHeader, 0, 1));
                		readerUnderTest.readHeader(ByteBuffer.wrap(fullFiveHeader, 1, 1));
                	});
                	it("should not be complete", () -> {
                		expect(readerUnderTest.isComplete()).toBeFalse();
                	});
                	it("should have two bytes remaining", () -> {
                		expect(readerUnderTest.remaining()).toEqual(2);
                	});
                	it("should throw an exception for any attempt to get the value", () -> {
                		expect(() -> readerUnderTest.getValue()).toThrow(IllegalStateException.class);
                	});
                });
                describe("single bytes", () -> {
                	beforeEach(() -> {
                		readerUnderTest.readHeader(ByteBuffer.wrap(fullFiveHeader, 0, 1));
                		readerUnderTest.readHeader(ByteBuffer.wrap(fullFiveHeader, 1, 1));
                		readerUnderTest.readHeader(ByteBuffer.wrap(fullFiveHeader, 2, 1));
                		readerUnderTest.readHeader(ByteBuffer.wrap(fullFiveHeader, 3, 1));
                	});
                    it("should complete", () -> {
                        expect(readerUnderTest.isComplete()).toBeTrue();
                    });
                    it("should have zero bytes remaining", () -> {
                    	expect(readerUnderTest.remaining()).toEqual(0);
                    });
                    it("should have the correct value", () -> {
                    	expect(readerUnderTest.getValue()).toEqual(singleFirstByteValue);
                    });
                });
                describe("two parts", ()-> {
                	describe("with 3 bytes arriving first", ()-> {
	                	beforeEach(() -> {
	                		readerUnderTest.readHeader(partBuffer);
	                	});
	                    describe("and then finally the last byte", ()-> {
	                    	beforeEach(() -> {
	                    		expect(readerUnderTest.isComplete()).toBeFalse();
	                    	});
	                        it("should read the correct header value", () -> {
	                            readerUnderTest.readHeader(restOfItBuffer);
	                            expect(readerUnderTest.isComplete()).toBeTrue();
	                            expect(readerUnderTest.getValue()).toEqual(singleFirstByteValue);
	                        });
	                        it("should show 1 remaining after the first 3 bytes arrive", () -> {
	                            readerUnderTest.readHeader(partBuffer);
	                            expect(readerUnderTest.isComplete()).toBeFalse();
	                            expect(readerUnderTest.remaining()).toEqual(1);
	                        });
	                    });
                	});
                    describe("where the last byte arrives first", ()-> {
                        it("should read the correct header value", () -> {
                            ByteBuffer firstByte = ByteBuffer.wrap(fullFiveHeader, 0, 1);
                            ByteBuffer restOfItBuffer = ByteBuffer.wrap(fullFiveHeader, 1, fullFiveHeader.length - 1);
                            readerUnderTest.readHeader(firstByte);
                            expect(readerUnderTest.isComplete()).toBeFalse();
                            readerUnderTest.readHeader(restOfItBuffer);
                            expect(readerUnderTest.isComplete()).toBeTrue();
                            expect(readerUnderTest.getValue()).toEqual(singleFirstByteValue);
                        });
                        it("should show 3 remaining after the first byte arrives", () -> {
                            ByteBuffer firstByte = ByteBuffer.wrap(fullFiveHeader, 0, 1);
                            readerUnderTest.readHeader(firstByte);
                            expect(readerUnderTest.isComplete()).toBeFalse();
                            expect(readerUnderTest.remaining()).toEqual(3);
                        });
                        describe("and the header value uses three bytes", ()-> {
                            it("should read the correct header value", () -> {
                                ByteBuffer firstByte = ByteBuffer.wrap(lastThreeByteHeader, 0, 1);
                                ByteBuffer restOfItBuffer = ByteBuffer.wrap(lastThreeByteHeader, 1, lastThreeByteHeader.length - 1);
                                readerUnderTest.readHeader(firstByte);
                                expect(readerUnderTest.isComplete()).toBeFalse();
                                readerUnderTest.readHeader(restOfItBuffer);
                                expect(readerUnderTest.isComplete()).toBeTrue();
                                expect(readerUnderTest.getValue()).toEqual(lastThreeByteValue);
                            });
                        });
                    });
                });
                describe("three parts", ()-> {
                    it("should read the correct header value", () -> {
                        ByteBuffer partBuffer = ByteBuffer.wrap(fullFiveHeader, 0, fullFiveHeader.length - 2);
                        ByteBuffer middlePartBuffer = ByteBuffer.wrap(fullFiveHeader, fullFiveHeader.length - 2, 1);
                        readerUnderTest.readHeader(partBuffer);
                        expect(readerUnderTest.isComplete()).toBeFalse();
                        readerUnderTest.readHeader(middlePartBuffer);
                        expect(readerUnderTest.isComplete()).toBeFalse();
                        readerUnderTest.readHeader(restOfItBuffer);
                        expect(readerUnderTest.isComplete()).toBeTrue();
                        expect(readerUnderTest.getValue()).toEqual(singleFirstByteValue);
                    });
                    describe("with an empty part in the middle", ()-> {
                        it("should read the correct header value", () -> {
                            ByteBuffer middlePartBuffer = ByteBuffer.wrap(new byte[0]);
                            readerUnderTest.readHeader(partBuffer);
                            expect(readerUnderTest.isComplete()).toBeFalse();
                            readerUnderTest.readHeader(middlePartBuffer);
                            expect(readerUnderTest.isComplete()).toBeFalse();
                            readerUnderTest.readHeader(restOfItBuffer);
                            expect(readerUnderTest.isComplete()).toBeTrue();
                            expect(readerUnderTest.getValue()).toEqual(singleFirstByteValue);
                        });
                    });
                });
            });
            describe("one single read, that is, all at once", ()-> {
            	describe("but in a ByteBuffer with old data at the beginning", ()-> {
                	beforeEach(() -> {
                		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + completeBuffer.capacity());
                		buffer.putLong(123456789L).put(completeBuffer);
                		readerUnderTest.readHeader(buffer, Long.BYTES);
                	});
                    it("should read the correct header value", ()->{
                        expect(readerUnderTest.isComplete()).toBeTrue();
                    });
                    it("should have 0 bytes remaining", () -> {
                    	expect(readerUnderTest.remaining()).toEqual(0);
                    });
                    it("should have the correct value", () -> {
                    	expect(readerUnderTest.getValue()).toEqual(singleFirstByteValue);
                    });
            	});
            });
            describe("does not arrive", ()-> {
            	beforeEach(() -> {
            		ByteBuffer emptyBuffer = ByteBuffer.wrap(new byte[0]);
            		readerUnderTest.readHeader(emptyBuffer);
            	});
                it("should not complete", ()->{
                    expect(readerUnderTest.isComplete()).toBeFalse();
                });
                it("should have 4 bytes remaining", ()->{
                	expect(readerUnderTest.remaining()).toEqual(Integer.BYTES);
                });
                it("should throw an exception for any attempt to get the value", () -> {
            		expect(() -> readerUnderTest.getValue()).toThrow(IllegalStateException.class);
            	});
            });

            describe("When an IntHeaderReader is constructed without an ID", () -> {
            	it("should construct without error", IntHeaderReader::new);
            });

        }); // end when header data... suite
    }

}
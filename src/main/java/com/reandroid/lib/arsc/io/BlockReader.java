package com.reandroid.lib.arsc.io;

import com.reandroid.lib.arsc.header.HeaderBlock;

import java.io.*;


public class BlockReader extends InputStream {
    private final Object mLock=new Object();
    private byte[] BUFFER;
    private final int mStart;
    private final int mLength;
    private int mPosition;
    private boolean mIsClosed;
    private int mMark;
    public BlockReader(byte[] buffer, int start, int length) {
        this.BUFFER=buffer;
        this.mStart=start;
        this.mLength=length;
        this.mPosition =0;
    }
    public BlockReader(byte[] buffer) {
        this(buffer, 0, buffer.length);
    }
    public BlockReader(InputStream in) throws IOException {
        this(loadBuffer(in));
    }
    public BlockReader(File file) throws IOException {
        this(loadBuffer(file));
    }
    public HeaderBlock readHeaderBlock() throws IOException {
        int pos=getPosition();
        HeaderBlock headerBlock=new HeaderBlock((short)0);
        try{
            headerBlock.readBytes(this);
        }catch (EOFException ex){
            return null;
        }
        seek(pos);
        return headerBlock;
    }
    public int searchNextIntPosition(int bytesOffset, int value){
        if(mIsClosed || mPosition>=mLength){
            return -1;
        }
        synchronized (mLock){
            int actPos=mStart+mPosition+bytesOffset;
            int max=available()/4;
            for(int i=0;i<max;i++){
                int pos=actPos+(i*4);
                int valCur=toInt(BUFFER, pos);
                if(valCur==value){
                    return pos-mStart;
                }
            }
            return -1;
        }
    }
    private int toInt(byte[] bts, int offset){
        return bts[offset] & 0xff |
                (bts[offset+1] & 0xff) << 8 |
                (bts[offset+2] & 0xff) << 16 |
                (bts[offset+3] & 0xff) << 24;
    }
    public byte[] getBuffer(){
        return BUFFER;
    }
    public BlockReader create(int start, int len){
        int max=start+len;
        if(len<0 || max> mLength){
            len= mLength -start;
        }
        start=start+mStart;
        return new BlockReader(BUFFER, start, len);
    }
    public boolean isAvailable(){
        if(mIsClosed){
            return false;
        }
        return available()>0;
    }
    public void offset(int off){
        int pos=getPosition()+off;
        seek(pos);
    }
    public void seek(int relPos){
        if(relPos<0){
            relPos=0;
        }else if(relPos>length()){
            relPos=length();
        }
        setPosition(relPos);
    }
    private void setPosition(int pos){
        if(pos==mPosition){
            return;
        }
        synchronized (mLock){
            mPosition=pos;
        }
    }
    public int length(){
        return mLength;
    }
    public byte[] readBytes(int len) throws IOException {
        byte[] result=new byte[len];
        if(len==0){
            return result;
        }
        int len2=read(result);
        if(len2<0){
            throw new EOFException("Finished reading: "+ mPosition);
        }
        if(len==len2){
            return result;
        }
        byte[] result2=new byte[len2];
        System.arraycopy(result, 0, result2, 0, len2);
        return result2;
    }
    public int lengthUntilNextZero(int offset, int zeroCount){
        if(mIsClosed || mPosition>=mLength){
            return 0;
        }
        synchronized (mLock){
            int actPos=mStart+mPosition-1;
            int max=available();
            int remZero=zeroCount;
            int len=0;
            for(int i=0;i<max;i++){
                actPos++;
                len++;
                if(i<offset){
                    continue;
                }
                if(BUFFER[actPos]!=0){
                    remZero=zeroCount;
                    continue;
                }
                remZero--;
                if(remZero<=0){
                    break;
                }
            }
            return len;
        }
    }
    public int lengthUntilLastZero(int offset){
        if(mIsClosed || mPosition>=mLength){
            return 0;
        }
        synchronized (mLock){
            int actPos=mStart+mPosition-1;
            int max=available();
            int len=0;
            boolean zeroFound=false;
            for(int i=0;i<max;i++){
                actPos++;
                len++;
                if(i<offset){
                    continue;
                }
                byte b=BUFFER[actPos];
                if(b==0){
                    zeroFound=true;
                }else if(zeroFound){
                    len--;
                    break;
                }
            }
            return len;
        }
    }
    public int readFully(byte[] bts) throws IOException{
        return readFully(bts, 0, bts.length);
    }
    public int readFully(byte[] bts, int length) throws IOException{
        if(length==0){
            return 0;
        }
        return readFully(bts, 0, length);
    }

    public int skipByteValues(byte b) throws IOException {
        if(mIsClosed){
            throw new IOException("Stream is closed");
        }
        if(mPosition>=mLength){
            throw new EOFException("Finished reading: "+mPosition);
        }
        synchronized (mLock){
            int i=0;
            while (mPosition<mLength){
                if(BUFFER[mStart+mPosition]!=b){
                    return i;
                }
                mPosition++;
                i++;
            }
            return i;
        }
    }
    public int readFully(byte[] bts, int start, int length) throws IOException {
        if(length==0){
            return 0;
        }
        if(mIsClosed){
            throw new IOException("Stream is closed");
        }
        if(mPosition>=mLength){
            throw new EOFException("Finished reading: "+mPosition);
        }
        int len=bts.length;
        if(length<len){
            len=length;
        }
        synchronized (mLock){
            int actPos=mStart+mPosition;
            int j;
            for(j=0;j<len;j++){
                bts[start+j]=BUFFER[actPos+j];
                mPosition++;
                if(mPosition>=mLength){
                    j++;
                    break;
                }
            }
            return j;
        }
    }
    public int getPosition(){
        return mPosition;
    }
    public int getActualPosition(){
        return mStart + mPosition;
    }
    public int getStartPosition(){
        return mStart;
    }
    @Override
    public int read() throws IOException {
        if(mIsClosed){
            throw new IOException("Stream is closed");
        }
        int i=mPosition;
        if(i>=mLength){
            throw new EOFException("Finished reading: "+i);
        }
        synchronized (mLock){
            int actPos=mStart+i;
            int val=BUFFER[actPos] & 0xff;
            mPosition++;
            return val;
        }
    }
    @Override
    public void mark(int pos){
        mMark=pos;
    }
    @Override
    public int available(){
        return mLength-mPosition;
    }
    @Override
    public void reset() throws IOException{
        if(mIsClosed){
            throw new IOException("Can not reset stream is closed");
        }
        mPosition=mMark;
    }
    @Override
    public void close(){
        mIsClosed=true;
        BUFFER=null;
        mMark=0;
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        if(mIsClosed){
            builder.append("Closed");
        }else{
            int av=available();
            if(av==0){
                builder.append("Finished: ");
                builder.append(getPosition());
            }else {
                if(mStart>0){
                    builder.append("START=");
                    builder.append(mStart);
                    builder.append(", ACTUAL=");
                    builder.append(getActualPosition());
                    builder.append(", ");
                }
                builder.append("POS=");
                builder.append(getPosition());
                builder.append(", available=");
                builder.append(av);
            }
        }
        return builder.toString();
    }


    private static byte[] loadBuffer(File file) throws IOException {
        FileInputStream in=new FileInputStream(file);
        return loadBuffer(in);
    }
    private static byte[] loadBuffer(InputStream in) throws IOException {
        byte[] result=new byte[0];
        byte[] buff=new byte[40960];
        int len;
        while((len=in.read(buff))>0){
            result=add(result, buff, len);
        }
        in.close();
        return result;
    }
    private static byte[] add(byte[] arr1, byte[] arr2, int len){
        byte[] result=new byte[arr1.length + len];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, len);
        return result;
    }
}

package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.BaseChunk;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ResXmlString;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;

import java.io.IOException;


public class BaseXmlChunk extends BaseChunk {

    private final IntegerItem mLineNumber;
    private final IntegerItem mCommentReference;
    private final IntegerItem mNamespaceReference;
    private final IntegerItem mStringReference;
    BaseXmlChunk(ChunkType chunkType, int initialChildesCount) {
        super(chunkType, initialChildesCount+2);
        this.mLineNumber=new IntegerItem();
        this.mCommentReference =new IntegerItem(-1);

        this.mNamespaceReference=new IntegerItem(-1);
        this.mStringReference=new IntegerItem(-1);

        addToHeader(mLineNumber);
        addToHeader(mCommentReference);

        addChild(mNamespaceReference);
        addChild(mStringReference);
    }


    public void setLineNumber(int val){
        mLineNumber.set(val);
    }
    public int getLineNumber(){
        return mLineNumber.get();
    }
    public void setCommentReference(int val){
        mLineNumber.set(val);
    }
    public int getCommentReference(){
        return mCommentReference.get();
    }
    public void setNamespaceReference(int val){
        mNamespaceReference.set(val);
    }
    public int getNamespaceReference(){
        return mNamespaceReference.get();
    }
    public void setStringReference(int val){
        mStringReference.set(val);
    }
    public int getStringReference(){
        return mStringReference.get();
    }


    public ResXmlStringPool getStringPool(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof ResXmlBlock){
                return ((ResXmlBlock)parent).getStringPool();
            }
            if(parent instanceof ResXmlElement){
                return ((ResXmlElement)parent).getStringPool();
            }
            parent=parent.getParent();
        }
        return null;
    }
    public ResXmlString getResXmlString(int ref){
        if(ref<0){
            return null;
        }
        ResXmlStringPool stringPool=getStringPool();
        if(stringPool!=null){
            return stringPool.get(ref);
        }
        return null;
    }
    String getString(int ref){
        ResXmlString xmlString=getResXmlString(ref);
        if(xmlString!=null){
            return xmlString.get();
        }
        return null;
    }

    public String getName(){
        return getString(getStringReference());
    }
    public String getUri(){
        return getString(getNamespaceReference());
    }
    public ResXmlElement getParentResXmlElement(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof ResXmlElement){
                return (ResXmlElement)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }
    @Override
    protected void onChunkRefreshed() {

    }
    @Override
    public String toString(){
        ChunkType chunkType=getHeaderBlock().getChunkType();
        if(chunkType==null){
            return super.toString();
        }
        StringBuilder builder=new StringBuilder();
        builder.append(chunkType.toString());
        builder.append(": line=");
        builder.append(getLineNumber());
        builder.append(" {");
        builder.append(getName());
        builder.append("}");
        return builder.toString();
    }
}

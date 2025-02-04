package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ResXmlString;

public class ResXmlText extends BaseXmlChunk {
    private final IntegerItem mReserved;
    public ResXmlText() {
        super(ChunkType.XML_CDATA, 1);
        this.mReserved=new IntegerItem();
        addChild(mReserved);
    }
    public String getText(){
        ResXmlString xmlString=getResXmlString(getTextReference());
        if(xmlString!=null){
            return xmlString.getHtml();
        }
        return null;
    }
    public int getTextReference(){
        return getNamespaceReference();
    }
    public void setTextReference(int ref){
        setNamespaceReference(ref);
    }
    @Override
    public String toString(){
        String txt=getText();
        if(txt!=null){
            return "TEXT: line="+getLineNumber()+" >"+txt+"<";
        }
        return super.toString();
    }
}

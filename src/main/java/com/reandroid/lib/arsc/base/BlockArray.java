package com.reandroid.lib.arsc.base;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;


public abstract class BlockArray<T extends Block> extends BlockContainer<T> implements BlockArrayCreator<T>  {
    private T[] elementData;
    public BlockArray(){
        elementData= newInstance(0);
    }
    public List<T> listItems(){
        return new AbstractList<T>() {
            @Override
            public T get(int i) {
                return BlockArray.this.get(i);
            }
            @Override
            public int size() {
                return BlockArray.this.childesCount();
            }
        };
    }
    @Override
    public T[] getChildes(){
        return elementData;
    }
    public void ensureSize(int size){
        if(size<= childesCount()){
            return;
        }
        setChildesCount(size);
    }
    public void setChildesCount(int count){
        if(count<0){
            count=0;
        }
        if(count==0){
            clearChildes();
            return;
        }
        int diff = count - childesCount();
        if(diff==0){
            return;
        }
        changeSize(diff);
    }
    public final void clearChildes(){
        T[] allChildes=elementData;
        if(allChildes==null || allChildes.length==0){
            return;
        }
        int max=allChildes.length;
        for(int i=0;i<max;i++){
            T block=allChildes[i];
            if(block==null){
                continue;
            }
            block.setIndex(-1);
            block.setParent(null);
            allChildes[i]=null;
        }
        elementData=newInstance(0);
    }

    public void addAll(T[] blocks){
        if(blocks==null||blocks.length==0){
            return;
        }
        T[] old=elementData;
        if(old==null){
            old=newInstance(0);
        }
        int oldLen=old.length;
        int len=blocks.length;
        T[] update= newInstance(oldLen+len);
        if(oldLen>0){
            System.arraycopy(old, 0, update, 0, oldLen);
        }
        boolean foundNull=false;
        for(int i=0;i<len;i++){
            T item=blocks[i];
            if(item==null){
                foundNull=true;
                continue;
            }
            int index=oldLen + i;
            update[index]=item;
            item.setParent(this);
            item.setIndex(index);
        }
        elementData=update;
        if(foundNull){
            trimNullBlocks();
        }
    }
    public void add(T block){
        if(block==null){
            return;
        }
        T[] old=elementData;
        int index=old.length;
        elementData= newInstance(index+1);
        if(index>0){
            System.arraycopy(old, 0, elementData, 0, index);
        }
        elementData[index]=block;
        block.setIndex(index);
        block.setParent(this);
    }
    public final int childesCount(){
        return elementData.length;
    }
    public final T createNext(){
        T block=newInstance();
        add(block);
        return block;
    }
    public final T get(int i){
        if(i >= childesCount() || i<0){
            return null;
        }
        return elementData[i];
    }
    public boolean contains(T block){
        T[] items=elementData;
        if(block==null || items==null){
            return false;
        }
        int len=items.length;
        for(int i=0;i<len;i++){
            if(block==items[i]){
                return true;
            }
        }
        return false;
    }
    public boolean remove(T block){
        T[] items=elementData;
        if(block==null||items==null){
            return false;
        }
        boolean found=false;
        int len=items.length;
        for(int i=0;i<len;i++){
            if(block==items[i]){
                items[i]=null;
                found=true;
            }
        }
        if(found){
            trimNullBlocks();
        }
        return found;
    }
    private void trimNullBlocks(){
        T[] items=elementData;
        if(items==null){
            return;
        }
        int count=countNonNull();
        int len=items.length;
        if(count==len){
            return;
        }
        T[] update=newInstance(count);
        int index=0;
        for(int i=0;i<len;i++){
            T block=items[i];
            if(block!=null){
                update[index]=block;
                block.setIndex(index);
                index++;
            }
        }
        elementData=update;
    }
    private int countNonNull(){
        T[] items=elementData;
        if(items==null){
            return 0;
        }
        int result=0;
        for(T block:items){
            if(block!=null){
                result++;
            }
        }
        return result;
    }
    private void changeSize(int amount){
        T[] old=elementData;
        int index=old.length;
        int size=index+amount;
        T[] update= newInstance(size);
        int end;
        if(index>size){
            end=size;
        }else {
            end=index;
        }
        if(end>0){
            System.arraycopy(old, 0, update, 0, end);
        }
        for(int i=end;i<size;i++){
            T item=newInstance();
            update[i]=item;
            item.setIndex(i);
            item.setParent(this);
        }
        elementData=update;
        Arrays.fill(old, null);
    }

    @Override
    public String toString(){
        return "count="+ childesCount();
    }
}
package com.reandroid.lib.arsc.pool;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.array.StringArray;
import com.reandroid.lib.arsc.array.StyleArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.BaseChunk;
import com.reandroid.lib.arsc.io.BlockLoad;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public abstract class BaseStringPool<T extends StringItem> extends BaseChunk implements BlockLoad {
    private final IntegerItem mCountStrings;
    private final IntegerItem mCountStyles;
    private final ShortItem mFlagUtf8;
    private final ShortItem mFlagSorted;
    private final IntegerItem mStartStrings;
    private final IntegerItem mStartStyles;
    private final IntegerArray mOffsetStrings;
    private final IntegerArray mOffsetStyles;
    private final StringArray<T> mArrayStrings;
    private final StyleArray mArrayStyles;

    private final Map<String, StringGroup<T>> mUniqueMap;


    BaseStringPool(boolean is_utf8){
        super(ChunkType.STRING, 4);

        this.mCountStrings=new IntegerItem(); //header
        this.mCountStyles=new IntegerItem(); //header
        this.mFlagUtf8 =new ShortItem(); //header
        this.mFlagSorted=new ShortItem(); //header
        this.mStartStrings=new IntegerItem(); //header
        this.mStartStyles=new IntegerItem(); //header

        this.mOffsetStrings=new IntegerArray();//1
        this.mOffsetStyles=new IntegerArray(); //2
        this.mArrayStrings=newInstance(mOffsetStrings, mCountStrings, mStartStrings, is_utf8); //3
        this.mArrayStyles=new StyleArray(mOffsetStyles, mCountStyles, mStartStyles); //4

        addToHeader(mCountStrings);
        addToHeader(mCountStyles);
        addToHeader(mFlagUtf8);
        addToHeader(mFlagSorted);
        addToHeader(mStartStrings);
        addToHeader(mStartStyles);

        addChild(mOffsetStrings);
        addChild(mOffsetStyles);
        addChild(mArrayStrings);
        addChild(mArrayStyles);

        setUtf8(is_utf8, false);

        mFlagUtf8.setBlockLoad(this);

        mUniqueMap=new HashMap<>();

    }
    public boolean contains(String str){
        return mUniqueMap.containsKey(str);
    }
    public final T get(int index){
        return mArrayStrings.get(index);
    }
    public final StringGroup<T> get(String str){
        return mUniqueMap.get(str);
    }
    public final T getOrCreate(String str){
        StringGroup<T> group=getOrCreateGroup(str);
        T[] items=group.getItems();
        if(items.length==0){
            T t=createNewString(str);
            group.add(t);
            items=group.getItems();
        }
        return items[0];
    }
    private StringGroup<T> getOrCreateGroup(String str){
        StringGroup<T> group=get(str);
        if(group!=null){
            return group;
        }
        T[] items=mArrayStrings.newInstance(0);
        group=new StringGroup<>(mArrayStrings, str, items);
        mUniqueMap.put(str, group);
        return group;
    }
    private T createNewString(String str){
        T item=mArrayStrings.createNext();
        item.set(str);
        mCountStrings.set(mArrayStrings.childesCount());
        return item;
    }
    private void reUpdateUniqueMap(){
        mUniqueMap.clear();
        T[] allChildes=getStrings();
        if(allChildes==null){
            return;
        }
        int max=allChildes.length;
        for(int i=0;i<max;i++){
            T item=allChildes[i];
            if(item==null){
                continue;
            }
            String str=item.get();
            if(str==null){
                continue;
            }
            updateUniqueMap(item);
        }
    }
    private void updateUniqueMap(T item){
        String str=item.get();
        StringGroup<T> group= getOrCreateGroup(str);
        group.add(item);
    }
    public final StyleItem getStyle(int index){
        return mArrayStyles.get(index);
    }
    public final int countStrings(){
        return mArrayStrings.childesCount();
    }
    public final int countStyles(){
        return mArrayStyles.childesCount();
    }

    public final T[] getStrings(){
        return mArrayStrings.getChildes();
    }
    public final StyleItem[] getStyles(){
        return mArrayStyles.getChildes();
    }
    private void setUtf8Flag(short flag){
        mFlagUtf8.set(flag);
    }
    public void setUtf8(boolean is_utf8){
        setUtf8(is_utf8, true);
    }
    private void setSortedFlag(short flag){
        mFlagSorted.set(flag);
    }
    public final void setSorted(boolean sorted){
        if(sorted){
            setSortedFlag(FLAG_SORTED);
        }else {
            setSortedFlag((short)0);
        }
    }
    private void setUtf8(boolean is_utf8, boolean updateAll){
        boolean old= isUtf8Flag();
        if(is_utf8){
            setUtf8Flag(UTF8_FLAG_VALUE);
        }else {
            setUtf8Flag((short) 0);
        }
        if(!updateAll || old == isUtf8Flag()){
            return;
        }
        mArrayStrings.setUtf8(is_utf8);
    }
    private boolean isUtf8Flag(){
        return (mFlagUtf8.get() & FLAG_UTF8) !=0;
    }
    private boolean isSortedFlag(){
        return (mFlagSorted.get() & FLAG_SORTED) !=0;
    }


    abstract StringArray<T> newInstance(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8);
    @Override
    protected void onChunkRefreshed() {

    }
    @Override
    public void onChunkLoaded() {
        reUpdateUniqueMap();
    }

    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender== mFlagUtf8){
            mArrayStrings.setUtf8(isUtf8Flag());
        }
    }
    private static final short UTF8_FLAG_VALUE=0x0100;

    private static final short FLAG_UTF8 = 0x0100;
    private static final short FLAG_SORTED = 0x0100;
}
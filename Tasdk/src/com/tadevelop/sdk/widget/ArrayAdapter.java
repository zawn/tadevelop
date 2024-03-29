/*
 * Copyright (C) 2006 The Android Open Source Project
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

package  com.tadevelop.sdk.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

/**
 * 
 * *************************************
 * <pre>
 * Copied from AOSP:
 * https://android.googlesource.com/platform/frameworks/base.git/+/android-4.4.4_r2/core/java/android/widget/ArrayAdapter.java
 *
 * 更改如下:
 *    1.将类变为抽象类.
 *    2.添加抽象方法{@link #attachDataToView(int position, T item, View view)};
 *    3.添加{@link #getList()}用于返回包含当前与Adapter关联的内容.
 *    4.在构造函数{@link #ArrayAdapter(Context, int, Object[])}中添加初始化空数组的支持.
 *    5.添加双向队列支持,{@link #addFirst(Object)},{@link #addLast(Object)}.主要用于简化下拉刷新等应用场景的数据插入操作.
 *    6.更改字段{@link #mOriginalValues}为List<T>.
 *    
 *  @author ZhangZhenli
 * </pre>
 * *************************************
 * <br>
 * 
 * A concrete BaseAdapter that is backed by an array of arbitrary
 * objects.  By default this class expects that the provided resource id references
 * a single TextView.  If you want to use a more complex layout, use the constructors that
 * also takes a field id.  That field id should reference a TextView in the larger layout
 * resource.
 *
 * <p>However the TextView is referenced, it will be filled with the toString() of each object in
 * the array. You can add lists or arrays of custom objects. Override the toString() method
 * of your objects to determine what text will be displayed for the item in the list.
 *
 * <p>To use something other than TextViews for the array display, for instance, ImageViews,
 * or to have some of data besides toString() results fill the views,
 * override {@link #getView(int, View, ViewGroup)} to return the type of view you want.
 */
abstract public class ArrayAdapter<T> extends BaseAdapter implements Filterable {
    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<T> mObjects;

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    private final Object mLock = new Object();

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private int mResource;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private int mDropDownResource;

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mObjects} is modified.
     */
    private boolean mNotifyOnChange = true;

    private Context mContext;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private List<T> mOriginalValues;
    private ArrayFilter mFilter;

    private LayoutInflater mInflater;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     */
    public ArrayAdapter(Context context, int resource) {
        init(context, resource, new ArrayList<T>());
    }


    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects The objects to represent in the ListView.
     */
    public ArrayAdapter(Context context, int resource, T[] objects) {
    	List<T> asList;
    	if (objects == null || objects.length == 0) {
    		asList = new ArrayList<T>();
		}else{
			asList = Arrays.asList(objects);
		}
		init(context, resource, asList);
    }


    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects The objects to represent in the ListView.
     */
    public ArrayAdapter(Context context, int resource, List<T> objects) {
        init(context, resource, objects);
    }


    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mObjects.add(object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAll(Collection<? extends T> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mObjects.addAll(collection);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(T ... items) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.addAll(mOriginalValues, items);
            } else {
                Collections.addAll(mObjects, items);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }
    
    /**
     * 转换当前{@link #mObjects},{@link #mOriginalValues}为支持双端队列的List类型.
     */
    private void ensureDeque() {
        synchronized (mLock) {
            if (mOriginalValues != null && !(mOriginalValues instanceof LinkedList<?>)) {
                mOriginalValues = new LinkedList<T>(mOriginalValues);
            }
            if (mObjects != null && !(mObjects instanceof LinkedList<?>)) {
                mObjects = new LinkedList<T>(mObjects);
            }
        }
    }

    /**
     * Adds the specified object at the beginning of this {@code ArrayAdapter}.
     *
     * @param object
     *            the object to add.
     */
    public void addFirstAll(Collection<? extends T> collection) {
        ensureDeque();
        synchronized (mLock) {
            if (mOriginalValues != null) {
                ((LinkedList<T>) mOriginalValues).addAll(0, collection);
            } else {
                ((LinkedList<T>) mObjects).addAll(0, collection);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified object at the beginning of this {@code ArrayAdapter}.
     *
     * @param object
     *            the object to add.
     */
    public void addFirstAll(T... items) {
        ensureDeque();
        synchronized (mLock) {
            if (mOriginalValues != null) {
                ((LinkedList<T>) mOriginalValues).addAll(0, Arrays.asList(items));
            } else {
                ((LinkedList<T>) mObjects).addAll(0, Arrays.asList(items));
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified object at the beginning of this {@code ArrayAdapter}.
     *
     * @param object
     *            the object to add.
     */
    public void addFirst(T object) {
        ensureDeque();
        synchronized (mLock) {
            if (mOriginalValues != null) {
                ((LinkedList<T>) mOriginalValues).addFirst(object);
            } else {
                ((LinkedList<T>) mObjects).addFirst(object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void addLast(T object) {
        ensureDeque();
        synchronized (mLock) {
            if (mOriginalValues != null) {
                ((LinkedList<T>) mOriginalValues).addLast(object);
            } else {
                ((LinkedList<T>) mObjects).addLast(object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified object at the end of this {@code ArrayAdapter}.
     *
     * @param object
     *            the object to add.
     */
    public void addLastAll(Collection<? extends T> collection) {
        ensureDeque();
        synchronized (mLock) {
            if (mOriginalValues != null) {
                ((LinkedList<T>) mOriginalValues).addAll(mOriginalValues.size(), collection);
            } else {
                ((LinkedList<T>) mObjects).addAll(mObjects.size(), collection);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified object at the end of this {@code ArrayAdapter}.
     *
     * @param object
     *            the object to add.
     */
    public void addLastAll(T... items) {
        ensureDeque();
        synchronized (mLock) {
            if (mOriginalValues != null) {
                ((LinkedList<T>) mOriginalValues).addAll(mOriginalValues.size(), Arrays.asList(items));
            } else {
                ((LinkedList<T>) mObjects).addAll(mObjects.size(), Arrays.asList(items));
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public T removeFirst() {
        ensureDeque();
        T removeFirst;
        synchronized (mLock) {
            if (mOriginalValues != null) {
                removeFirst = ((LinkedList<T>) mOriginalValues).removeFirst();
            } else {
                removeFirst = ((LinkedList<T>) mObjects).removeFirst();
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
        return removeFirst;
    }

    public T removeLast() {
        ensureDeque();
        T removeLast;
        synchronized (mLock) {
            if (mOriginalValues != null) {
                removeLast = ((LinkedList<T>) mOriginalValues).removeLast();
            } else {
                removeLast = ((LinkedList<T>) mObjects).removeLast();
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
        return removeLast;
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     */
    public void insert(T object, int index) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(index, object);
            } else {
                mObjects.add(index, object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(object);
            } else {
                mObjects.remove(object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * 获取元数据列表
     * 
     * @return 数据列表
     */
    public List<T> getList() {
        if (mOriginalValues != null) {
            return mOriginalValues;
        } else {
            return mObjects;
        }
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mObjects.clear();
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *        in this adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator);
            } else {
                Collections.sort(mObjects, comparator);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}.  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     *
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link
     *                       #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    private void init(Context context, int resource, List<T> objects) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = mDropDownResource = resource;
        mObjects = objects;
    }

    /**
     * Returns the context associated with this array adapter. The context is used
     * to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * {@inheritDoc}
     */
    public int getCount() {
        return mObjects.size();
    }

    /**
     * {@inheritDoc}
     */
    public T getItem(int position) {
        return mObjects.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    public int getPosition(T item) {
        return mObjects.indexOf(item);
    }

    /**
     * {@inheritDoc}
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent,
            int resource) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        return attachDataToView(position, getItem(position), view);
    }

    /**
     * 将数据与视图组件相关联
     * 
     * @param position
     * @param item 数据item
     * @param view 需要关联的视图组件
     * @return 返回关联之后的视图组件
     */
    abstract public View attachDataToView(int position, T item, View view);

    /**
     * <p>Sets the layout resource to create the drop down views.</p>
     *
     * @param resource the layout resource defining the drop down views
     * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    public void setDropDownViewResource(int resource) {
        this.mDropDownResource = resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownResource);
    }


    /**
     * {@inheritDoc}
     */
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<T>(mObjects);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<T> list;
                synchronized (mLock) {
                    list = new ArrayList<T>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<T> values;
                synchronized (mLock) {
                    values = new ArrayList<T>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<T>();

                for (int i = 0; i < count; i++) {
                    final T value = values.get(i);
                    final String valueText = value.toString().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mObjects = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}

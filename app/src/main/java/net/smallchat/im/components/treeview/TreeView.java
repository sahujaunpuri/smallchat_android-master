package net.smallchat.im.components.treeview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * ����TreeView.java
 * ��������ʵ�����νṹ��view
 * @author wader
 * ����ʱ�䣺2011-11-03 16:32
 */
public class TreeView extends ListView implements OnItemClickListener {
    String TAG = "TreeView";
    List<TreeElement> treeElements = null;// ���нڵ㼯��
    List<TreeElement> currentElements = null;// ��ǰ��ʾ�Ľڵ㼯��
    List<TreeElement> tempElements = null;// ������ʱ�洢
    List<TreeElement> treeElementsToDel; // ��ʱ�洢��ɾ��Ľڵ�
    TreeViewAdapter adapter = null;// ����������
    LastLevelItemClickListener itemClickCallBack;// �û�����¼��ص�
 
    public TreeView(final Context context, AttributeSet attrs) {
       super(context, attrs);
       Log.d(TAG, "create with TreeView(Context context, AttributeSet attrs)");
       treeElements = new ArrayList<TreeElement>();
       currentElements = new ArrayList<TreeElement>();
 
       adapter = new TreeViewAdapter(context, currentElements);
       this.setAdapter(adapter);
       itemClickCallBack = new LastLevelItemClickListener() {
           @Override
           public void onLastLevelItemClick(int position,TreeViewAdapter adapter) {
              Log.d(TAG, "last level element "
                     + currentElements.get(position).getTitle()
                     + " is clicked");
              Toast.makeText(context,
                     currentElements.get(position).getTitle(), 200).show();
           }
       };
       this.setOnItemClickListener(this);
    }
 
    public void initData(Context context, List<TreeElement> treeElements) {
       this.treeElements = treeElements;
       getFirstLevelElements(context);
       adapter.notifyDataSetChanged();
    }
 
    /**
     * ���õ���¼��ص��ӿ�
     *
     * @param itemClickCallBack
     */
 
    public void setLastLevelItemClickCallBack(LastLevelItemClickListener itemClickCallBack) {
       this.itemClickCallBack = itemClickCallBack;
    }
 
    /**
     * ��ʼ�����νṹ�б����,�ѵ�һ�㼶�������ӵ�currentElements��
     */
    public void getFirstLevelElements(Context context) {
       Log.e(TAG, "initCurrentElements");
       int size = treeElements.size();
       Log.d(TAG, "tree elements num is: " + size);
       if (currentElements == null) {
           currentElements = new ArrayList<TreeElement>();
       }
       currentElements.clear();
       for (int i = 0; i < size; i++) {
           if (treeElements.get(i).getLevel() == 1) {
              currentElements.add(treeElements.get(i));
              Log.d(TAG, "find a first level element: " + treeElements.get(i));
 
           }
       }
    }
 
    /**
     * �����нڵ㼯���л�ȡĳ���ڵ���ӽڵ㼯��
     *
     * @param parentId
     * @return
     */
    private List<TreeElement> getChildElementsFromAllById(String parentId) {
       tempElements = new ArrayList<TreeElement>();
       int size = treeElements.size();
 
       for (int i = 0; i < size; i++) {
           if (treeElements.get(i).getParentId().equalsIgnoreCase(parentId)) {
              tempElements.add(treeElements.get(i));
              Log.d(TAG, "find a child element�� " + treeElements.get(i));
           }
       }
       return tempElements;
    }
 
    /**
     * �ӵ�ǰ��ʾ�Ľڵ㼯���л�ȡĳ���ڵ���ӽڵ㼯��
     *
     * @param parentId
     * @return
     */
    private List<TreeElement> getChildElementsFromCurrentById(String parentId) {
       Log.d(TAG, "getChildElementsFromCurrentById    parentId�� " + parentId);
       if (tempElements == null) {
           tempElements = new ArrayList<TreeElement>();
       } else {
           tempElements.clear();
       }
 
       int size = currentElements.size();
       for (int i = 0; i < size; i++) {
           if (currentElements.get(i).getParentId().equalsIgnoreCase(parentId)) {
              tempElements.add(currentElements.get(i));
              Log.d(TAG,
                     "find a child element to delete�� "
                            + currentElements.get(i));
           }
       }
 
       return tempElements;
    }
 
    /**
     * ɾ��ĳ���ڵ�������ӽڵ㼯��
     *
     * @param parentId
     * @return
     */
    private synchronized boolean delAllChildElementsByParentId(String parentId) {
       Log.e(TAG, "delAllChildElementsByParentId: " + parentId);
       int size;
       TreeElement tempElement = currentElements
              .get(getElementIndexById(parentId));
       List<TreeElement> childElments = getChildElementsFromCurrentById(parentId);
       if (treeElementsToDel == null) {
           treeElementsToDel = new ArrayList<TreeElement>();
       } else {
           treeElementsToDel.clear();
       }
       size = childElments.size();
       Log.e(TAG, "childElments size : " + size);
       for (int i = 0; i < size; i++) {
           tempElement = childElments.get(i);
 
           if (tempElement.hasChild && tempElement.fold) {
              treeElementsToDel.add(tempElement);
           }
       }
       size = treeElementsToDel.size();
       Log.e(TAG, "treeElementsToDel size : " + size);
 
       for (int i = size - 1; i >= 0; i--) {
           delAllChildElementsByParentId(treeElementsToDel.get(i).getId());
       }
       delDirectChildElementsByParentId(parentId);
       return true;
    }
 
    /**
     * ɾ��ĳ���ڵ��ֱ���ӽڵ㼯��
     *
     * @param parentId
     * @return
     */
    private synchronized boolean delDirectChildElementsByParentId(
           String parentId) {
       Log.d(TAG, "delDirectChildElementsByParentId(): " + parentId);
       boolean success = false;
       if (currentElements == null || currentElements.size() == 0) {
           Log.d(TAG,
                  "delChildElementsById() failed,currentElements is null or it's size is 0");
           return success;
       }
       synchronized (currentElements) {
           int size = currentElements.size();
           Log.d(TAG, "begin delete");
           for (int i = size - 1; i >= 0; i--) {
              if (currentElements.get(i).getParentId()
                     .equalsIgnoreCase(parentId)) {
                  currentElements.get(i).fold = false;// �ǵ������ӽڵ�ʱ��չ��״̬��Ϊfalse
                  currentElements.remove(i);
              }
           }
        }
       success = true;
       return success;
    }
 
    /**
     * ���tid���±�
     *
     * @param id
     * @return
     */
    private int getElementIndexById(String id) {
       int num = currentElements.size();
       for (int i = 0; i < num; i++) {
           if (currentElements.get(i).getId().equalsIgnoreCase(id)) {
              return i;
           }
       }
       return -1;
    }
 
    @Override
    public void onItemClick(AdapterView<?> arg0, View convertView,
           int position, long id) {
       TreeElement element = currentElements.get(position);
       if (element.isHasChild()) {// ��ǰ�ڵ����ӽڵ�ʱֻ���������ʾ�����صȲ���
           if (!element.isFold()) {// ��ǰ���ڵ�Ϊδչ��״̬
              currentElements.addAll(position + 1,
                     this.getChildElementsFromAllById(element.getId()));
           } else if (element.fold) {// ��ǰ���ڵ�Ϊչ��״̬
              boolean success = this.delAllChildElementsByParentId(element
                     .getId());
              // boolean success =
              // this.delDirectChildElementsByParentId(element
              // .getId());
              Log.d(TAG, "delete child state: " + success);
              if (!success) {
                  return;
              }
           }
           // ������Ϣ
           // Log.d(TAG, "elements in currentElements:\n");
           // for (int i = 0; i < currentElements.size(); i++) {
           // Log.d(TAG + i, currentElements.get(i) + "\n");
           // }
 
           element.setFold(!element.isFold());// ���÷�״̬
           adapter.notifyDataSetChanged();// ˢ�������ʾ
       } else {// ��ǰ�ڵ����ӽڵ�ʱֻ�����û��Զ������
           itemClickCallBack.onLastLevelItemClick(position,adapter);
       }
 
    }
 
    /**
     * �Զ����ڲ��ӿڣ������û�������սڵ�ʱ���¼��ص�
     */
    public interface LastLevelItemClickListener {
       public void onLastLevelItemClick(int position,TreeViewAdapter adapter);
    }
}
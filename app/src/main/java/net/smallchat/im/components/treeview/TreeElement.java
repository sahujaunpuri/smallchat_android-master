package net.smallchat.im.components.treeview;

/**
 * ����TreeElement.java
* �����������νṹ�ڵ���
 * @author wader
 * ����ʱ�䣺2011-11-03 16:32
 */
public class TreeElement {
	String headUrl;
	String sign;
    String id = null;// ��ǰ�ڵ�tid
    String title = null;// ��ǰ�ڵ�����
    String parentTitle = null;//����ڵ�ֵ
    boolean hasParent = false;// ��ǰ�ڵ��Ƿ��и��ڵ�
    boolean hasChild = false;// ��ǰ�ڵ��Ƿ����ӽڵ�
    boolean childShow = false;// ����ӽڵ㣬�ֽڵ���ǰ�Ƿ�����ʾ
    String parentId = null;// ���ڵ�tid
    int level = -1;// ��ǰ����㼶
    
    boolean fold = false;// �Ƿ���չ��״̬
    boolean isBigItem = false;
 
    public boolean isChildShow() {
       return childShow;
    }
 
    public void setChildShow(boolean childShow) {
       this.childShow = childShow;
    }
 
    public String getId() {
        return id;
    }
 
    public void setId(String id) {
       this.id = id;
    }
 
    public String getTitle() {
       return title;
    }
 
    public void setTitle(String title) {
       this.title = title;
    }
 
    public boolean isHasParent() {
       return hasParent;
    }
 
    public void setHasParent(boolean hasParent) {
       this.hasParent = hasParent;
    }
 
    public boolean isHasChild() {
       return hasChild;
    }
 
    public void setHasChild(boolean hasChild) {
       this.hasChild = hasChild;
    }
 
    public String getParentId() {
       return parentId;
    }
 
    public void setParentId(String parentId) {
       this.parentId = parentId;
    }
 
    public int getLevel() {
       return level;
    }
 
    public void setLevel(int level) {
       this.level = level;
    }
 
    public boolean isFold() {
       return fold;
    }
 
    public void setFold(boolean fold) {
       this.fold = fold;
    }
    
    
 
    public String getHeadUrl() {
		return headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
	
	

	public String getParentTitle() {
		return parentTitle;
	}

	public void setParentTitle(String parentTitle) {
		this.parentTitle = parentTitle;
	}
	

	public boolean isBigItem() {
		return isBigItem;
	}

	public void setBigItem(boolean isBigItem) {
		this.isBigItem = isBigItem;
	}

	@Override
    public String toString() {
       return "id:" + this.id + "-level:" + this.level + "-title:"
              + this.title + "-fold:" + this.fold + "-hasChidl:"
              + this.hasChild + "-hasParent:" + this.hasParent + "-parentId:"+ this.parentId;
    }
}
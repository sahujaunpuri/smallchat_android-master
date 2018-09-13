package net.smallchat.im.components.sortlist;

import java.util.Comparator;

import net.smallchat.im.Entity.Login;

/**
 * 
 * @author xiaanming
 *
 */
public class PinyinComparator implements Comparator<Login> {

	public int compare(Login o1, Login o2) {
		if (o1.sort.equals("@")
				|| o2.sort.equals("#")
				) {
			return -1;
		} else if (o1.sort.equals("#")
				|| o2.sort.equals("@")) {
			return 1;
		}else if( o1.sort.equals("↑")
				|| o2.sort.equals("☆")){
			return 1;
		}else if( o1.sort.equals("☆")
				|| o2.sort.equals("↑")){
			return 2;
		}
		else {
			return o1.sort.compareTo(o2.sort);
		}
	}

}

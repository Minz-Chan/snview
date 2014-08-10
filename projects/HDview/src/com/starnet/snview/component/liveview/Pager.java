package com.starnet.snview.component.liveview;

import java.util.ArrayList;
import java.util.List;

public class Pager {

	private int total; // 项目总数
	private int pageCapacity; // 每页显示个数
	private int pageCount;  // 页数
	private int index; // 当前项索引

	private List<PageNode> pageList;
	private PageNode current;
	
	
	public Pager(int total, int pageCapacity) {
		this(total, pageCapacity, 1);
	}

	public Pager(int total, int pageCapacity, int initIndex) {

		this.total = total;
		this.pageCapacity = pageCapacity;

		int pageCount = ((total % pageCapacity) == 0) ? (total / pageCapacity)
				: (total / pageCapacity + 1);
		
		this.pageCount = pageCount;

		pageList = new ArrayList<PageNode>();

		int i;

		for (i = 0; i < pageCount; i++) {
			pageList.add(new PageNode());
		}

		for (i = 0; i < pageCount; i++) {

			pageList.get(i).setPageNumber(i + 1);

			if (i + 1 < pageCount) {
				pageList.get(i).setNext(pageList.get(i + 1));
			}

			if (i - 1 >= 0) {
				pageList.get(i).setPrevious(pageList.get(i - 1));
			}
		}

		PageNode head = pageList.get(0);
		PageNode tail = pageList.get(pageCount - 1);
		
		head.setPrevious(tail);
		tail.setNext(head);

		index = initIndex;
		
		int currPage = ((index % pageCapacity) == 0) ? (index / pageCapacity) : (index / pageCapacity + 1);
		
		current = head;
		while (current != null) {
			if (current.getPageNumber() == currPage) {
				break;
			}
			
			current = current.getNext();
		}

	}

	public int getTotalCount() {
		return total;
	}

	public void nextPage() {
		current = current.getNext();
		index = (current.getPageNumber() - 1) * pageCapacity + 1;
	}

	public void previousPage() {
		current = current.getPrevious();
		index = (current.getPageNumber() - 1) * pageCapacity + 1; 
	}

	public int getPageCapacity() {
		return pageCapacity;
	}
	
	public int getCurrentPage() {
		return current.getPageNumber();
	}

	public int getCurrentIndex() {
		return index;
	}
	
	public int getCurrentPageCount() {
		int currPage = current.getPageNumber();
		
		if (currPage == pageCount) {
			return total - pageCapacity * (pageCount - 1);
		} else {
			return pageCapacity;
		}
	}

	public void setCurrentIndex(int i) {

		if (i < 1 || i > total) {
			throw new IllegalArgumentException("Error index " + i
					+ ", index should be in [1, " + total + "].");
		}

		index = i;

		int currPage = ((index % pageCapacity) == 0) ? (index / pageCapacity)
				: (index / pageCapacity + 1);

		PageNode p = current;

		do {
			if (p.getPageNumber() == currPage) {
				break;
			}

			p = p.getNext();
		} while (p != null);

		current = p;

	}

	private class PageNode {
		private int page;
		private PageNode previous;
		private PageNode next;

		public int getPageNumber() {
			return page;
		}

		public void setPageNumber(int page) {
			this.page = page;
		}

		public PageNode getPrevious() {
			return previous;
		}

		public void setPrevious(PageNode p) {
			this.previous = p;
		}

		public PageNode getNext() {
			return next;
		}

		public void setNext(PageNode p) {
			this.next = p;
		}
	}
}

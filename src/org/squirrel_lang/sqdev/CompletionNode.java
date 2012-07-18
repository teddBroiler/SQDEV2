package org.squirrel_lang.sqdev;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.swt.graphics.Image;


public class CompletionNode implements IContextInformation {
	public boolean equals(Object object) 
	{
		return object == this;
	}
	public String getContextDisplayString() 
	{
		return display;
	}
	public String getInformationDisplayString() 
	{
		return signature!= null?display+signature:display;
	}
	public Image getImage(){ return icon; }
	static Comparator<CompletionNode> cmp = new Comparator<CompletionNode>(){
		public int compare(CompletionNode a, CompletionNode b) {
			return a.name.compareTo(b.name);
		}};
	public void addChildren(CompletionNode c)
	{
		if(children == null) {
			children = new ArrayList<CompletionNode>();
			
		}
		if(display != null && !display.equals("")) {
			c.display = display + "." + c.display;
		}
		int size = children.size();
		for(int n = 0; n< size; n++)
		{
			CompletionNode node = children.get(n);
			if(node.display.equals(c.display)) {
				children.set(n,c);
				break;
			}
		}
		children.add(c);
		Collections.sort(children,cmp);
		
	}
	public int getCandidates(ArrayList<CompletionNode> ret,String[] s)
	{
		return getCandidates(ret,s,0);
	}
	static String[] empty = new String[] {""};
	public int getCandidates(ArrayList<CompletionNode> ret,String[] syms,int part)
	{
		if(children == null) return 0;
		int len = children.size();
		int found = 0;
		String s = syms[part]; 
		for(int n = 0; n < len; n++) {
			CompletionNode cn = (CompletionNode)children.get(n);
			if(cn.name.startsWith(s)) {
				int temp = found;
				
				if(cn.name.equals(s)) {
					if(part < syms.length-1) {
						found += cn.getCandidates(ret,syms,part+1);
						
					}
					else {
						//force all childrens to be in
						found += cn.getCandidates(ret,empty,0);
					}
				}
				//add the parent only if no children were added
				if(temp == found) {
					found++;
					ret.add(cn);
				}
				
			}
		}
		return found;
	}
	public String name;
	public String display;
	public String type;
	public String signature;
	public Image icon;
	public ArrayList<CompletionNode> children;
}

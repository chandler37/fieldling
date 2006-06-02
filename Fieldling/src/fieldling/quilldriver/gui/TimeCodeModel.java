package fieldling.quilldriver.gui;

import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import org.w3c.dom.Node;
import javax.xml.xpath.XPathExpression;
import fieldling.quilldriver.xml.XPathUtilities;

public class TimeCodeModel {
		private EventListenerList listenerList;
		long t1, t2; //start and stop times in milliseconds
		private TextHighlightPlayer thp;
		private Object currentNode = null;
                private Map config;
                private org.jdom.Namespace[] namespaces;
                //private Action insertTimesAction;
                
                TimeCodeModel(TextHighlightPlayer thp, Map config, org.jdom.Namespace[] namespaces) {
		//TimeCodeModel(TextHighlightPlayer thp, Map config, org.jdom.Namespace[] namespaces, Action insertTimesIntoXMLAction) {
			listenerList = new EventListenerList();
			this.thp = thp;
                        this.config = config;
                        this.namespaces = namespaces;
                        //insertTimesAction = insertTimesIntoXMLAction;
			t1 = 0;
			t2 = 0;
		}
		public void addTimeCodeModelListener(TimeCodeModelListener tcml) {
			listenerList.add(TimeCodeModelListener.class, tcml);
		}
		public void removeTimeCodeModelListener(TimeCodeModelListener tcml) {
			listenerList.remove(TimeCodeModelListener.class, tcml);
		}
		public void removeAllTimeCodeModelListeners() {
			listenerList = new EventListenerList();
		}
		public Long getInTime() {
			return new Long(t1);
		}
		public Long getOutTime() {
			return new Long(t2);
		}
		public Object getCurrentNode() {
			return currentNode;
		}
		/*private void changeTimeCodesInXML() {
                    if (getCurrentNode() != null)
                        insertTimesAction.actionPerformed(new ActionEvent(TimeCodeModel.this, 0, "no.command"));
			//if (taskActions != null) {
			//	AbstractAction action = (AbstractAction)taskActions.get("qd.insertTimes");
		}*/
		public void setTimeCodes(long t1, long t2, Object node) {
			Object oldNode = currentNode;
			currentNode = node;
			if (!(this.t1 == t1 && this.t2 == t2)) {
				this.t1 = t1;
				this.t2 = t2;
				//see javadocs on EventListenerList for how following array is structured
				Object[] listeners = listenerList.getListenerList();
				for (int i = listeners.length-2; i>=0; i-=2) {
					if (listeners[i]==TimeCodeModelListener.class) {
						((TimeCodeModelListener)listeners[i+1]).setStartTime(t1);
						((TimeCodeModelListener)listeners[i+1]).setStopTime(t2);
					}
				}
				/*if (currentNode == oldNode) {
					if ((t2 >= t1 && t1 > -1) || (t1 > -1 && t2 == -1)) changeTimeCodesInXML(); //update the XML file
				}*/
			}
		}
		public void setNode(Object node) {
			Node playableparent = XPathUtilities.saxonSelectSingleDOMNode(node, (XPathExpression)(config.get("qd.nearestplayableparent")));
			if (playableparent == null) {
				setTimeCodes(-1, -1, node);
				thp.unhighlightAll();
				return;
			} else {
				String t1 = XPathUtilities.saxonSelectSingleDOMNodeToString(playableparent, (XPathExpression)(config.get("qd.nodebegins")));
				String t2 = XPathUtilities.saxonSelectSingleDOMNodeToString(playableparent, (XPathExpression)(config.get("qd.nodeends")));
				float f1, f2;
				if (t1 == null) f1 = -1;
				else f1 = new Float(t1).floatValue()*1000;
				if (t2 == null) f2 = -1;
				else f2 = new Float(t2).floatValue()*1000;
				setTimeCodes(new Float(f1).longValue(), new Float(f2).longValue(), node);
				thp.unhighlightAll();
			}
		}
}

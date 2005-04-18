/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2005 Michel Jacobson jacobson@idf.ext.jussieu.fr
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * ***** END LICENSE BLOCK ***** */

/*-----------------------------------------------------------------------*/
package fr.yanntool.gloser;

import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;

import org.jdom.*;
import org.jdom.output.*;
import org.jdom.transform.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

/*-----------------------------------------------------------------------*/

/** Manipulations de création de noeuds dans le document xml.
*/
public class domOperations {

		private StreamSource streamsource = null;

	/** Manipulations de création de noeuds dans le document xml.
	* Charge et applique les transformations xsl pour ajouter des éléments dans le document.
	*/
	public domOperations(frame_editor editor) {
		URL RESS_modifications_XSL      = getClass().getResource(editor.getPref("XSLT_modifications"));
		if (RESS_modifications_XSL != null) {
			String xslfile = RESS_modifications_XSL.toString();
			streamsource = new StreamSource(xslfile);
		} else {
			try {
				URL mofifFile = new URL(editor.getPref("XSLT_modifications"));
				String xslfile = editor.getPref("XSLT_modifications");
				streamsource = new StreamSource(xslfile);
			} catch (MalformedURLException e) {
				JOptionPane.showMessageDialog(null, "MalformedURLException: "+editor.getPref("XSLT_modifications"));
			}
		}
	}


	/** Emboitement d'une annotation (translation ou transcription) dans un niveau.
	*/

	private org.jdom.Element embedAnnotation(org.jdom.Element Elt, org.jdom.Element topElt, String level, String annotation) {
		org.jdom.Element result = topElt;
		if (streamsource != null) {
			try {
				DOMOutputter domOutputter = new DOMOutputter();
				org.w3c.dom.Document elt = domOutputter.output(new org.jdom.Document(Elt));
				TransformerFactory transformerfactory = TransformerFactory.newInstance();
				Transformer transformer = transformerfactory.newTransformer(streamsource);
				transformer.setParameter("doc", elt);
				transformer.setParameter("level", level);
				transformer.setParameter("annotation", annotation);
				transformer.setParameter("what", "embedAnnotation");
				JDOMResult out = new JDOMResult();
				JDOMSource in  = new JDOMSource(topElt);
				transformer.transform(in, out);
				if (out.getDocument() != null) {
					result = (org.jdom.Element)out.getDocument().getRootElement().detach();
				}
			} catch (Exception err) {
				JOptionPane.showMessageDialog(null, err.getMessage());
			}
		}
		return result;
	}
	public org.jdom.Element embedTranscr2level(String level, org.jdom.Element FORM, org.jdom.Element LEVEL) {
		return embedAnnotation(FORM, LEVEL, level, "transcription");
	}
	public org.jdom.Element embedGlose2level(String level, org.jdom.Element TRANSL, org.jdom.Element LEVEL) {
		return embedAnnotation(TRANSL, LEVEL, level, "translation");
	}

	/** emboitement d'un niveau dans un autre niveau.
	*/

	public org.jdom.Element embedWord2sentence(org.jdom.Element W, org.jdom.Element S) {
		return embed(W, S);
	}
	public org.jdom.Element embedSentence2text(org.jdom.Element S, org.jdom.Element T) {
		return embed(S, T);
	}
	public org.jdom.Element embedMorpheme2word(org.jdom.Element M, org.jdom.Element W) {
		return embed(M, W);
	}
	private org.jdom.Element embed(org.jdom.Element Elt, org.jdom.Element topElt) {
		org.jdom.Element result = topElt;
		if (streamsource != null) {
			try {
				DOMOutputter domOutputter = new DOMOutputter();
				org.w3c.dom.Document elt = domOutputter.output(new org.jdom.Document(Elt));
				TransformerFactory transformerfactory = TransformerFactory.newInstance();
				Transformer transformer = transformerfactory.newTransformer(streamsource);
				transformer.setParameter("doc", elt);
				transformer.setParameter("what", "embedLevel");
				JDOMResult out = new JDOMResult();
				JDOMSource in  = new JDOMSource(topElt);
				transformer.transform(in, out);
				if (out.getDocument() != null) {
					result = (org.jdom.Element)out.getDocument().getRootElement().detach();
				}
			} catch (Exception err) {
				JOptionPane.showMessageDialog(null, err.getMessage());
			}
		}
		return result;
	}


	/** creation d'un nouveau document.
	*/

	public org.jdom.Document createDocument() {
		org.jdom.Document result = null;
		if (streamsource != null) {
			try {
				TransformerFactory transformerfactory = TransformerFactory.newInstance();
				Transformer transformer = transformerfactory.newTransformer(streamsource);
				transformer.setParameter("level", "doc");
				transformer.setParameter("what", "createLevel");
				JDOMResult out = new JDOMResult();
				JDOMSource in  = new JDOMSource(new org.jdom.Element("empty"));
				transformer.transform(in, out);
				if (out.getDocument() != null) {
					result = out.getDocument();
				}
			} catch (Exception err) {
				JOptionPane.showMessageDialog(null, err.getMessage());
			}
		}
		return result;
	}
	public org.jdom.Element createSentence() {
		return createLevel("sentence");
	}
	public org.jdom.Element createWord() {
		return createLevel("word");
	}
	public org.jdom.Element createMorpheme() {
		return createLevel("morpheme");
	}
	private org.jdom.Element createLevel(String level) {
		org.jdom.Element result = null;
		if (streamsource != null) {
			try {
				TransformerFactory transformerfactory = TransformerFactory.newInstance();
				Transformer transformer = transformerfactory.newTransformer(streamsource);
				transformer.setParameter("level", level);
				transformer.setParameter("what", "createLevel");
				JDOMResult out = new JDOMResult();
				JDOMSource in  = new JDOMSource(new org.jdom.Element("empty"));
				transformer.transform(in, out);
				if (out.getDocument() != null) {
					result = (org.jdom.Element)out.getDocument().getRootElement().detach();
				}
			} catch (Exception err) {
				JOptionPane.showMessageDialog(null, err.getMessage());
			}
		}
		return result;
	}
	public org.jdom.Element createLevelTranscr(String level) {
		return createAnnotation(level, "transcription");
	}
	public org.jdom.Element createLevelGlose(String level) {
		return createAnnotation(level, "translation");
	}

	private org.jdom.Element createAnnotation(String level, String annotation) {
		org.jdom.Element result = null;
		if (streamsource != null) {
			try {
				TransformerFactory transformerfactory = TransformerFactory.newInstance();
				Transformer transformer = transformerfactory.newTransformer(streamsource);
				transformer.setParameter("level", level);
				transformer.setParameter("annotation", annotation);
				transformer.setParameter("what", "createAnnotation");
				JDOMResult out = new JDOMResult();
				JDOMSource in  = new JDOMSource(new org.jdom.Element("empty"));
				transformer.transform(in, out);
				if (out.getDocument() != null) {
					result = (org.jdom.Element)out.getDocument().getRootElement().detach();
				}
			} catch (Exception err) {
				JOptionPane.showMessageDialog(null, err.getMessage());
			}
		}
		return result;
	}
};
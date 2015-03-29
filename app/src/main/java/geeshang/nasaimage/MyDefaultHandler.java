/****************************************************************************
 Copyright (c) 2015 Geeshang Xu (Geeshangxu@gmail.com)

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
package geeshang.nasaimage;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


class MyDefaultHandler extends DefaultHandler {
    private final MainActivity mMainActivity;
    private String tagName;
    private int itemCount;
    private boolean inItem;

    MyDefaultHandler(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("item")) {
            inItem = true;
        } else if (localName.equals("enclosure")) {
            mMainActivity.image_url[itemCount] = atts.getValue(0);
        }
        tagName = localName;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("item")) {
            inItem = false;
            itemCount++;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inItem) {
            switch (tagName) {
                case "title":
                    mMainActivity.title_data[itemCount].append(new String(ch, start, length));
                    break;
                case "pubDate":
                    mMainActivity.date_data[itemCount].append(new String(ch, start, length));
                    break;
                case "description":
                    mMainActivity.description_data[itemCount].append(new String(ch, start, length));
                    break;
            }
        }
    }
}


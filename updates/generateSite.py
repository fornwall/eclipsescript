#!/usr/bin/env python
# Create Eclipse feature jars from plug-in jars in the plugins folder
# See: http://www.eclipse.org/articles/Article-Update/keeping-up-to-date.html
# for more information about features and update sites

# for ganymede compliance, see
# https://bugs.eclipse.org/bugs/show_bug.cgi?id=236142
# http://wiki.eclipse.org/WTP/What_we_have_learned_(to_love)_about_P2

import re
from glob import glob
from os import stat, remove
from xml.dom.minidom import parseString
from zipfile import ZipFile


descriptions = {
  'org.eclipsescript': 'Plug-in allowing scripting of the Eclipse IDE using javascript.',
  'org.mozilla.javascript': 'Mozilla Rhino 1.7r2.'
}


def readProps(lines):
        result = {}
        for line in lines:
                if '=' in line and not line.startswith('#'):
                        line = line.strip()
                        i = line.index('=')
                        result[line[0:i].strip()] = line[i+1:len(line)].strip()
        return result

def unzippedSize(zipFile):
        "Return the size of the zipFile it it were to be uncompressed"
	return sum([info.file_size for info in zipFile.infolist()])

def featureXMLFromPlugin(pluginJarFileName):
	"Return a feature.xml for the ZipFile passed"
        print 'Parsing ' + pluginJarFileName
	result = ''
	pluginJar = ZipFile(pluginJarFileName)
	manifest = pluginJar.read('META-INF/MANIFEST.MF')
        for line in manifest.split("\n"):
            if line.find('Bundle-SymbolicName') != -1:
                featureID = re.match('Bundle-SymbolicName: ([^;]*)', line).group(1).strip()
                print 'FeatureId: ' + featureID
            if line.find('Bundle-Version:') != -1:
                featureVersion = re.match('Bundle-Version: (.*)', line).group(1).strip()
                print 'Version: ' + featureVersion
            if line.find('Bundle-Name:') != -1:
                featureLabel = re.match('Bundle-Name: (.*)', line).group(1).strip()
		if featureLabel.startswith('%'): # localized
			featureLabel = readProps(pluginJar.read('plugin.properties').split('\n'))[featureLabel[1:len(featureLabel)]]
                print 'Name: ' + featureLabel
        print ''

	result += '<?xml version="1.0" encoding="UTF-8"?>\n'
	result += '<feature id="%s" version="%s" label="%s" provider-name="eclipsescript.org">\n' % \
		(featureID, featureVersion, featureLabel)
	result += '  <install-handler/>\n'

	result += '  <description url="http://eclipsescript.org/updates/">' + \
		descriptions[featureID] + \
		'</description>\n'
	#result += '  <copyright>2010 Fredrik Fornwall. All rights reserved.</copyright>\n'
	result += '  <license>Eclipse Public License 1.0</license>\n'
	result += '  <url><update label="EclipseScript Update Site" url="http://eclipsescript.org/updates/"/></url>\n'

        if 'Require-Bundle' in manifest:
	    result += '  <requires>\n'
	    # get content between "Require-Bundle:" and next entry:
	    reg = re.compile('Require-Bundle:(.+?)(^[^ ])', re.DOTALL | re.MULTILINE)
            #for package in manifest.split("Require-Bundle:")[1].split("Bundle")[0].split(","):
            for package in reg.search(manifest).group(1).split(","):
		# handle lines with version: net.fornwall.eclipsecoder;bundle-version="0.2.7",
		package = package.strip().replace('\n', '').replace('\r', '').replace(' ', '')
		if ';' in package: package = package[0:package.index(';')]
		if package[0:1].isdigit():
			#  handle commans in "eclipsecoder;bundle-version="x.x,y,y". ugly...
			continue
                result += '    <import plugin="%s"/>\n' % package
	    result += '  </requires>\n'

	# Note the the unit is KB. TODO: Update to KB?
	jarSize = stat(pluginJarFileName).st_size / 1024
	result += '  <plugin id="%s" unpack="false" download-size="%s" install-size="%s" version="%s"/>\n' % \
		(featureID, jarSize, jarSize, featureVersion)
        #if 'net.fornwall.eclipsecoder' == featureID:
            #result += '  <plugin id="%s" unpack="false" download-size="%s" install-size="%s" version="%s"/>\n' % \
                    #(appletID, appletSize, appletSize, appletVersion)
	result += '</feature>\n'
	return (featureID, featureVersion, result)

#'<site associateSitesURL="http://fornwall.net/eclipsecoder/associates.xml">\n' + \
siteXML = '<?xml version="1.0" encoding="UTF-8"?>\n' + \
	'<site>\n' + \
	'  <description url="http://eclipsescript.org/updates/">\n' + \
	'    Update site for EclipseScript\n' + \
	'  </description>\n'

# Clear old featuers
for f in glob('features/*'): remove(f)

# Go through all plugin jars and sort them in wanted order
files = glob('plugins/*')
wantedOrder = [
  'org.eclipsescript',
  'org.mozilla.javascript'
]
def pluginSort(a, b):
	# plugins/..._0.2.0.jar
	aName = a[a.index('/')+1:a.index('_')]
	bName = b[b.index('/')+1:b.index('_')]
	return wantedOrder.index(aName) - wantedOrder.index(bName)
files.sort(pluginSort)

siteXML += '  <category-def name="EclipseScript" label="EclipseScript">\n'
siteXML += '    <description>Plug-in allowing scripting of the Eclipse IDE using javascript.</description>\n'
siteXML += '  </category-def>\n'

for f in files:
        (featureID, featureVersion, featureXML) = featureXMLFromPlugin(f)
        if featureID == None: continue
	featureJarFileName = f.replace('plugins/', 'features/').replace('.jar', '-feature.jar')
	featureJar = ZipFile(featureJarFileName, 'w')
        featureJar.writestr('feature.xml', featureXML)

        # Update site.xml
        siteXML += '  <feature id="%s" url="%s" version="%s"><category name="EclipseScript"/></feature>\n' % (featureID, featureJarFileName, featureVersion)

siteXML += '</site>'

print 'Writing file site.xml:\n', siteXML
open('site.xml', 'w').write(siteXML)


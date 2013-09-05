#!/bin/bash

perl -pi -e 's#src="js/#src="/pentaho/content/pentaho-cdf-dd/getJsResource?resource=js/#g' resource/resources/cdf-dd.html
perl -pi -e 's#src="images/#src="/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/#g' resource/resources/cdf-dd.html

perl -pi -e 's#href="css/#href="/pentaho/content/pentaho-cdf-dd/getCssResource?resource=css/#g' resource/resources/cdf-dd.html
perl -pi -e 's#url\(../images/#url\(/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/#g' resource/css/*.css
perl -pi -e 's#icon: \"images/#icon: \"/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/#g' resource/js/*.js
#perl -pi -e 's#url\(images/#url\(/pentaho/content/pentaho-cdf-dd/getResource?resource=/js/theme/images/#g' resource/js/theme/ui.theme.css

perl -pi -e  's#url\(images/#url\(/pentaho/content/pentaho-cdf-dd/getResource?resource=/css/smoothness/images/#g' resource/css/smoothness/jquery-ui-1.7.2.custom.css
perl -pi -e  's#url\(images/#url\(/pentaho/content/pentaho-cdf-dd/getResource?resource=/css/sunny/images/#g' resource/css/sunny/jquery-ui-1.7.2.custom.css

perl -pi -e 's#url\(icons/#url\(/pentaho/content/pentaho-cdf-dd/getResource?resource=/css/blueprint/plugins/link-icons/icons/#g' resource/css/blueprint/plugins/link-icons/screen.css
perl -pi -e 's#url\(src/#url\(/pentaho/content/pentaho-cdf-dd/getResource?resource=/css/blueprint/src/#g' resource/css/blueprint/screen.css
perl -pi -e 's#url\(src/#url\(/pentaho/content/pentaho-cdf-dd/getResource?resource=/css/blueprint/src/#g' resource/css/blueprint/src/grid.css


perl -pi -e  's#url\(images/#url\(/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/#g' resource/css/jqueryFileTree.css




#!/bin/bash

OUTPUT=templates.js
folder=./templates
echo "(function(templates){" > $OUTPUT

echo "  $.extend( true, templates, {" >> $OUTPUT
for file in $(find $folder -type f -iname "*.mustache")
do
    echo "Processing $file"
    echo '    "'$(basename $file .mustache)'": [' >> $OUTPUT
    while read -r line
    do
        echo -e "      '"$line"',"  >> $OUTPUT
    done < $file
    echo "      ''" >> $OUTPUT
    echo '    ].join(""),' >> $OUTPUT
done
echo '    undefined: "No template" ' >> $OUTPUT
echo '  });' >> $OUTPUT

echo "})(TreeFilter.templates);" >> $OUTPUT
